package com.bt.cqm.config;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Properties;

import static java.lang.String.*;

class ConfigurationInvocationHandler implements InvocationHandler {
    private final ConfigurationElement configurationElement;

    public ConfigurationInvocationHandler(ConfigurationElement configurationElement) {
        this.configurationElement = configurationElement;
    }

    static <T> T createElementInstance(Class<T> elementClass, ConfigurationElement childElement) {
        //noinspection unchecked
        return (T) Proxy.newProxyInstance(elementClass.getClassLoader(),
                new Class<?>[]{elementClass},
                new ConfigurationInvocationHandler(childElement));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws ConfigurationException {
        Class<?> returnType = method.getReturnType();
        if (returnType.isArray()) {
            return getChildrenOfElement(configurationElement, returnType.getComponentType());
        } else if (returnType.isInterface()) {
            if (args != null && args.length == 1) {
                String id = (String) args[0];
                return getChildOfElementById(configurationElement, returnType, id);
            }
            return getChildOfElement(configurationElement, returnType);
        } else if (Properties.class.equals(returnType)) {
            return parseProperties(method);
        }
        return getAttributeValue(method, returnType);
    }

    private Object parseProperties(Method method) {
        Properties properties = new Properties();
        ConfigurationElement propertiesElement = (ConfigurationElement) configurationElement.getChildElement(deriveElementNameFromMethodName(method.getName()));
        for (ConfigurationElement propertyElement : propertiesElement.getChildElements("property")) {
            if (propertyElement.isSetAttributeValue("valueref")) {
                properties.setProperty(propertyElement.getAttributeValue("name"), propertyElement.getAttributeValue("valueref"));
            } else {
                properties.setProperty(propertyElement.getAttributeValue("name"), propertyElement.getAttributeValue("value"));
            }
        }
        return properties;
    }

    private <T> T getChildOfElement(ConfigurationElement parentElement, Class<T> elementClass) {
        ConfigurationElement childElement = parentElement.getChildElement(deriveElementNameFromClassName(elementClass));
        childElement = childElement.dereference();
        return createElementInstance(elementClass, childElement);
    }

    private static String deriveElementNameFromClassName(Class<?> elementClass) {
        String className = elementClass.getSimpleName();
        String suffix = "Config";
        if (className.endsWith(suffix)) {
            return className.substring(0, className.length() - suffix.length());
        }
        return className;
    }

    private <T> T getChildOfElementById(ConfigurationElement parentElement, Class<T> elementClass, String id) {
        ConfigurationElement childElement = parentElement.getChildElementById(deriveElementNameFromClassName(elementClass), id);
        return createElementInstance(elementClass, childElement.dereference());
    }

    private <T> T[] getChildrenOfElement(ConfigurationElement parentElement, Class<T> componentType) {
        List<? extends ConfigurationElement> elementList = parentElement.getChildElements(
                deriveElementNameFromClassName(componentType));
        T[] elements = (T[]) Array.newInstance(componentType, elementList.size());
        for (int i = 0; i < elements.length; i++) {
            T elementInstance = createElementInstance(componentType, elementList.get(i).dereference());
            elements[i] = elementInstance;
        }
        return elements;
    }

    private Object getAttributeValue(Method method, Class<?> returnType) throws ConfigurationException {
        String attributeName = convertMethodNameToAttributeName(method.getName());
        String stringValueOfAttribute = configurationElement.getAttributeValue(attributeName);
      /*  if (stringValueOfAttribute == null) {
            throw new ConfigurationException(format("Requested configuration attribute %s@%s is missing from configuration file", configurationElement.getName(), attributeName));
        }*/
        return convertStringValueToType(stringValueOfAttribute, returnType);
    }

    private Object convertStringValueToType(String attributeValue, Class<?> returnType) throws ConfigurationException {
        if (returnType.isAssignableFrom(String.class)) {
            return attributeValue;
        } else if (returnType.isAssignableFrom(Integer.TYPE)) {
            return convertStringValueToInt(attributeValue);
        } else if (returnType.isAssignableFrom(Long.TYPE)) {
            return convertStringValueToLong(attributeValue);
        } else if (returnType.isEnum()) {
            return convertStringValueToEnum(attributeValue, (Class<? extends Enum>) returnType);
        }

        throw new ConfigurationException(format("Unsupported attribute return type:%s", returnType));
    }

    private <E extends Enum<E>> Enum<E> convertStringValueToEnum(String attributeValue, Class<E> returnType) {
        try {
            return Enum.valueOf(returnType, attributeValue);
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException(attributeValue + " is not a valid for enumerated type: " + returnType.getName(), e);
        }
    }

    private static int convertStringValueToInt(String attributeValue) throws ConfigurationException {
        try {
            return Integer.parseInt(attributeValue);
        } catch (NumberFormatException e) {
            throw new ConfigurationException(format("Unable to convert attribute value: %s to int", attributeValue), e);
        }
    }

    private static long convertStringValueToLong(String attributeValue) {
        try {
            return Long.parseLong(attributeValue);
        } catch (NumberFormatException e) {
            throw new ConfigurationException(format("Unable to convert attribute value: %s to long", attributeValue), e);
        }
    }

    private static String convertMethodNameToAttributeName(String methodName) {
        if (methodName.startsWith("get")) {
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        }
        return methodName;
    }

    private static String deriveElementNameFromMethodName(String methodName) {
        if (methodName.startsWith("get")) {
            return methodName.substring(3);
        }
        return methodName;
    }
}
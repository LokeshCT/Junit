package com.bt.cqm.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.*;

public class XmlConfigurationElement implements ConfigurationElement {
  private final List<XmlConfigurationElement> children = new ArrayList<XmlConfigurationElement>();
  private String tagName;
  private final Map<String, String> attributes = new HashMap<String, String>();
  private XmlConfigurationElement parent;
  private static final String VALUE_REF = "valueref";
  private static final Pattern INLINE_REF = Pattern.compile("^\\$\\{([\\w\\-\\.]+)\\}$");

  XmlConfigurationElement(String tagName, Map<String, String> attributes) {
    this.attributes.putAll(attributes);
    this.tagName = tagName;
  }

  @Override
  public String getAttributeValue(String attributeName) {
    String attributeValue = attributes.get(attributeName);
    if (VALUE_REF.equals(attributeName)) {
      return dereferenceAttributeValue(attributeValue);
    }
    if (attributeValue != null) {
      Matcher inlineReferenceMatcher = INLINE_REF.matcher(attributeValue);
      if (inlineReferenceMatcher.matches()) {
        String refId = inlineReferenceMatcher.group(1); // The reference is the bit between the braces
        return dereferenceAttributeValue(refId);
      }
    }
    return attributeValue;
  }

  protected String dereferenceAttributeValue(String valueRefId) {
    for (ConfigurationElement child : getChildElements("CONSTANT")) {
      if (valueRefId.equals(child.getAttributeValue("id"))) {
        return child.getAttributeValue("value");
      }
    }

    // Not available in a local scope, try a wider one
    if (!this.isRootElement()) {
      return parent.dereferenceAttributeValue(valueRefId);
    } else {
      throw new ConfigurationException(String.format("Value with reference %s is undefined", valueRefId));
    }
  }

  @Override
  public boolean isSetAttributeValue(String attributeName) {
    return attributes.get(attributeName) != null;
  }

  @Override
  public ConfigurationElement getChildElement(String name) throws ConfigurationException {
    return findChildByTagName(name);
  }

  private XmlConfigurationElement findChildByTagName(String name) {
    List<XmlConfigurationElement> childrenWithTagName = findChildrenByTagName(name);

    if (childrenWithTagName.isEmpty()) {
      throw new ConfigurationException("Requested configuration element does not exist: " + name);
    }

    return childrenWithTagName.get(0);
  }

  private List<XmlConfigurationElement> findChildrenByTagName(String name) {
    List<XmlConfigurationElement> childrenWithTagName = new ArrayList<XmlConfigurationElement>();
    for (XmlConfigurationElement childElement : children) {
      if (childElement.getTagName().equals(name)) {
        childrenWithTagName.add(childElement);
      }
    }

    return childrenWithTagName;
  }

  @Override
  public ConfigurationElement getChildElementById(String name, String id) throws ConfigurationException {

    XmlConfigurationElement childWithId = findChildWithId(name, id);
    if (childWithId != null) {
      return childWithId;
    }

    throw new ConfigurationException(format("Requested configuration element does not exist: %s@id='%s'", name, id));
  }

  private XmlConfigurationElement findChildWithId(String name, String id) {
    XmlConfigurationElement childWithId = null;
    List<XmlConfigurationElement> elementList = findChildrenByTagName(name);
    for (XmlConfigurationElement element : elementList) {
      if (element.hasId() && element.getId().equals(id)) {
        childWithId = element;
      }
    }

    return childWithId;
  }

  private boolean hasId() {
    return getId() != null;
  }

  @Override
  public String getName() {
    return getTagName();
  }

  @Override
  public List<? extends ConfigurationElement> getChildElements(String name) {
    return findChildrenByTagName(name);
  }

  String getTagName() {
    return tagName;
  }

  String getId() {
    return attributes.get("id");
  }

  @Override
  public ConfigurationElement dereference() {
    return isReference() ? parent.dereferenceElement(tagName, getRef()) : this;
  }

  @Override
  public boolean isReference() {
    return getRef() != null;
  }

  private XmlConfigurationElement dereferenceElement(String tagName, String ref) {

    XmlConfigurationElement referencedElement = findChildWithId(tagName, ref);

    if (referencedElement == null) {
      if (isRootElement()) {
        throw new ConfigurationException(String.format("Referenced element %s@id='%s' not found in scope", tagName, ref));
      }
      referencedElement = parent.dereferenceElement(tagName, ref);
    }

    return referencedElement;
  }

  boolean isRootElement() {
    return parent == null;
  }

  private String getRef() {
    return attributes.get("ref");
  }

  void addChildElement(XmlConfigurationElement child) {
    child.setParent(this);
    children.add(child);
  }

  private void setParent(XmlConfigurationElement parent) {
    this.parent = parent;
  }

  public List<XmlConfigurationElement> getChildAllElements() {
    return children;
  }
}

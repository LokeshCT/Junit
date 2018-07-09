package com.bt.cqm.config;

import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConfigurationProvider {

    private static final ConfigurationLog log = LogFactory.createDefaultLogger(ConfigurationLog.class);
    private static Map<Class<?>, Map<String, Configuration<?>>> configs = new HashMap<Class<?>, Map<String, Configuration<?>>>();

    private ConfigurationProvider() {
    }

    public static <T> T provide(Class<T> configClass, final String environment) {
        if (configs.get(configClass) == null ||
            configs.get(configClass).get(environment) == null) {
            loadConfig(configClass, environment);
        }
        return (T) configs.get(configClass).get(environment).getConfigurationRoot();
    }

    public static <T> T provide(Class<T> configClass){
        if (configs.get(configClass) != null ) {
            Map<String,Configuration<?>> configClassMap =configs.get(configClass);

            if(configClassMap!=null)
            {
            Set<String> envs= configClassMap.keySet();
            /*
            Assuming only one config gets loaded for a given application.
            Hence retrieve the first available config , which has got loaded at the app startup.
            */
                if(envs!=null)
                {
                    String key=envs.iterator().next();
                    if(key!=null && key.trim().length()>0)
                    {
                    return (T) (configClassMap.get(key).getConfigurationRoot());
                    }
                }

            }


        }
        throw new ConfigurationException("Unable to load the Configuration for "+configClass==null?"NULL":configClass.getSimpleName());
    }

    private static ConfigurationParser getParser(InputStream stream, Class resourceLoaderContext) {
        return new XmlConfigurationParser(stream, resourceLoaderContext);
    }

    private static synchronized <T> void loadConfig(Class<T> configClass, String environment) {
        String relativeResourcePath = String.format("%s.xml", environment);
        URL resourceLocation = configClass.getResource(relativeResourcePath);
        try {
            if (null == resourceLocation) {
                throw new NullPointerException("Unable to load resource: " + resourceLocation +" from relativeResourcePath :"+relativeResourcePath);
            }
            InputStream stream = resourceLocation.openStream();
            try {
                log.readingConfigFrom(resourceLocation);
                readConfig(configClass, environment, stream);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to load resource: " + resourceLocation, e);
        }
    }

    private static <T> void readConfig(Class<T> configClass, String environment, InputStream stream) throws IOException {
        Map<String, Configuration<?>> byClass = configs.get(configClass);
        if (byClass == null) {
            configs.put(configClass, new HashMap<String, Configuration<?>>());
        }
        byClass = configs.get(configClass);
        byClass.put(environment, new Configuration<T>(configClass, getParser(stream, configClass)));
    }

    public interface ConfigurationLog {
        @Log(level = LogLevel.INFO, format = "Reading config from '%s'")
        void readingConfigFrom(URL resourceLocation);
    }
}

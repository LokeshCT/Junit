package com.asidua.statsintegration.utilities;

import com.asidua.statsintegration.services.ParameterMap;
import com.asidua.statsintegration.services.TestInvocationException;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

import static com.google.common.collect.Maps.*;

public class Templater {
    private final Configuration freeMarker;
    public Templater() {
        this("");
    }
    public Templater(final String path) {
        freeMarker = new Configuration() {{
            setEncoding(Locale.UK, "UTF-8");
            setURLEscapingCharset("UTF-8");
            setTemplateLoader(new ClassTemplateLoader(getClass(), path));
        }};
    }

    public String toString(String templateName, ParameterMap map) throws TestInvocationException {
        try {
            Map<String,Object> parameters = newHashMap();
            parameters.put("params", map);
            StringWriter writer = new StringWriter();
            freeMarker.getTemplate(templateName).process(parameters, writer);
            return writer.toString();
        } catch (IOException e) {
            throw new TestInvocationException ("Unexpected IO Exception while processing template" + templateName, e);
        } catch (TemplateException e) {
            throw new TestInvocationException("Unexpected IO Exception while processing template" + templateName, e);
        }
    }


    public String toTempFile(String templateName, ParameterMap map) throws TestInvocationException{
        try {
            Map<String,Object> parameters = newHashMap();
            parameters.put("params", map);
            File tempfile = File.createTempFile("TI_",".xml");
            FileWriter fileWriter = new FileWriter(tempfile,true);
            freeMarker.getTemplate(templateName).process(parameters, fileWriter);
            System.out.println("Parameters written to file " + tempfile.getAbsolutePath());
            String path = tempfile.getCanonicalPath();
            fileWriter.close();
            tempfile.deleteOnExit();
            return path;
        } catch (IOException e) {
            throw new TestInvocationException ("Unexpected IO Exception while processing template" + templateName, e);
        } catch (TemplateException e) {
            throw new TestInvocationException("Unexpected Template Processing Exception while processing " + templateName, e);
        }
    }
}

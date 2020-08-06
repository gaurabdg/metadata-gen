package org.example;

import java.io.FileOutputStream;
import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class XMLMetaWriter {
    public void write(ModuleDetails moduleDetails) throws IOException {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("checkstyle-metadata").addElement("module");
        Element checkModule = null;
        ModuleType moduleType = moduleDetails.getModuleType();
        if (moduleType == ModuleType.CHECK) {
            checkModule = root.addElement("check");
        }
        else if (moduleType == ModuleType.FILTER) {
            checkModule = root.addElement("filter");
        }
        else if (moduleType == ModuleType.FILEFILTER) {
            checkModule = root.addElement("file-filter");
        }
        checkModule.addAttribute("name", moduleDetails.getName());
        checkModule.addAttribute("fully-qualified-name", moduleDetails.getFullQualifiedName());
        // check parent parsing in Sonar
        checkModule.addAttribute("parent", moduleDetails.getParent());
        checkModule.addElement("description").addCDATA(moduleDetails.getDescription());
        if (!moduleDetails.getProperties().isEmpty()) {
            Element properties = checkModule.addElement("properties");
            for (ModulePropertyDetails modulePropertyDetails : moduleDetails.getProperties()) {
                Element property = properties.addElement("property");
                property.addAttribute("name", modulePropertyDetails.getName());
                property.addAttribute("type", modulePropertyDetails.getType());
                property.addAttribute("default-value", modulePropertyDetails.getDefaultValue());
                property.addAttribute("validation-type", modulePropertyDetails.getValidationType());
                property.addElement("description").addCDATA(modulePropertyDetails.getDescription());
            }
        }

        if (!moduleDetails.getViolationMessageKeys().isEmpty()) {
            Element violationMessages = checkModule.addElement("message-keys");
            for (String msg : moduleDetails.getViolationMessageKeys()) {
                violationMessages.addElement("message-key").addAttribute("key", msg);
            }
        }

        OutputFormat format = OutputFormat.createPrettyPrint();
        String moduleName = moduleDetails.getName();
        if (moduleDetails.getModuleType() == ModuleType.CHECK) {
            moduleName += "Check";
        }
        XMLWriter writer =
                new XMLWriter(new FileOutputStream(Main.outputRootPath + moduleName + ".xml"),
                        format);
        writer.write(document);
    }
}

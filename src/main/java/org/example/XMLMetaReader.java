package org.example;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class XMLMetaReader {
    public ModuleDetails read(InputStream moduleMetadataStream, ModuleType moduleType) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ignored) {}
        Document document = null;
        try {
            document = builder.parse(moduleMetadataStream);
        } catch (SAXException | IOException ex) {
            ex.printStackTrace();
        }
        Element root = document.getDocumentElement();
        Element element = getDirectChildsByTag(root, "module").get(0);
        Element module = null;
        if (moduleType == ModuleType.CHECK) {
            module = getDirectChildsByTag(element, "check").get(0);
        }
        else if (moduleType == ModuleType.FILTER) {
            module = getDirectChildsByTag(element, "filter").get(0);
        }
        else if (moduleType == ModuleType.FILEFILTER) {
            module = getDirectChildsByTag(element, "file-filter").get(0);
        }
        return createModule(module);
    }

    public static ModuleDetails createModule(Element mod) {
        ModuleDetails check = new ModuleDetails();
        check.setName(getAttributeValue(mod, "name"));
        check.setFullQualifiedName(getAttributeValue(mod, "fully-qualified-name"));
        check.setParent(getAttributeValue(mod, "parent"));
        check.setDescription(getDirectChildsByTag(mod, "description").get(0).getFirstChild().getNodeValue());
        List<Element> properties = getDirectChildsByTag(mod, "properties") ;
        if (!properties.isEmpty()) {
            List<ModulePropertyDetails> modulePropertyDetailsList =
                    createProperties(properties.get(0));
            check.addToProperties(modulePropertyDetailsList);
        }
        List<String> messageKeys = getListContentByAttribute(mod, "message-keys", "message-key",
                "key");
        if (messageKeys != null) {
            check.addToViolationMessages(messageKeys);
        }
        return check;
    }

    public static List<ModulePropertyDetails> createProperties(Element properties) {
        List<ModulePropertyDetails> result = new ArrayList<>();
        NodeList propertyList = properties.getElementsByTagName("property");
        for (int i = 0;i < propertyList.getLength(); i++) {
            ModulePropertyDetails propertyDetails = new ModulePropertyDetails();
            Element prop = (Element) propertyList.item(i);
            propertyDetails.setName(getAttributeValue(prop, "name"));
            propertyDetails.setType(getAttributeValue(prop, "type"));
            propertyDetails.setDefaultValue(getAttributeValue(prop, "default-value"));
            if (prop.hasAttribute("validation-type")) {
                propertyDetails.setValidationType(getAttributeValue(prop, "validation-type"));
            }
            propertyDetails.setDescription(getDirectChildsByTag(prop, "description").get(0).getFirstChild().getNodeValue());
            result.add(propertyDetails);
        }
        return result;
    }

    public static List<String> getListContentByAttribute(Element element, String listParent,
                                                         String listOption, String attribute) {
        List<Element> children = getDirectChildsByTag(element, listParent);
        List<String> result = null;
        if (!children.isEmpty()) {
            NodeList nodeList = children.get(0).getElementsByTagName(listOption);
            List<String> listContent = new ArrayList<>();
            for (int j = 0;j < nodeList.getLength(); j++) {
                listContent.add(getAttributeValue((Element) nodeList.item(j), attribute));
            }
            result = listContent;
        }
        return result;
    }

    public static List<Element> getDirectChildsByTag(Element element, String sTagName) {
        NodeList children = element.getElementsByTagName(sTagName);
        List<Element> res = new ArrayList<>();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getParentNode().equals(element)) {
                res.add((Element) children.item(i));
            }
        }
        return res;
    }

    public static String getAttributeValue(Element element, String attribute) {
        return element.getAttributes().getNamedItem(attribute).getNodeValue();
    }

    public List<ModuleDetails> readAllModules() {
        List<ModuleDetails> result = new ArrayList<>();
        Reflections reflections = new Reflections("org.example", new ResourcesScanner());
        Set<String> fileNames = reflections.getResources(Pattern.compile(".*\\.xml"));
        fileNames.forEach(fileName -> {
            ModuleType moduleType;
            if (fileName.endsWith("FileFilter.xml")) {
                moduleType = ModuleType.FILEFILTER;
            }
            else if (fileName.endsWith("Filter.xml")) {
                moduleType = ModuleType.FILTER;
            }
            else {
                moduleType = ModuleType.CHECK;
            }
            try (InputStream inputStream = getClass().getResourceAsStream("/" + fileName)) {
                result.add(read(inputStream, moduleType));
            }
            catch (IOException ignored) {
            }
        });
        return result;
    }
}
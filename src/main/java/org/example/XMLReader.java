package org.example;

import java.io.File;

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

public class XMLReader {
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
        ModuleDetails result = null;
        if (moduleType == ModuleType.CHECK) {
            result = createCheck(element);
        }
//        else {
//            result = createFilter(element);
//        }
        return result;
    }

    public static ModuleDetails createCheck(Element module) {
        Element mod = getDirectChildsByTag(module, "check").get(0);
        ModuleDetails check = new ModuleDetails();
        check.setName(getAttributeValue(mod, "name"));
        check.setFullQualifiedName(getAttributeValue(mod, "fullyQualifiedName"));
        check.setParent(getAttributeValue(mod, "parent"));
        check.setDescription(getDirectChildsByTag(mod, "description").get(0).getFirstChild().getNodeValue());
        List<ModulePropertyDetails> modulePropertyDetailsList =
                createProperties(getDirectChildsByTag(mod, "properties").get(0));
        check.setProperties(modulePropertyDetailsList);
        check.setModulePropertyByKey(modulePropertyDetailsList);
        check.setViolationMessageKeys(getListContentByAttribute(mod, "message-keys", "message-key", "key"));
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
            propertyDetails.setValueType(getAttributeValue(prop, "value-type"));
            propertyDetails.setDescription(getDirectChildsByTag(prop, "description").get(0).getFirstChild().getNodeValue());
            propertyDetails.setValues(getListContentByAttribute(prop, "property-options",
                    "property-option", "value"));
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
}

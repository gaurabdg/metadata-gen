import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class XMLReaderTest {
    @Test
    public void testModuleDetails() throws Exception{
        XMLReader xr = new XMLReader();
        ModuleDetails actualEmptyBlockDetails = xr.read("EmptyBlockCheck");
        ModuleDetails emptyBlockCheck = new ModuleDetails();
        assertEquals("EmptyBlock", actualEmptyBlockDetails.getName(), "Name doesn't match");
        assertEquals("com.puppycrawl.tools.checkstyle.checks.blocks.EmptyBlockCheck",
                actualEmptyBlockDetails.getFullQualifiedName(), "Full qualified name doesn't "
                        + "match");
        assertEquals("TreeWalker", actualEmptyBlockDetails.getParent(), "Parent doesn't match");
        assertEquals("<p>Checks for empty blocks. This check does not validate sequential blocks.</p>\n"
                + "        <p>Sequential blocks won't be checked. Also, no violations for fallthrough:</p>\n"
                + "        <pre>switch (a) {\n"
                + "                case 1:                          // no violation\n"
                + "                case 2:                          // no violation\n"
                + "                case 3: someMethod(); { }        // no violation\n"
                + "                default: break;\n"
                + "              }</pre>\n"
                + "        <p>This check processes LITERAL_CASE and LITERAL_DEFAULT separately.\n"
                + "              So, if tokens=LITERAL_DEFAULT, following code will not trigger any violation,\n"
                + "              as the empty block belongs to LITERAL_CASE:</p>\n"
                + "        <p>Configuration:</p>\n"
                + "        <pre>&lt;module name=\"EmptyBlock\"&gt;\n"
                + "                &lt;property name=\"tokens\" value=\"LITERAL_DEFAULT\"/&gt;\n"
                + "              &lt;/module&gt;</pre>\n"
                + "        <p>Result:</p>\n"
                + "        <pre>switch (a) {\n"
                + "                default:        // no violation for \"default:\" as empty block belong to \"case 1:\"\n"
                + "                case 1: { }\n"
                + "              }</pre>", actualEmptyBlockDetails.getDescription(), "Description"
                                                                                + " doesn't match");
        assertEquals(2, actualEmptyBlockDetails.getProperties().size(), "Number of properties "
                + "doesn't macth");
        List<String> expectedPropertiesName = Arrays.asList("option", "tokens");
        List<String> expectedPropertiesValueType = Arrays.asList("scalar", "set");
        List<String> expectedPropertiesType = Arrays.asList("BlockOption", "subsetOfTokenType");
        List<String> expectedPropertiesDefaultValue = Arrays.asList("statement", "LITERAL_WHILE,LITERAL_TRY,LITERAL_FINALLY,LITERAL_DO,"
                + "LITERAL_IF,LITERAL_ELSE,LITERAL_FOR,INSTANCE_INIT,"
                + "STATIC_INIT,LITERAL_SWITCH,LITERAL_SYNCHRONIZED");
        List<String> expectedPropertiesDescription = Arrays.asList("specify the policy on block "
                + "contents.", "tokens to check");
        List<List<String>> expectedPropertiesValues = Arrays.asList(Arrays.asList("statement",
                "text"), Arrays.asList("LITERAL_WHILE", "LITERAL_TRY", "LITERAL_CATCH",
                "LITERAL_FINALLY", "LITERAL_DO", "LITERAL_IF", "LITERAL_ELSE", "LITERAL_FOR",
                "INSTANCE_INIT", "STATIC_INIT", "LITERAL_SWITCH", "LITERAL_SYNCHRONIZED",
                "LITERAL_CASE", "LITERAL_DEFAULT", "ARRAY_INIT"));
        for (int i = 0; i < actualEmptyBlockDetails.getProperties().size(); i++) {
            assertEquals(expectedPropertiesName.get(i), actualEmptyBlockDetails.getProperties().get(i).getName(),
                    "Property: " + expectedPropertiesName.get(i) + " name doesn't macth");
            assertEquals(expectedPropertiesType.get(i), actualEmptyBlockDetails.getProperties().get(i).getType(),
                    "Property: " + expectedPropertiesName.get(i) + " type doesn't macth");
            assertEquals(expectedPropertiesDefaultValue.get(i),
                    actualEmptyBlockDetails.getProperties().get(i).getDefaultValue(),
                    "Property: " + expectedPropertiesName.get(i) + " default value doesn't macth");
            assertEquals(expectedPropertiesDescription.get(i),
                    actualEmptyBlockDetails.getProperties().get(i).getDescription(),
                    "Property: " + expectedPropertiesName.get(i) + " description doesn't macth");
            assertEquals(expectedPropertiesValues.get(i),
                    actualEmptyBlockDetails.getProperties().get(i).getValues(),
                    "Property: " + expectedPropertiesName.get(i) + " values doesn't macth");
            assertEquals(expectedPropertiesValueType.get(i),
                    actualEmptyBlockDetails.getProperties().get(i).getValueType(),
                    "Property: " + expectedPropertiesName.get(i) + " value type doesn't macth");
        }
        emptyBlockCheck.setViolationMessageKeys(Arrays.asList("block.noStatement", "block.empty"));

    }
}

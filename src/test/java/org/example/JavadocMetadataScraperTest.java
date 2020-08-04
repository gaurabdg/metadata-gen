package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

import org.junit.jupiter.api.Test;

public class JavadocMetadataScraperTest {
    private static final String METADATA_OUTPUT_PATH = System.getProperty("user.dir") + "/src"
            + "/test/resources/java/inputs/";

    @Test
    public void testCheckModuleAndEnumTokenValues() throws Exception {
        checkModuleMeta("right-curly-check/RightCurlyCheck", ModuleType.CHECK);
    }

    @Test
    public void testFileFilterModuleAndEmptyViolationMessageKeys() throws Exception {
        checkModuleMeta("before-execution-exclusion-file-filter"
                + "/BeforeExecutionExclusionFileFilter", ModuleType.FILEFILTER);
    }

    @Test
    public void testFilterModule() throws Exception {
        checkModuleMeta("suppression-xpath-single-filter/SuppressionXpathSingleFilter",
                ModuleType.FILTER);
    }

    @Test
    public void testEmptyPropretiesList() throws Exception {
        checkModuleMeta("upper-ell-check/UpperEllCheck", ModuleType.CHECK);
    }

    @Test
    public void testExampleLessModuleDescription() throws Exception {
        checkModuleMeta("upper-ell-check/ExampleLessUpperEllCheck", ModuleType.CHECK);
    }

    /**
     * This test shows how the presence of a established and predefined "marker" ("To configure
     * the check:") is required to differentiate the examples
     * section in absence of properties list e.g. PropertyLessTranslationCheck.
     *
     * https://github.com/checkstyle/metadata-gen/issues/34 will be closed when this test passes.
     *
     * @throws Exception exception
     */
    @Test
    public void testNonPropertyModuleDescription() throws Exception {
        final String moduleName = "translation-check/PropertyLessTranslationCheck";
        Main.main(getJavaFileInputPath(moduleName), METADATA_OUTPUT_PATH + "/translation-check/");
        ModuleDetails incorrectMetaDesc = new XMLMetaReader().read(new FileInputStream(
                        new File(METADATA_OUTPUT_PATH + "/" + moduleName + ".xml")),
        ModuleType.CHECK);

        ModuleDetails expectedMetaDesc = new XMLMetaReader().read(getClass().getClassLoader()
                .getResourceAsStream("java/inputs/translation-check/TranslationCheck"
                        + ".xml"),
                ModuleType.CHECK);
        assertEquals(expectedMetaDesc.getDescription(), incorrectMetaDesc.getDescription(), "This"
                + " test fails to show the diff in description");
    }

    private static String getJavaFileInputPath(String moduleLocation) {
        return System.getProperty("user.dir") + "/src/test/resources/java/inputs/" + moduleLocation + ".java";
    }

    private void checkModuleMeta(String moduleName, ModuleType moduleType) throws IOException, CheckstyleException {
        ModuleDetails expectedMeta = new XMLMetaReader().read(getClass().getClassLoader()
                .getResourceAsStream("java/inputs/" + moduleName + ".xml"), moduleType);

        Main.main(getJavaFileInputPath(moduleName),
                METADATA_OUTPUT_PATH + "/" + moduleName.substring(0, moduleName.indexOf('/')) +
                        "/temp");
        ModuleDetails actualMeta = loadMetaFromFile(METADATA_OUTPUT_PATH + "/" + moduleName.substring(0, moduleName.indexOf('/')) +
                        "/temp" + expectedMeta.getName() + ".xml", moduleType);

        assertEquals(expectedMeta.getDescription(), actualMeta.getDescription(), "Description "
                + "doesn't match: " + expectedMeta.getName());
        assertEquals(expectedMeta.getFullQualifiedName(), actualMeta.getFullQualifiedName(),
                "Fully qualified name doesn't match: " + expectedMeta.getName());
        assertEquals(expectedMeta.getParent(), actualMeta.getParent(),
                "Parent doesn't match: " + expectedMeta.getName());
        assertEquals(expectedMeta.getProperties().size(), actualMeta.getProperties().size(),
                "Properties size dont match: " + expectedMeta.getName());
        expectedMeta.getProperties().forEach(expectedProperty -> {
            ModulePropertyDetails actualProperty = actualMeta.getModulePropertyByKey(expectedProperty.getName());
            assertEquals(expectedProperty.getDescription(), actualProperty.getDescription(),
                    "Description doesnt match: " + expectedProperty.getName() + ":" + expectedMeta.getName());
            assertEquals(expectedProperty.getType(), actualProperty.getType(), "Type didnt match:"
                    + " " + expectedProperty.getName() + ":" + expectedMeta.getName());
            assertEquals(expectedProperty.getValidationType(), actualProperty.getValidationType()
                    , "Validation type doesnt match: " + expectedProperty.getName() + ":" + expectedMeta.getName());
            assertEquals(expectedProperty.getDefaultValue(), actualProperty.getDefaultValue(),
                    "Default valu doesnt match: " + expectedProperty.getName() + ":" + expectedMeta.getName());
        });
        assertEquals(expectedMeta.getViolationMessageKeys(), actualMeta.getViolationMessageKeys()
                , "Violation message keys dont match: " + expectedMeta.getName());
    }

    private static ModuleDetails loadMetaFromFile(String moduleLocation, ModuleType moduleType) throws FileNotFoundException {
        return new XMLMetaReader().read(new FileInputStream(new File(moduleLocation)), moduleType);
    }
}

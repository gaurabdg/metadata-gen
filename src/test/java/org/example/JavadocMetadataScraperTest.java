package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import org.junit.jupiter.api.Test;

public class JavadocMetadataScraperTest {

    @Test
    public void testCheckModuleAndEnumTokenValues() throws Exception {
        checkModuleMeta("RightCurlyCheck", ModuleType.CHECK);
    }

    @Test
    public void testFileFilterModuleAndEmptyViolationMessageKeys() throws Exception {
        checkModuleMeta("BeforeExecutionExclusionFileFilter", ModuleType.FILEFILTER);
    }

    @Test
    public void testFilterModule() throws Exception {
        checkModuleMeta("SuppressionXpathSingleFilter", ModuleType.FILTER);
    }

    @Test
    public void testEmptyPropretiesList() throws Exception {
        checkModuleMeta("UpperEllCheck", ModuleType.CHECK);
    }

    private static String getFileInputPath(String moduleName) {
        return System.getProperty("user.dir") + "/src/test/resources/java/example-pkg/" + moduleName + ".java";
    }

    private static String getFileOutputPath(String moduleName) {
        return System.getProperty("user.dir") + "/src/test/resources/javadoc-scraper-output";
    }

    private void checkModuleMeta(String moduleName, ModuleType moduleType) throws IOException, CheckstyleException {
        ModuleDetails expectedMeta = new XMLMetaReader().read(getClass().getClassLoader()
                .getResourceAsStream(moduleName + ".xml"), moduleType);

        Main.main(getFileInputPath(moduleName), getFileOutputPath(moduleName));
        ModuleDetails actualMeta =
                new XMLMetaReader().read(new FileInputStream(new File(getFileOutputPath(moduleName) + "/" + moduleName + ".xml")),
        moduleType);

        assertEquals(expectedMeta.getDescription(), actualMeta.getDescription(), "Description "
                + "doesn't macth: " + expectedMeta.getName());
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

}

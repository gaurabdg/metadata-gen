package org.example;

import java.io.File;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

public final class Main {
    private Main() {
    }
    public static String outputRootPath = "";

    public static void main(String... args) throws IOException, CheckstyleException {
        final Checker checker = new Checker();
        checker.setModuleClassLoader(Checker.class.getClassLoader());
        final DefaultConfiguration scraperCheckConfig =
                        new DefaultConfiguration(JavadocMetadataScraper.class.getName());
        final DefaultConfiguration defaultConfiguration = new DefaultConfiguration("configuration");
        final DefaultConfiguration treeWalkerConfig = new DefaultConfiguration(TreeWalker.class.getName());
        defaultConfiguration.addAttribute("charset", StandardCharsets.UTF_8.name());
        defaultConfiguration.addChild(treeWalkerConfig);
        treeWalkerConfig.addChild(scraperCheckConfig);
        checker.configure(defaultConfiguration);
        outputRootPath = args[1];
        createMeta(checker, args[0]);
    }

    public static void createMeta(Checker checker, String path) throws CheckstyleException,
            IOException {
        List<File> validFiles = new ArrayList<>();
        if (path.endsWith(".java")) {
            validFiles.add(new File(path));
        }
        else {
            final List<String> moduleFolders = Arrays.asList("checks", "filters", "filefilters");
            for (String folder : moduleFolders) {
                try (Stream<Path> files = Files.walk(Paths.get(path + "/" + folder))) {
                    validFiles.addAll(files.map(Path::toString)
                            .filter(fileName -> (fileName.endsWith("SuppressWarningsHolder.java")
                                    || fileName.endsWith("Check.java")
                                    || fileName.endsWith("Filter.java"))
                                    && (fileName.endsWith("AbstractClassNameCheck.java")
                                    || !fileName.substring(fileName.lastIndexOf('/') + 1).startsWith(
                                            "Abstract")))
                            .map(File::new)
                            .collect(Collectors.toList()));
                }
            }
        }

        checker.process(validFiles);
    }
}

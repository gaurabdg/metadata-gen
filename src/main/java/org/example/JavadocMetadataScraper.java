package org.example;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.api.JavadocTokenTypes;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.javadoc.AbstractJavadocCheck;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

public class JavadocMetadataScraper extends AbstractJavadocCheck {
    private static final Pattern PROPERTY_TAG = Pattern.compile("\\s*Property\\s*");
    private static final Pattern TYPE_TAG = Pattern.compile("\\s.*Type is\\s.*");
    private static final Pattern VALIDATION_TYPE_TAG
            = Pattern.compile("\\s.*Validation type is\\s.*");
    private static final Pattern DEFAULT_VALUE_TAG = Pattern.compile("\\s*Default value is:*.*");
    private static final Pattern EXAMPLES_TAG
            = Pattern.compile("\\s*To configure the (default )?check.*");
    private static final Pattern PARENT_TAG = Pattern.compile("\\s*Parent is\\s*");
    private static final Pattern VIOLATION_MESSAGES_TAG =
            Pattern.compile("\\s*Violation Message Keys:\\s*");
    private static final Pattern TOKEN_TEXT_PATTERN = Pattern.compile("([A-Z]+_*)+[A-Z]+");
    private static final Pattern DESC_CLEAN = Pattern.compile("-", Pattern.LITERAL);
    private static final Pattern PACKAGE_NAME_CONVERSION_CLEAN_UP = Pattern.compile("[\\/]");
    private static final Pattern PATTERN_QUOTES_REMOVAL = Pattern.compile("\"");

    private ModuleDetails moduleDetails;
    private ScrapeStatus currentStatus;
    private boolean toScan;
    private String descriptionText;
    private DetailNode rootNode;
    private int propertySectionStartIdx;
    private int exampleSectionStartIdx;
    private int parentSectionStartIdx;

    @Override
    public int[] getDefaultJavadocTokens() {
        return new int[]{
                JavadocTokenTypes.JAVADOC,
                JavadocTokenTypes.PARAGRAPH,
                JavadocTokenTypes.LI,
                JavadocTokenTypes.SINCE_LITERAL
        };
    }

    @Override
    public int[] getRequiredJavadocTokens() {
        return getAcceptableJavadocTokens();
    }

    @Override
    public void beginJavadocTree(DetailNode ast) {
        if (isTopLevelClassJavadoc()) {
            moduleDetails = new ModuleDetails();
            currentStatus = ScrapeStatus.DESCRIPTION;
            toScan = false;
            descriptionText = "";
            propertySectionStartIdx = -1;
            exampleSectionStartIdx = -1;
            parentSectionStartIdx = -1;

            final String filePath = getFileContents().getFileName();
            String moduleName = getModuleSimpleName();
            if (moduleName.contains("Check")) {
                moduleName = moduleName.substring(0, moduleName.indexOf("Check"));
            }
            moduleDetails.setName(moduleName);
            moduleDetails.setFullQualifiedName(getPackageName(filePath));
            moduleDetails.setModuleType(getModuleType(moduleName));
        }
    }

    @Override
    public void visitJavadocToken(DetailNode ast) {
        if (toScan) {
            scrapeContent(ast);
        }

        if (ast.getType() == JavadocTokenTypes.JAVADOC
            && getParent(getBlockCommentAst()).getType() == TokenTypes.CLASS_DEF) {
            rootNode = ast;
            toScan = true;
        }
        else if (ast.getType() == JavadocTokenTypes.SINCE_LITERAL) {
            toScan = false;
        }
    }

    @Override
    public void finishJavadocTree(DetailNode rootAst) {
        moduleDetails.setDescription(getDescriptionText());
         if (isTopLevelClassJavadoc()) {
             try {
                 new XMLMetaWriter().write(moduleDetails);
             } catch (IOException ignored) {}
         }
    }

    /**
     * Method containing the core logic of scraping. This keeps track and decides which phase of
     * scraping we are in, and accordingly call other subroutines.
     *
     * @param ast javadoc ast
     */
    public void scrapeContent(DetailNode ast) {
        if (ast.getType() == JavadocTokenTypes.PARAGRAPH) {
            if (getParentText(ast) != null) {
                currentStatus = ScrapeStatus.PARENT;
                parentSectionStartIdx = getParentIndexOf(ast);
                moduleDetails.setParent(getParentText(ast));
            }
            else if (isViolationMessagesText(ast)) {
                currentStatus = ScrapeStatus.VIOLATION_MESSAGES;
            }
            else if (isExamplesText(ast)) {
                if (exampleSectionStartIdx == -1) {
                    exampleSectionStartIdx = getParentIndexOf(ast);
                }
            }
        }
        else if (ast.getType() == JavadocTokenTypes.LI){
            if (isPropertyList(ast)) {
                currentStatus = ScrapeStatus.PROPERTY;

                if (propertySectionStartIdx == -1) {
                    propertySectionStartIdx = getParentIndexOf(ast);
                }

                moduleDetails.setDescription(descriptionText);
                moduleDetails.addToProperties(createProperties(ast));
            }
            else if (currentStatus == ScrapeStatus.VIOLATION_MESSAGES) {
                moduleDetails.addToViolationMessages(getViolationMessages(ast));
            }
        }
    }

    /**
     * Create the modulePropertyDetails content
     *
     * @param nodeLi list item javadoc node
     * @return modulePropertyDetail object for the corresponding property
     */
    private static ModulePropertyDetails createProperties(DetailNode nodeLi) {
        ModulePropertyDetails modulePropertyDetails = new ModulePropertyDetails();
        DetailNode propertyNameTag
                = getFirstChildOfType(nodeLi, JavadocTokenTypes.JAVADOC_INLINE_TAG, 0);
        DetailNode propertyType = getFirstChildOfMatchingText(nodeLi, TYPE_TAG);

        String propertyDesc = DESC_CLEAN.matcher(constructSubTreeText(nodeLi, propertyNameTag.getIndex() + 1,
                propertyType.getIndex() - 1)).replaceAll(Matcher.quoteReplacement(""));

        modulePropertyDetails.setDescription(propertyDesc.trim());
        modulePropertyDetails.setName(getTextFromTag(propertyNameTag));
        modulePropertyDetails.setType(
                getTagTextFromProperty(nodeLi,
                getFirstChildOfMatchingText(nodeLi, TYPE_TAG)));
        final DetailNode validationTypeNode = getFirstChildOfMatchingText(nodeLi,
                VALIDATION_TYPE_TAG);
        if (validationTypeNode != null) {
            modulePropertyDetails.setValidationType(getTagTextFromProperty(nodeLi,
                    validationTypeNode));
        }
        final String defaultValue = getPropertyDefaultText(nodeLi);
        if (!"null".equals(defaultValue)
            && !"the charset property of the parent".equals(defaultValue)) {
            modulePropertyDetails.setDefaultValue(defaultValue);
        }
        return modulePropertyDetails;
    }

    /**
     * Get tag text from property data.
     *
     * @param nodeLi javadoc li item node
     * @param propertyMeta property javadoc node
     * @return property metadata text
     */
    private static String getTagTextFromProperty(DetailNode nodeLi, DetailNode propertyMeta) {
        DetailNode tagNode = getFirstChildOfType(nodeLi,
                JavadocTokenTypes.JAVADOC_INLINE_TAG, propertyMeta.getIndex() + 1);
        return getTextFromTag(tagNode);
    }

    /**
     * Clean up the default token text by removing hyperlinks, and only keeping token type text.
     *
     * @param initialText unclean text
     * @return clean text
     */
    private static String cleanDefaultTokensText(String initialText) {
        Set<String> tokens = new LinkedHashSet<>();
        Matcher matcher = TOKEN_TEXT_PATTERN.matcher(initialText);
        while (matcher.find()) {
            tokens.add(matcher.group(0));
        }
        return String.join(",", tokens);
    }

    /**
     * Performs a DFS of the subtree with a node as the root and constructs the text of that
     * tree, ignoring JavadocToken texts.
     *
     * @param node root node of subtree
     * @param childLeftLimit the left index of root children from where to scan
     * @param childRightLimit the right index of root children till where to scan
     * @return constructed text of subtree
     */
    private static String constructSubTreeText(DetailNode node, int childLeftLimit, int childRightLimit){
        StringBuilder result = new StringBuilder();
        DetailNode detailNode = node;
        DetailNode treeRootNode = node;
        Set<DetailNode> visited = new HashSet<>();

        Deque<DetailNode> stack = new ArrayDeque<>();
        stack.addFirst(detailNode);
        while (!stack.isEmpty()) {
            detailNode = stack.getFirst();
            stack.removeFirst();

            if (!visited.contains(detailNode)) {
                final String childText = detailNode.getText();
                if (detailNode.getType() != JavadocTokenTypes.LEADING_ASTERISK
                        && !TOKEN_TEXT_PATTERN.matcher(childText).matches()) {
                    result.insert(0, detailNode.getText());
                }
                visited.add(detailNode);
            }

            for (DetailNode child : detailNode.getChildren()) {
                if (child.getParent().equals(treeRootNode)
                        && (child.getIndex() < childLeftLimit
                        || child.getIndex() > childRightLimit)) {
                    continue;
                }
                if (!visited.contains(child)) {
                        stack.addFirst(child);
                }
            }
        }
        return result.toString().trim();
    }

    /**
     * Create the description text with starting index as 0 and ending index would be the first
     * valid non zero index amongst in the order of {@code propertySectionStartIdx},
     * {@code exampleSectionStartIdx} and {@code parentSectionStartIdx}.
     *
     * @return description text
     */
    private String getDescriptionText() {
        int descriptionEndIdx;
        if (propertySectionStartIdx != -1) {
            descriptionEndIdx = propertySectionStartIdx;
        }
        else if (exampleSectionStartIdx != -1) {
            descriptionEndIdx = exampleSectionStartIdx;
        }
        else {
            descriptionEndIdx = parentSectionStartIdx;
        }
        return constructSubTreeText(rootNode, 0, descriptionEndIdx - 1);
    }

    /**
     * Create property default text, which is either normal property value or list of tokens
     *
     * @param nodeLi list item javadoc node
     * @return default property text
     */
    private static String getPropertyDefaultText(DetailNode nodeLi) {
        String result;
        DetailNode defaultValueNode = getFirstChildOfMatchingText(nodeLi, DEFAULT_VALUE_TAG);
        DetailNode propertyDefaultValueTag = getFirstChildOfType(nodeLi,
                JavadocTokenTypes.JAVADOC_INLINE_TAG, defaultValueNode.getIndex() + 1);
        if (propertyDefaultValueTag == null) {
            final String tokenText = constructSubTreeText(nodeLi,
                    defaultValueNode.getIndex(), nodeLi.getChildren().length);
            result = cleanDefaultTokensText(tokenText);
        }
        else {
            result = getTextFromTag(propertyDefaultValueTag);
        }
        return result;
    }

    /**
     * Get the violation message text for a specific key from the list item
     *
     * @param nodeLi list item javadoc node
     * @return violation message key text
     */
    private static String getViolationMessages(DetailNode nodeLi) {
        return getTextFromTag(
                getFirstChildOfType(nodeLi, JavadocTokenTypes.JAVADOC_INLINE_TAG, 0));
    }

    /**
     * Get text from {@code JavadocTokenTypes.JAVADOC_INLINE_TAG}.
     *
     * @param nodeTag target javadoc tag
     * @return text contained by the tag
     */
    private static String getTextFromTag(DetailNode nodeTag) {
        String result = "";
        if (nodeTag != null) {
            result = PATTERN_QUOTES_REMOVAL.matcher(
                    getFirstChildOfType(nodeTag, JavadocTokenTypes.TEXT, 0)
                            .getText().trim()).replaceAll("");
        }
        return result;
    }

    /**
     * Returns the first child node which matches the provided {@code TokenType} and has the
     * children index after the offset value.
     *
     * @param node parent node
     * @param tokenType token type to match
     * @param offset children array index offset
     * @return the first child satisfying the conditions
     */
    private static DetailNode getFirstChildOfType(DetailNode node, int tokenType, int offset){
        return Arrays.stream(node.getChildren())
                .filter(child -> child.getIndex() >= offset && child.getType() == tokenType)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get first child of parent node matching the provided pattern.
     *
     * @param node parent node
     * @param pattern pattern to match against
     * @return the first child node matching the condition
     */
    private static DetailNode getFirstChildOfMatchingText(DetailNode node, Pattern pattern) {
        return Arrays.stream(node.getChildren())
                .filter(child -> pattern.matcher(child.getText()).matches())
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns parent node, removing modifier/annotation nodes.
     *
     * @param commentBlock child node.
     * @return parent node.
     */
    private static DetailAST getParent (DetailAST commentBlock){
        final DetailAST parentNode = commentBlock.getParent();
        DetailAST result = null;
        if (parentNode != null) {
            result = parentNode;
            if (result.getType() == TokenTypes.ANNOTATION) {
                result = parentNode.getParent().getParent();
            }
            else if (result.getType() == TokenTypes.MODIFIERS) {
                result = parentNode.getParent();
            }
        }
        return result;
    }

    /**
     * Traverse parents until we reach the root node (@code{JavadocTokenTypes.JAVADOC})
     * child and return its index.
     *
     * @param node subtree child node
     * @return root node child index
     */
    private static int getParentIndexOf(DetailNode node) {
        DetailNode currNode = node;
        while (currNode.getParent().getIndex() != -1) {
            currNode = currNode.getParent();
        }
        return currNode.getIndex();
    }

    /**
     * Get module parent text from paragaraph javadoc node
     *
     * @param nodeParagraph paragraph javadoc node
     * @return parent text
     */
    private static String getParentText(DetailNode nodeParagraph) {
        String result = null;
        if (PARENT_TAG.matcher(
                getFirstChildOfType(nodeParagraph, JavadocTokenTypes.TEXT, 0)
                        .getText()).matches()) {
            result = getTextFromTag(
                    getFirstChildOfType(nodeParagraph, JavadocTokenTypes.JAVADOC_INLINE_TAG, 0));
        }
        return result;
    }

    /**
     * Get module type(check/filter/filefilter) based on file name
     *
     * @param fileName file name
     * @return module type
     */
    private ModuleType getModuleType(String fileName) {
        final String simpleModuleName = getModuleSimpleName();
        ModuleType result = null;
        if (simpleModuleName.endsWith("FileFilter")) {
            result = ModuleType.FILEFILTER;
        }
        else if (simpleModuleName.endsWith("Filter")) {
            result = ModuleType.FILTER;
        }
        else {
            result = ModuleType.CHECK;
        }
        return result;
    }

    /**
     * Extract simple file name from the whole file path name
     * @return simple module name
     */
    private String getModuleSimpleName() {
        final String fullFileName = getFileContents().getFileName();
        final String[] pathTokens = PACKAGE_NAME_CONVERSION_CLEAN_UP.split(fullFileName);
        final String fileName = pathTokens[pathTokens.length - 1];
        return fileName.substring(0, fileName.length() - 5);
    }

    /**
     * Retrieve package name of module from the absolute file path.
     *
     * @param filePath absolute file path
     * @return package name
     */
    private String getPackageName(String filePath) {
        Deque<String> result = new ArrayDeque<>();
        String[] filePathTokens = PACKAGE_NAME_CONVERSION_CLEAN_UP.split(filePath);
        for (int i = filePathTokens.length - 1; i >=0; i--) {
            if ("java".equals(filePathTokens[i]) || "resources".equals(filePathTokens[i])) {
                break;
            }
            result.addFirst(filePathTokens[i]);
        }
        String fileName = result.removeLast();
        result.addLast(fileName.substring(0, fileName.length() - 5));
        return String.join(".", result);
    }

    /**
     * Check if the current javadoc block comment AST corresponds to the top-level class as we
     * only want to scrape top-level class javadoc
     *
     * @return true if the current AST corresponds to top level class
     */
    public boolean isTopLevelClassJavadoc() {
        final DetailAST parent = getParent(getBlockCommentAst());
        final Optional<DetailAST> className = TokenUtil.findFirstTokenByPredicate(parent,
                child -> parent.getType() == TokenTypes.CLASS_DEF
                            && child.getType() == TokenTypes.IDENT);
        return className.isPresent()
                && getModuleSimpleName().equals(className.get().getText());
    }

    /**
     * Checks whether the paragraph node corresponds to the example section.
     *
     * @param ast javadoc paragraph node
     * @return true if the section matches the example section marker
     */
    private static boolean isExamplesText(DetailNode ast) {
        return EXAMPLES_TAG.matcher(
                getFirstChildOfType(ast, JavadocTokenTypes.TEXT, 0)
                        .getText()).matches();
    }

    /**
     * Checks whether the list item node is part of a property list.
     *
     * @param nodeLi {@code JavadocTokenType.LI} node
     * @return true if the node is part of a property list
     */
    private static boolean isPropertyList(DetailNode nodeLi){
        final DetailNode firstTextChildToken = getFirstChildOfType(nodeLi, JavadocTokenTypes.TEXT
                , 0);
        return firstTextChildToken != null
                && PROPERTY_TAG.matcher(firstTextChildToken.getText()).matches();
    }

    /**
     * Checks whether the {@code JavadocTokenType.PARAGRAPH} node is referring to the violation
     * message keys javadoc segment.
     *
     * @param nodePararaph paragraph javadoc node
     * @return true if paragraph node contains the violation message keys text
     */
    private static boolean isViolationMessagesText(DetailNode nodePararaph) {
        return VIOLATION_MESSAGES_TAG.matcher(
                getFirstChildOfType(nodePararaph, JavadocTokenTypes.TEXT, 0)
                        .getText()).matches();
    }

    /**
     * Status used to keep track in which phase(for e.g. creating description/violation message
     * keys) of scraping we are in, while processing the tokens.
     */
    private enum ScrapeStatus {
        DESCRIPTION,
        PROPERTY,
        PARENT,
        VIOLATION_MESSAGES
    }
}

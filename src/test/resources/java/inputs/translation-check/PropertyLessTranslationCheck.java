////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2020 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.checks;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.puppycrawl.tools.checkstyle.Definitions;
import com.puppycrawl.tools.checkstyle.GlobalStatefulCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.LocalizedMessage;
import com.puppycrawl.tools.checkstyle.api.MessageDispatcher;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Ensures the correct translation of code by checking property files for consistency
 * regarding their keys. Two property files describing one and the same context
 * are consistent if they contain the same keys. TranslationCheck also can check
 * an existence of required translations which must exist in project, if
 * {@code requiredTranslations} option is used.
 * </p>
 * <p>
 * Consider the following properties file in the same directory:
 * </p>
 * <pre>
 * #messages.properties
 * hello=Hello
 * cancel=Cancel
 *
 * #messages_de.properties
 * hell=Hallo
 * ok=OK
 * </pre>
 * <p>
 * The Translation check will find the typo in the German {@code hello} key,
 * the missing {@code ok} key in the default resource file and the missing
 * {@code cancel} key in the German resource file:
 * </p>
 * <pre>
 * messages_de.properties: Key 'hello' missing.
 * messages_de.properties: Key 'cancel' missing.
 * messages.properties: Key 'hell' missing.
 * messages.properties: Key 'ok' missing.
 * </pre>
 * <p>
 * Language code for the property {@code requiredTranslations} is composed of
 * the lowercase, two-letter codes as defined by
 * <a href="https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes">ISO 639-1</a>.
 * Default value is empty String Set which means that only the existence of default
 * translation is checked. Note, if you specify language codes (or just one
 * language code) of required translations the check will also check for existence
 * of default translation files in project.
 * </p>
 * <p>
 * Attention: the check will perform the validation of ISO codes if the option
 * is used. So, if you specify, for example, "mm" for language code,
 * TranslationCheck will rise violation that the language code is incorrect.
 * </p>
 * <p>
 * Attention: this Check could produce false-positives if it is used with
 * <a href="https://checkstyle.org/config.html#Checker">Checker</a> that use cache
 * (property "cacheFile") This is known design problem, will be addressed at
 * <a href="https://github.com/checkstyle/checkstyle/issues/3539">issue</a>.
 * </p>
 * <p>
 * To configure the check to check only files which have '.properties' and
 * '.translations' extensions:
 * </p>
 * <pre>
 * &lt;module name="Translation"&gt;
 *   &lt;property name="fileExtensions" value="properties, translations"/&gt;
 * &lt;/module&gt;
 * </pre>
 * <p>
 * Note, that files with the same path and base name but which have different
 * extensions will be considered as files that belong to different resource bundles.
 * </p>
 * <p>
 * An example of how to configure the check to validate only bundles which base
 * names start with "ButtonLabels":
 * </p>
 * <pre>
 * &lt;module name="Translation"&gt;
 *   &lt;property name="baseName" value="^ButtonLabels.*$"/&gt;
 * &lt;/module&gt;
 * </pre>
 * <p>
 * To configure the check to check existence of Japanese and French translations:
 * </p>
 * <pre>
 * &lt;module name="Translation"&gt;
 *   &lt;property name="requiredTranslations" value="ja, fr"/&gt;
 * &lt;/module&gt;
 * </pre>
 * <p>
 * The following example shows how the check works if there is a message bundle
 * which element name contains language code, county code, platform name.
 * Consider that we have the below configuration:
 * </p>
 * <pre>
 * &lt;module name="Translation"&gt;
 *   &lt;property name="requiredTranslations" value="es, fr, de"/&gt;
 * &lt;/module&gt;
 * </pre>
 * <p>
 * As we can see from the configuration, the TranslationCheck was configured
 * to check an existence of 'es', 'fr' and 'de' translations. Lets assume that
 * we have the resource bundle:
 * </p>
 * <pre>
 * messages_home.properties
 * messages_home_es_US.properties
 * messages_home_fr_CA_UNIX.properties
 * </pre>
 * <p>
 * Than the check will rise the following violation: "0: Properties file
 * 'messages_home_de.properties' is missing."
 * </p>
 * <p>
 * Parent is {@code com.puppycrawl.tools.checkstyle.Checker}
 * </p>
 * <p>
 * Violation Message Keys:
 * </p>
 * <ul>
 * <li>
 * {@code translation.missingKey}
 * </li>
 * <li>
 * {@code translation.missingTranslationFile}
 * </li>
 * </ul>
 *
 * @since 3.0
 */
@GlobalStatefulCheck
public class PropertyLessTranslationCheck extends AbstractFileSetCheck {

}

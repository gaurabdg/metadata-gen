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

package com.puppycrawl.tools.checkstyle.checks.blocks;

import java.util.Locale;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * <p>
 * Checks for empty blocks. This check does not validate sequential blocks.
 * </p>
 * <p>
 * Sequential blocks won't be checked. Also, no violations for fallthrough:
 * </p>
 * <pre>
 * switch (a) {
 *   case 1:                          // no violation
 *   case 2:                          // no violation
 *   case 3: someMethod(); { }        // no violation
 *   default: break;
 * }
 * </pre>
 * <p>
 * This check processes LITERAL_CASE and LITERAL_DEFAULT separately.
 * So, if tokens=LITERAL_DEFAULT, following code will not trigger any violation,
 * as the empty block belongs to LITERAL_CASE:
 * </p>
 * <p>
 * Configuration:
 * </p>
 * <pre>
 * &lt;module name=&quot;EmptyBlock&quot;&gt;
 *   &lt;property name=&quot;tokens&quot; value=&quot;LITERAL_DEFAULT&quot;/&gt;
 * &lt;/module&gt;
 * </pre>
 * <p>
 * Result:
 * </p>
 * <pre>
 * switch (a) {
 *   default:        // no violation for "default:" as empty block belong to "case 1:"
 *   case 1: { }
 * }
 * </pre>
 * <ul>
 * <li>
 * Property {@code option} - specify the policy on block contents.
 * Type is {@code com.puppycrawl.tools.checkstyle.checks.blocks.BlockOption}.
 * Default value is {@code statement}.
 * </li>
 * <li>
 * Property {@code tokens} - tokens to check
 * Type is {@code java.lang.String[]}.
 * Validation type is {@code tokenSet}.
 * Default value is:
 * <a href="https://checkstyle.org/apidocs/com/puppycrawl/tools/checkstyle/api/TokenTypes.html#LITERAL_WHILE">
 * LITERAL_WHILE</a>,
 * <a href="https://checkstyle.org/apidocs/com/puppycrawl/tools/checkstyle/api/TokenTypes.html#LITERAL_TRY">
 * LITERAL_TRY</a>,
 * <a href="https://checkstyle.org/apidocs/com/puppycrawl/tools/checkstyle/api/TokenTypes.html#LITERAL_FINALLY">
 * LITERAL_FINALLY</a>,
 * <a href="https://checkstyle.org/apidocs/com/puppycrawl/tools/checkstyle/api/TokenTypes.html#LITERAL_DO">
 * LITERAL_DO</a>,
 * <a href="https://checkstyle.org/apidocs/com/puppycrawl/tools/checkstyle/api/TokenTypes.html#LITERAL_IF">
 * LITERAL_IF</a>,
 * <a href="https://checkstyle.org/apidocs/com/puppycrawl/tools/checkstyle/api/TokenTypes.html#LITERAL_ELSE">
 * LITERAL_ELSE</a>,
 * <a href="https://checkstyle.org/apidocs/com/puppycrawl/tools/checkstyle/api/TokenTypes.html#LITERAL_FOR">
 * LITERAL_FOR</a>,
 * <a href="https://checkstyle.org/apidocs/com/puppycrawl/tools/checkstyle/api/TokenTypes.html#INSTANCE_INIT">
 * INSTANCE_INIT</a>,
 * <a href="https://checkstyle.org/apidocs/com/puppycrawl/tools/checkstyle/api/TokenTypes.html#STATIC_INIT">
 * STATIC_INIT</a>,
 * <a href="https://checkstyle.org/apidocs/com/puppycrawl/tools/checkstyle/api/TokenTypes.html#LITERAL_SWITCH">
 * LITERAL_SWITCH</a>,
 * <a href="https://checkstyle.org/apidocs/com/puppycrawl/tools/checkstyle/api/TokenTypes.html#LITERAL_SYNCHRONIZED">
 * LITERAL_SYNCHRONIZED</a>.
 * </li>
 * </ul>
 * <p>
 * To configure the check:
 * </p>
 * <pre>
 * &lt;module name="EmptyBlock"/&gt;
 * </pre>
 * <p>
 * To configure the check for the {@code text} policy and only {@code try} blocks:
 * </p>
 * <pre>
 * &lt;module name=&quot;EmptyBlock&quot;&gt;
 *   &lt;property name=&quot;option&quot; value=&quot;text&quot;/&gt;
 *   &lt;property name=&quot;tokens&quot; value=&quot;LITERAL_TRY&quot;/&gt;
 * &lt;/module&gt;
 * </pre>
 * <p>
 * Parent is {@code com.puppycrawl.tools.checkstyle.TreeWalker}
 * </p>
 * <p>
 * Violation Message Keys:
 * </p>
 * <ul>
 * <li>
 * {@code block.empty}
 * </li>
 * <li>
 * {@code block.noStatement}
 * </li>
 * </ul>
 *
 * @since 3.0
 */
@StatelessCheck
public class EmptyBlockCheck
    extends AbstractCheck {
}

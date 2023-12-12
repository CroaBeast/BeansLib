package me.croabeast.beanslib.misc;

import org.intellij.lang.annotations.Language;

/**
 * This annotation indicates that a String parameter, field, or method return value
 * should be a valid regular expression.
 *
 * <p> The annotation can be used to specify the syntax and flags of the regular expression,
 * as well as an example of a matching and a non-matching input.
 *
 * @see java.util.regex.Pattern
 * @see org.intellij.lang.annotations.Language
 */
@Language("RegExp")
public @interface Regex {}

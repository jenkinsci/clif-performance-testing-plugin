/*
 * CLIF is a Load Injection Framework
 * Copyright (C) 2026 Orange SA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Contact: clif@ow2.org
 */
package org.ow2.clif.jenkins.utils;

public class StringUtils {

    private StringUtils() {
    }

    /**
     * Count the number of character occurrences in a string
     *
     * @param value the source string where to look for character occurrences
     * @param character the character to look for
     * @return the number of occurrences of the given character in the given string value, or 0 if value is null
     */
    public static int countMatches(String value, int character) {
        return value == null
                ? 0
                : (int) value.chars().filter(c -> c == character).count();
    }

    /**
     * Checks if a string ends with a given string, in a case-insensitive way
     * @param value the full string
     * @param suffix the terminating substring to check, case-insensitive
     * @return true if value is non-null and contains suffix, false otherwise.
     * Special cases:
     * <ul>
     *     <li>True when suffix is the empty string and value is non-null</li>
     *     <li>False when suffix is null</li>
     * </ul>
     */
    public static boolean endsWithIgnoreCase(String value, String suffix) {
        return value != null && suffix != null && value.toLowerCase().endsWith(suffix.toLowerCase());
    }

    /**
     * Checks a string is null or empty, trimming whitespaces
     * @return true if the given string is null or empty, regardless of possible whitespaces, false otherwise
     */
    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Checks a string is not null and not empty, trimming whitespaces
     * @return true if the given string is not null and not empty, regardless of possible whitespaces, false otherwise
     */
    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }
}

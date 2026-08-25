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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.ow2.clif.jenkins.utils.StringUtils.countMatches;
import static org.ow2.clif.jenkins.utils.StringUtils.endsWithIgnoreCase;
import static org.ow2.clif.jenkins.utils.StringUtils.isBlank;
import static org.ow2.clif.jenkins.utils.StringUtils.isNotBlank;

class StringUtilsTest {

    @Test
    void isBlankChecks() {
        assertTrue(isBlank(null), "isBlankWithNullIsTrue");
        assertTrue(isBlank(""), "isBlankWithEmptyIsTrue");
        assertTrue(isBlank("  "), "isBlankWithWhitespacesIsTrue");
        assertFalse(isBlank("c"), "isBlankWithOneCharIsFalse");
        assertFalse(isBlank(" c "), "isBlankWithOneCharAndWhitespacesIsFalse");
    }

    @Test
    void isNotBlankChecks() {
        assertFalse(isNotBlank(null), "isNotBlankWithNullIsFalse");
        assertFalse(isNotBlank(""), "isNotBlankWithEmptyIsFalse");
        assertFalse(isNotBlank("  "), "isNotBlankWithWhitespacesIsFalse");
        assertTrue(isNotBlank("c"), "isNotBlankWithOneCharIsTrue");
        assertTrue(isNotBlank(" c "), "isNotBlankWithOneCharAndWhitespacesIsTrue");
    }

    @Test
    void countMatchesChecks() {
        assertEquals(0, countMatches(null, 0), "countMatchesInNullIs0");
        assertEquals(0, countMatches("ABC", 'c'), "countMatchesOf_c_In_ABC_Is0");
        assertEquals(1, countMatches("ABC", 'C'), "countMatchesOf_C_In_ABC_Is1");
        assertEquals(2, countMatches("CABC", 'C'), "countMatchesOf_C_In_CABC_Is2");
    }

    @Test
    void endsWithIgnoreCaseChecks() {
        assertFalse(endsWithIgnoreCase(null, null), "endsWithIgnoreCaseForNullNullIsFalse");
        assertFalse(endsWithIgnoreCase(null, ""), "endsWithIgnoreCaseForNullEmptyIsFalse");
        assertFalse(endsWithIgnoreCase("", null), "endsWithIgnoreCaseForEmptyNullIsFalse");
        assertTrue(endsWithIgnoreCase("", ""), "endsWithIgnoreCaseForEmptyEmptyIsTrue");
        assertFalse(endsWithIgnoreCase(null, "A"), "endsWithIgnoreCaseForNullWhateverIsFalse");
        assertFalse(endsWithIgnoreCase("", "A"), "endsWithIgnoreCaseForEmptyWhateverIsFalse");
        assertFalse(endsWithIgnoreCase("A", null), "endsWithIgnoreCaseForWhateverNullIsFalse");
        assertTrue(endsWithIgnoreCase("A", ""), "endsWithIgnoreCaseForWhateverEmptyIsTrue");
        assertTrue(endsWithIgnoreCase("AB", "B"), "endsWithIgnoreCaseFor_AB_B_isTrue");
        assertTrue(endsWithIgnoreCase("AB", "b"), "endsWithIgnoreCaseFor_AB_b_isTrue");
        assertFalse(endsWithIgnoreCase("AB", "A"), "endsWithIgnoreCaseFor_AB_A_isFalse");
    }
}

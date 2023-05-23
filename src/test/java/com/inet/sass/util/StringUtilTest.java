/*
 * Copyright 2023 i-net software
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.inet.sass.util;

import org.junit.Assert;
import org.junit.Test;

import com.inet.sass.util.StringUtil;

public class StringUtilTest {

    @Test
    public void testContainsVariable() {
        String sentence = "$var1 var2";
        String word = "var";
        Assert.assertFalse(StringUtil.containsVariable(sentence, word));

        word = "var1";
        Assert.assertTrue(StringUtil.containsVariable(sentence, word));

        String var2 = "var2";
        Assert.assertFalse(StringUtil.containsVariable(sentence, var2));
    }

    @Test
    public void testContainsVariableWithDash() {
        String sentence = "$var- var2";
        String word = "var";
        Assert.assertFalse(StringUtil.containsVariable(sentence, word));
    }

    @Test
    public void testReplaceVariable() {
        String sentence = "$var1 var2";
        String word = "var";
        String value = "abc";
        Assert.assertEquals(sentence,
                StringUtil.replaceVariable(sentence, word, value));

        word = "var1";
        Assert.assertEquals("abc var2",
                StringUtil.replaceVariable(sentence, word, value));

        String var2 = "var2";
        Assert.assertEquals(sentence,
                StringUtil.replaceVariable(sentence, var2, value));
    }

    @Test
    public void testReplaceVariableWithDash() {
        String sentence = "$var- var2";
        String word = "var";
        String value = "abc";
        Assert.assertEquals(sentence,
                StringUtil.replaceVariable(sentence, word, value));
    }

    @Test
    public void testContainsSubString() {
        String sentence = "var1 var2";
        String word = "var";
        Assert.assertFalse(StringUtil.containsSubString(sentence, word));

        word = "var1";
        Assert.assertTrue(StringUtil.containsSubString(sentence, word));

        String var2 = "var2";
        Assert.assertTrue(StringUtil.containsSubString(sentence, var2));

        Assert.assertTrue(StringUtil.containsSubString(".error.intrusion",
                ".error"));

        Assert.assertFalse(StringUtil.containsSubString(".foo", "oo"));
    }

    @Test
    public void testContainsSubStringWithDash() {
        String sentence = "var- var2";
        String word = "var";
        Assert.assertFalse(StringUtil.containsSubString(sentence, word));
    }

    @Test
    public void testReplaceSubString() {
        String sentence = "var1 var2";
        String word = "var";
        String value = "abc";

        word = "var1";
        Assert.assertEquals("abc var2",
                StringUtil.replaceSubString(sentence, word, value));

        String var2 = "var1 abc";
        Assert.assertEquals(sentence,
                StringUtil.replaceSubString(sentence, var2, value));

        Assert.assertEquals(".foo",
                StringUtil.replaceSubString(".foo", "oo", "aa"));
    }

    @Test
    public void testReplaceSubStringWithDash() {
        String sentence = "var- var2";
        String word = "var";
        String value = "abc";
        Assert.assertEquals(sentence,
                StringUtil.replaceSubString(sentence, word, value));
    }

    @Test
    public void testRemoveDuplicatedClassSelector() {
        Assert.assertEquals(".seriousError", StringUtil
                .removeDuplicatedSubString(".seriousError.seriousError", "."));
    }
}

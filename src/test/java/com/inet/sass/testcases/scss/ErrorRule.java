/*
 * Copyright 2023 i-net software
 * Copyright 2000-2014 Vaadin Ltd.
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

package com.inet.sass.testcases.scss;

import org.junit.Assert;
import org.junit.Test;

import com.inet.sass.AbstractTestBase;
import com.inet.sass.ScssStylesheet;

public class ErrorRule extends AbstractTestBase {

    @Test
    public void testCompiler() throws Exception {
        ScssStylesheet sheet;
        try {
            sheet = getStyleSheet("/scss/error-rule.scss");
            sheet.compile();
            Assert.fail();
        } catch (AssertionError ex) {
            ex.printStackTrace();
            Assert.assertEquals( "Property top must be either left or right.", ex.getMessage() );
        }
    }
}

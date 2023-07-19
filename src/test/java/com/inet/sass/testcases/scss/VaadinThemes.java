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
package com.inet.sass.testcases.scss;

import org.junit.Assert;
import org.junit.Test;

import com.inet.sass.AbstractTestBase;

public class VaadinThemes extends AbstractTestBase {
    String scssFolder = "/vaadin-themes/scss";
    String cssFolder = "/vaadin-themes/css";

    @Test
    public void compileValo() throws Exception {
        String scss = scssFolder + "/valo/styles.scss";
        String css = cssFolder + "/valo/styles.css";
        testCompiler(scss, css);
        Assert.assertEquals("Original CSS and parsed CSS doesn't match",
                comparisonCss, parsedScss);
    }

    @Test
    public void compileReindeer() throws Exception {
        String scss = scssFolder + "/reindeer/styles.scss";
        String css = cssFolder + "/reindeer/styles.css";
        testCompiler(scss, css);
        Assert.assertEquals("Original CSS and parsed CSS doesn't match",
                comparisonCss, parsedScss);
    }
}

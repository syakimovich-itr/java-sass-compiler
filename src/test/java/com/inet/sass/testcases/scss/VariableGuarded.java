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

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

import com.inet.sass.AbstractTestBase;
import com.inet.sass.ScssStylesheet;

public class VariableGuarded extends AbstractTestBase {
    String scss = "/scss/var-guarded.scss";
    String css = "/css/var-guarded.css";

    @Test
    public void testParser() throws  IOException, URISyntaxException {
        ScssStylesheet root = getStyleSheet(scss);
        Assert.assertEquals(4, root.getChildren().size());
    }

    @Test
    public void testCompiler() throws Exception {
        testCompiler(scss, css);
        Assert.assertEquals("Original CSS and parsed CSS doesn't match",
                comparisonCss, parsedScss);
    }
}

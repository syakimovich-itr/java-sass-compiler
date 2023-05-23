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

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.css.sac.CSSException;

import com.inet.sass.AbstractTestBase;
import com.inet.sass.ScssStylesheet;
import com.inet.sass.handler.SCSSDocumentHandler;
import com.inet.sass.parser.Parser;
import com.inet.sass.resolver.FilesystemResolver;
import com.inet.sass.tree.ImportNode;

public class CompassImports extends AbstractTestBase {

    String scssOtherDirectory = "/scss/compass-test/compass-import.scss";
    String scssSameDirectory = "/scss/compass-test2/compass-import2.scss";
    String css = "/css/compass-import.css";

    String compassPath = "/scss/compass-test2";

    @Test
    public void testParser() throws CSSException, IOException {
        Parser parser = new Parser();
        SCSSDocumentHandler handler = new SCSSDocumentHandler();
        parser.setDocumentHandler(handler);
        parser.parseStyleSheet(getClass().getResource(scssOtherDirectory)
                .getPath());
        ScssStylesheet root = handler.getStyleSheet();
        ImportNode importVariableNode = (ImportNode) root.getChildren().get(0);
        Assert.assertEquals("compass", importVariableNode.getUri());
        Assert.assertFalse(importVariableNode.isPureCssImport());
    }

    @Test
    public void testCompiler() throws Exception {
        testCompiler(scssSameDirectory, css, null);
    }

    @Test
    public void testCompilerWithCustomPath() throws Exception {
        File rootPath = new File(getClass().getResource(compassPath).toURI());

        testCompiler(scssOtherDirectory, css, rootPath.getPath());
    }

    public void testCompiler(String scss, String css, String additionalPath)
            throws Exception {
        comparisonCss = getFileContent(css);
        comparisonCss = comparisonCss.replaceAll(CR, "");
        File file = getFile(scss);
        FilesystemResolver resolver = new FilesystemResolver(additionalPath);
        ScssStylesheet sheet = ScssStylesheet.get( file.getAbsolutePath(), null, new SCSSDocumentHandler(), new AssertErrorHandler(), resolver );
        Assert.assertNotNull(sheet);

        sheet.compile();
        parsedScss = sheet.printState();
        parsedScss = parsedScss.replaceAll(CR, "");
        Assert.assertEquals("Original CSS and parsed CSS do not match",
                comparisonCss, parsedScss);
    }
}

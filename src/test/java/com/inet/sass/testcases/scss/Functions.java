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

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

import com.inet.sass.AbstractTestBase;
import com.inet.sass.ScssStylesheet;
import com.inet.sass.tree.BlockNode;

public class Functions extends AbstractTestBase {

    String scss = "/scss/functions.scss";
    String css  = "/css/functions.css";

    @Test
    public void testParser() throws IOException, URISyntaxException {
        ScssStylesheet root = getStyleSheet( scss );
        Assert.assertEquals( 6, root.getChildren().size() );
        BlockNode blockNode = (BlockNode)root.getChildren().get( 5 );
        Assert.assertEquals( 18, blockNode.getChildren().size() );
    }

    @Test
    public void testCompiler() throws Exception {
        testCompiler( scss, css );
    }
}

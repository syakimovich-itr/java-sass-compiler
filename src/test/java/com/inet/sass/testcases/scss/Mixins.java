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
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.SCSSLexicalUnit;
import com.inet.sass.tree.BlockNode;
import com.inet.sass.tree.MixinDefNode;
import com.inet.sass.tree.MixinNode;

public class Mixins extends AbstractTestBase {

    String scss = "/scss/mixins.scss";
    String css  = "/css/mixins.css";

    @Test
    public void testParser() throws URISyntaxException, IOException {
        ScssStylesheet root = getStyleSheet( scss );

        MixinDefNode mixinDefNode0 = (MixinDefNode)root.getChildren().get( 0 );
        Assert.assertEquals( "font-settings", mixinDefNode0.getName() );
        Assert.assertTrue( mixinDefNode0.getArglist().isEmpty() );
        Assert.assertEquals( 3, mixinDefNode0.getChildren().size() );

        MixinDefNode mixinDefNode1 = (MixinDefNode)root.getChildren().get( 1 );
        Assert.assertEquals( "rounded-borders", mixinDefNode1.getName() );
        Assert.assertEquals( 2, mixinDefNode1.getArglist().size() );
        Assert.assertEquals( "thickness", mixinDefNode1.getArglist().get( 0 ).getName() );
        Assert.assertEquals( "radius", mixinDefNode1.getArglist().get( 1 ).getName() );
        Assert.assertEquals( SCSSLexicalUnit.SAC_PIXEL, mixinDefNode1.getArglist().get( 1 ).getExpr().getContainedValue().getLexicalUnitType() );
        Assert.assertEquals( 3f, mixinDefNode1.getArglist().get( 1 ).getExpr().getContainedValue().getDoubleValue(), 0f );

        Assert.assertEquals( 4, mixinDefNode1.getChildren().size() );

        BlockNode mainBlockNode = (BlockNode)root.getChildren().get( 4 );
        Assert.assertEquals( 3, mainBlockNode.getChildren().size() );
        MixinNode mixinNode0MainBlock = (MixinNode)mainBlockNode.getChildren().get( 0 );
        Assert.assertEquals( "rounded-borders", mixinNode0MainBlock.getName() );
        Assert.assertEquals( "mixinVar", mixinNode0MainBlock.getArglist().get( 0 ).getContainedValue().getStringValue() );
        Assert.assertEquals( LexicalUnitImpl.SCSS_VARIABLE, mixinNode0MainBlock.getArglist().get( 0 ).getContainedValue().getLexicalUnitType() );
        MixinNode mixinNOde1MainBlock = (MixinNode)mainBlockNode.getChildren().get( 1 );
        Assert.assertEquals( "font-settings", mixinNOde1MainBlock.getName() );
        Assert.assertTrue( mixinNOde1MainBlock.getArglist().size() == 0 );

        MixinNode mixinNOde2MainBlock = (MixinNode)mainBlockNode.getChildren().get( 2 );
        Assert.assertEquals( "main-details", mixinNOde2MainBlock.getName() );
        Assert.assertTrue( mixinNOde1MainBlock.getArglist().size() == 0 );

        MixinNode mixinNode1MainBlock = (MixinNode)mainBlockNode.getChildren().get( 1 );
        Assert.assertTrue( mixinNode1MainBlock.getArglist().size() == 0 );

        BlockNode footerBlockNode = (BlockNode)root.getChildren().get( 4 );
        MixinNode mixinNodeFooterBlock = (MixinNode)footerBlockNode.getChildren().get( 0 );
        Assert.assertEquals( "mixinVar", mixinNodeFooterBlock.getArglist().get( 0 ).getContainedValue().getStringValue() );

        Assert.assertTrue( root.getChildren().get( 0 ) instanceof MixinDefNode );
        Assert.assertTrue( root.getChildren().get( 1 ) instanceof MixinDefNode );
        Assert.assertTrue( root.getChildren().get( 3 ) instanceof MixinDefNode );
        Assert.assertTrue( root.getChildren().get( 6 ) instanceof MixinDefNode );
        Assert.assertTrue( root.getChildren().get( 7 ) instanceof MixinDefNode );
        Assert.assertTrue( root.getChildren().get( 10 ) instanceof MixinNode );
    }

    @Test
    public void testCompiler() throws Exception {
        testCompiler( scss, css );
    }

}

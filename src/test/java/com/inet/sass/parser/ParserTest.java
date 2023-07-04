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

package com.inet.sass.parser;

import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.css.sac.InputSource;

import com.inet.sass.ScssStylesheet;
import com.inet.sass.resolver.ScssStylesheetResolver;
import com.inet.sass.testcases.scss.AssertErrorHandler;

public class ParserTest {

    @Test
    public void testCanIngoreSingleLineComment() throws Exception {
        ScssStylesheetResolver resolver = new ScssStylesheetResolver() {
            @Override
            public InputSource resolve( ScssStylesheet parentStylesheet, String identifier ) {
                return new InputSource( new StringReader( "//kjaljsföajsfalkj\n@12abcg;" ) );
            }
        };
        ScssStylesheet stylesheet = ScssStylesheet.get( "", new AssertErrorHandler(), resolver );
        stylesheet.compile();
        Assert.assertEquals( "@12abcg;", stylesheet.printState() );
    }
}

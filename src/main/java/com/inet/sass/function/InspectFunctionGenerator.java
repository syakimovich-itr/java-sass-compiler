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
package com.inet.sass.function;

import com.inet.sass.ScssContext;
import com.inet.sass.parser.FormalArgumentList;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.SassListItem;

class InspectFunctionGenerator extends AbstractFunctionGenerator {

    InspectFunctionGenerator() {
        super( createArgumentList( new String[] { "value" }, false ), "inspect" );
    }

    @Override
    protected SassListItem computeForArgumentList( ScssContext context, LexicalUnitImpl function, FormalArgumentList arglist ) {
        SassListItem param = getParam( arglist, 0 );
        String result;
        if (LexicalUnitImpl.checkLexicalUnitType(param, LexicalUnitImpl.SCSS_NULL)) {
            result = "null";
//        } else if( param instanceof SassList ) {
        } else {
            result = param.printState();
        }
        return LexicalUnitImpl.createRawIdent( param.getUri(), param.getLineNumber(), param.getColumnNumber(), result );
    }
}

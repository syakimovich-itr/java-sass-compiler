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
package com.inet.sass.function;

import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.ParseException;

class UnitlessFunctionGenerator extends
        AbstractSingleParameterFunctionGenerator {

    private static String[] argumentNames = { "number" };

    UnitlessFunctionGenerator() {
        super(createArgumentList(argumentNames, false), "unitless");
    }

    @Override
    protected LexicalUnitImpl computeForParam( LexicalUnitImpl function, LexicalUnitImpl firstParam ) {
        if( !firstParam.isNumber() ) {
            throw new ParseException( "The parameter of " + function.getFunctionName() + "() must be a number", firstParam );
        }
        Boolean value = "".equals( firstParam.getDimensionUnitText() );
        LexicalUnitImpl result = LexicalUnitImpl.createIdent( firstParam.getUri(), firstParam.getLineNumber(), firstParam.getColumnNumber(), value.toString() );
        return result;
    }
}
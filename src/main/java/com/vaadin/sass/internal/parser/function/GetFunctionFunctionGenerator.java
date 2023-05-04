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
package com.vaadin.sass.internal.parser.function;

import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;

/**
 * The SASS function "get-function(x)".
 */
public class GetFunctionFunctionGenerator extends AbstractSingleParameterFunctionGenerator {

    public GetFunctionFunctionGenerator() {
        super( createArgumentList( new String[]{ "name" }, false ), "get-function" );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected LexicalUnitImpl computeForParam( LexicalUnitImpl function, LexicalUnitImpl firstParam ) {
        if( !LexicalUnitImpl.checkLexicalUnitType( firstParam, LexicalUnitImpl.SAC_IDENT, LexicalUnitImpl.SAC_STRING_VALUE ) ) {
            throw new ParseException( "The parameter of get-function() must be a string", firstParam );
        }
        String fname = firstParam.unquotedString();
        return LexicalUnitImpl.createGetFunction( firstParam.getUri(), firstParam.getLineNumber(), firstParam.getColumnNumber(), fname );
    }
}

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
package com.vaadin.sass.internal.function;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.parser.FormalArgumentList;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassListItem;

class VariableExistsFunctionGenerator extends AbstractFunctionGenerator {

    VariableExistsFunctionGenerator() {
        super( createArgumentList( new String[] { "name" }, false ), "variable-exists" );
    }

    @Override
    protected SassListItem computeForArgumentList( ScssContext context, LexicalUnitImpl function, FormalArgumentList actualArguments ) {
        SassListItem param = getParam( actualArguments, 0 );
        if( !LexicalUnitImpl.checkLexicalUnitType( param, LexicalUnitImpl.SAC_IDENT, LexicalUnitImpl.SAC_STRING_VALUE ) ) {
            throw new ParseException( "The parameter of variable-exists() must be a string", param );
        }
        String name = param.unquotedString();
        Boolean exists = context.getVariable( name ) != null;

        LexicalUnitImpl result = LexicalUnitImpl.createIdent( param.getUri(), param.getLineNumber(), param.getColumnNumber(), exists.toString() );
        return result;
    }
}

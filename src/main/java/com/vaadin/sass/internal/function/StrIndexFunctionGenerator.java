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
import com.vaadin.sass.internal.parser.SassListItem;

class StrIndexFunctionGenerator extends AbstractFunctionGenerator {

    StrIndexFunctionGenerator() {
        super( createArgumentList( new String[] { "string", "substring" }, false ), "str-index" );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SassListItem computeForArgumentList( ScssContext context, LexicalUnitImpl function, FormalArgumentList actualArguments ) {
        String str = getParam( actualArguments, "string" ).unquotedString();
        String substring = getParam( actualArguments, "substring" ).unquotedString();
        int index = str.indexOf( substring );
        if( index < 0 ) {
            return LexicalUnitImpl.createNull( function.getUri(), function.getLineNumber(), function.getColumnNumber() );
        }
        return LexicalUnitImpl.createInteger( function.getUri(), function.getLineNumber(), function.getColumnNumber(), index + 1 );
    }
}

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

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.parser.ActualArgumentList;
import com.vaadin.sass.internal.parser.ArgumentList;
import com.vaadin.sass.internal.parser.FormalArgumentList;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassListItem;

/**
 * The SASS function "call(x,args)".
 */
public class CallFunctionGenerator extends AbstractFunctionGenerator {

    public CallFunctionGenerator() {
        super( createArgumentList( new String[] { "function", "args" }, true ), "call" );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SassListItem computeForArgumentList( ScssContext context, LexicalUnitImpl function, FormalArgumentList actualArguments ) {
        SassListItem first = getParam( actualArguments, 0 );
        ArgumentList args = (ArgumentList)getParam( actualArguments, 1 );

        if( !(first instanceof LexicalUnitImpl) || ((LexicalUnitImpl)first).getLexicalUnitType() != LexicalUnitImpl.SCSS_GET_FUNCTION ) {
            throw new ParseException( "The first parameter of call() must be result of get-function()", function );
        }
        LexicalUnitImpl funct = (LexicalUnitImpl)first;
        funct = LexicalUnitImpl.createFunction( funct.getUri(), funct.getLineNumber(), funct.getColumnNumber(), funct.getFunctionName(), new ActualArgumentList( args, null ) );
        return funct.evaluateFunctionsAndExpressions( context, true );
    }
}

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
import com.inet.sass.parser.ParseException;
import com.inet.sass.parser.SassListItem;

class StrSliceFunctionGenerator extends AbstractFunctionGenerator {

    StrSliceFunctionGenerator() {
        super( createArgumentList( new String[] { "string", "start-at", "end-at" }, false ), "str-slice" );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkForUnsetParameters() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SassListItem computeForArgumentList( ScssContext context, LexicalUnitImpl function, FormalArgumentList actualArguments ) {
        SassListItem strParam = getParam( actualArguments, "string" );
        if( strParam == null ) {
            throw new ParseException( "Error in parameter 'string' of function " + function.getFunctionName(), function );
        }
        SassListItem startParam = getParam( actualArguments, "start-at" );
        if( !(strParam instanceof LexicalUnitImpl) ) {
            throw new ParseException( "Error in parameter 'start-at' of function " + function.getFunctionName(), function );
        }
        SassListItem endParam = getParam( actualArguments, "end-at" );

        String str = strParam.unquotedString();
        int length = str.length();

        int start = ((LexicalUnitImpl)startParam).getIntegerValue() - 1;
        if( start < 0 ) {
            start = length + start + 1;
        }
        start = Math.min( start, length );

        int end;
        if( endParam instanceof LexicalUnitImpl ) {
            end = ((LexicalUnitImpl)endParam).getIntegerValue();
        } else {
            end = length;
        }
        if( end < 0 ) {
            end = length + end + 1;
        }
        end = Math.min( Math.max( start, end ), length );

        return LexicalUnitImpl.createString( str.substring( start, end ) );
    }
}

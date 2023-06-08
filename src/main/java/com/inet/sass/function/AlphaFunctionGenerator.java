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

import com.inet.sass.ScssContext;
import com.inet.sass.parser.ActualArgumentList;
import com.inet.sass.parser.FormalArgumentList;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.ParseException;
import com.inet.sass.parser.SassListItem;
import com.inet.sass.util.ColorUtil;

class AlphaFunctionGenerator extends AbstractFunctionGenerator {

    private static String[] argumentNames = { "color" };

    AlphaFunctionGenerator() {
        super( createArgumentList( argumentNames, false ), "alpha", "opacity" );
    }

    @Override
    protected SassListItem computeForArgumentList( ScssContext context, LexicalUnitImpl function, FormalArgumentList actualArguments ) {
        LexicalUnitImpl color = (LexicalUnitImpl)getParam( actualArguments, "color" );

        if( color.isNumber() ) {
            return function; // css filter function
        }

        float opacity;
        if( ColorUtil.isRgba( color ) || ColorUtil.isHsla( color ) ) {
            ActualArgumentList parameterList = color.getParameterList();
            SassListItem last = parameterList.get( parameterList.size() - 1 );
            opacity = ((LexicalUnitImpl)last).getFloatValue();
        } else if( ColorUtil.isColor( color ) ) {
            opacity = 1.0f;
        } else {
            throw new ParseException( "The function " + function.getFunctionName() + " requires a color as its first parameter: " + color, function );
        }
        return LexicalUnitImpl.createNumber( function.getUri(), function.getLineNumber(), function.getColumnNumber(), opacity );
    }
}

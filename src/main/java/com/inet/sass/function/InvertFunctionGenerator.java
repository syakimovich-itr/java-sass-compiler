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
import com.inet.sass.util.ColorUtil;

class InvertFunctionGenerator extends AbstractFunctionGenerator {

    InvertFunctionGenerator() {
        super( createArgumentList( new String[] { "color", "weight" }, false ), "invert" );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SassListItem computeForArgumentList( ScssContext context, LexicalUnitImpl function, FormalArgumentList actualArguments ) {
        LexicalUnitImpl color = (LexicalUnitImpl)getParam( actualArguments, "color" );

        if( color == null ) {
            throw new ParseException( "Missing argument $color. for function 'invert' ", function );
        }
        if( color.isNumber() ) {
            return function; // css function
        }

        SassListItem weightParam = getParam( actualArguments, "weight" );
        double weight = 1;
        if( weightParam != null ) {
            switch( weightParam.getItemType() ) {
                case LexicalUnitImpl.SAC_PERCENTAGE:
                    weight = ((LexicalUnitImpl)weightParam).getDoubleValue() / 100;
                    break;
                case LexicalUnitImpl.SAC_INTEGER:
                case LexicalUnitImpl.SAC_REAL:
                    weight = ((LexicalUnitImpl)weightParam).getDoubleValue();
                    break;
            }
        }

        int[] rgb = ColorUtil.colorToRgb( color );
        if( rgb == null ) {
            throw new ParseException( "The function 'invert' requires a color as its first parameter: " + color, function );
        }
        int[] newRgb = new int[3];
        for( int i = 0; i < 3; i++ ) {
            int val = rgb[i];
            newRgb[i] = (int)Math.round( val * (1-weight) + (255 - val) * weight );
        }
        return ColorUtil.createHexColor( function.getUri(), function.getLineNumber(), function.getColumnNumber(), newRgb );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkForUnsetParameters() {
        return false;
    }
}

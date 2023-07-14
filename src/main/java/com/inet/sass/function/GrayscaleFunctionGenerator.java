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
import com.inet.sass.util.ColorUtil;

class GrayscaleFunctionGenerator extends
        AbstractSingleParameterFunctionGenerator {

    private static String[] argumentNames = { "color" };

    GrayscaleFunctionGenerator() {
        super(createArgumentList(argumentNames, false), "grayscale");
    }

    @Override
    protected LexicalUnitImpl computeForParam( LexicalUnitImpl function, LexicalUnitImpl firstParam ) {
        if( ColorUtil.isColor( firstParam ) || ColorUtil.isRgba( firstParam ) || ColorUtil.isHsla( firstParam ) ) {
            double[] hsl = ColorUtil.colorToHsl( firstParam );
            hsl[1] = 0;
            double alpha = ColorUtil.getAlpha( firstParam );
            return ColorUtil.createHslaOrHslColor( hsl, alpha, firstParam.getLineNumber(), firstParam.getColumnNumber() );
        }
        if( firstParam.isNumber() ) {
            return function; // css filter function
        }
        throw new ParseException( "The argument of grayscale() must be a valid color or number", firstParam );
    }
}
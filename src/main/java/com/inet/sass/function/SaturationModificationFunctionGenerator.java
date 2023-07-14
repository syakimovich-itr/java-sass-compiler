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
import com.inet.sass.parser.FormalArgumentList;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.ParseException;
import com.inet.sass.parser.SassListItem;
import com.inet.sass.util.ColorUtil;

class SaturationModificationFunctionGenerator extends
        AbstractFunctionGenerator {

    private static String[] argumentNames = { "color", "amount" };

    SaturationModificationFunctionGenerator() {
        super(createArgumentList(argumentNames, false), "saturate",
                "desaturate");
    }

    @Override
    public SassListItem computeForArgumentList(ScssContext context,
            LexicalUnitImpl function, FormalArgumentList actualArguments) {
        LexicalUnitImpl color = checkAndGetColor(function, actualArguments);
        double amount = checkAndGetAmount(function, actualArguments);
        double alpha = ColorUtil.getAlpha(color);
        double[] hsl = ColorUtil.colorToHsl(color);
        if ("saturate".equals(function.getFunctionName())) {
            hsl[1] += amount;
        } else {
            hsl[1] -= amount;
        }
        hsl[1] = Math.max(0, Math.min(100, hsl[1]));
        return ColorUtil.createHslaOrHslColor(hsl, alpha,
                function.getLineNumber(), function.getColumnNumber());
    }

    private LexicalUnitImpl checkAndGetColor(LexicalUnitImpl function,
            FormalArgumentList actualArguments) {
        SassListItem colorItem = getParam(actualArguments, "color");
        if (!(colorItem instanceof LexicalUnitImpl)) {
            throw new ParseException("The first argument of "
                    + function.getFunctionName() + "() must be a valid color",
                    function);
        }
        LexicalUnitImpl color = (LexicalUnitImpl) colorItem;
        if (!ColorUtil.isColor(color) && !ColorUtil.isRgba(color)
                && !ColorUtil.isHsla(color)) {
            throw new ParseException("The first argument of "
                    + function.getFunctionName() + "() must be a valid color",
                    function);
        }
        return color;
    }

    private double checkAndGetAmount(LexicalUnitImpl function,
            FormalArgumentList actualArguments) {
        SassListItem amountItem = getParam(actualArguments, "amount");
        if (!LexicalUnitImpl.checkLexicalUnitType(amountItem,
                LexicalUnitImpl.SAC_PERCENTAGE)) {
            throw new ParseException("The amount argument of "
                    + function.getFunctionName()
                    + "() must be a percentage value", function);
        }
        double amount = amountItem.getContainedValue().getDoubleValue();
        if (amount < 0 || amount > 100) {
            throw new ParseException("The amount argument of "
                    + function.getFunctionName()
                    + "() must be a percentage value between 0% and 100%",
                    function);
        }
        return amount;
    }
}
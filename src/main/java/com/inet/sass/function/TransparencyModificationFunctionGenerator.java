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

class TransparencyModificationFunctionGenerator extends
        AbstractFunctionGenerator {

    private static String[] argumentNames = { "color", "amount" };

    TransparencyModificationFunctionGenerator() {
        super(createArgumentList(argumentNames, false), "transparentize",
                "fade-out", "opacify", "fade-in");
    }

    @Override
    protected SassListItem computeForArgumentList(ScssContext context,
            LexicalUnitImpl function, FormalArgumentList actualArguments) {
        checkParameters(function, actualArguments);
        double factor = 1.0; // for opacify/fade-in
        if ("fade-out".equals(function.getFunctionName())
                || "transparentize".equals(function.getFunctionName())) {
            factor = -1.0;
        }
        double amount = getParam(actualArguments, "amount").getContainedValue()
                .getDoubleValue();
        LexicalUnitImpl color = (LexicalUnitImpl) getParam(actualArguments,
                "color");
        double opacity = 1.0;
        boolean rgba = ColorUtil.isRgba(color);
        boolean hsla = ColorUtil.isHsla(color);
        boolean hsl = ColorUtil.isHslColor(color);
        if (rgba || hsla) {
            ActualArgumentList colorComponents = color.getParameterList();
            int lastIndex = colorComponents.size() - 1;
            opacity = getDouble(colorComponents, lastIndex);
        }
        opacity += factor * amount;
        opacity = Math.min(1, Math.max(0, opacity));
        if (hsl || hsla) {
            return ColorUtil.createHslaOrHslColor(ColorUtil.colorToHsl(color),
                    opacity, function.getLineNumber(),
                    function.getColumnNumber());
        } else {
            return ColorUtil.createRgbaOrHexColor(ColorUtil.colorToRgb(color),
                    opacity, function.getLineNumber(),
                    function.getColumnNumber());
        }
    }

    private void checkParameters(LexicalUnitImpl function,
            FormalArgumentList args) {

        SassListItem color = getParam(args, 0);
        if (!(color instanceof LexicalUnitImpl)
                || (!ColorUtil.isColor(color.getContainedValue())
                        && !ColorUtil.isRgba(color.getContainedValue()) && !ColorUtil
                            .isHsla(color.getContainedValue()))) {
            throw new ParseException("The function "
                    + function.getFunctionName()
                    + " requires a valid color as its first parameter", function);
        }
        SassListItem amountItem = getParam(args, 1);
        if (!(amountItem instanceof LexicalUnitImpl)
                || !LexicalUnitImpl.checkLexicalUnitType(amountItem,
                        LexicalUnitImpl.SAC_INTEGER, LexicalUnitImpl.SAC_REAL)) {
            throw new ParseException("The function "
                    + function.getFunctionName()
                    + " requires a number as its second parameter", function);
        }
        double amount = amountItem.getContainedValue().getDoubleValue();
        if (amount < 0.0 || amount > 1.0) {
            throw new ParseException(
                    "The function "
                            + function.getFunctionName()
                            + " requires a number in the range [0, 1] as its second parameter",
                    function);
        }
    }

    private double getDouble(ActualArgumentList params, int i) {
        return params.get(i).getContainedValue().getDoubleValue();
    }
}

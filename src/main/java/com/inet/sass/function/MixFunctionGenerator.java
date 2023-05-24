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

class MixFunctionGenerator extends AbstractFunctionGenerator {

    private static String[] argumentNames = { "color1", "color2", "weight" };
    private static SassListItem[] defaultValues = { null, null, LexicalUnitImpl.createPercentage( null, 0, 0, 50 ) };

    MixFunctionGenerator() {
        super(createArgumentList(argumentNames, defaultValues, false), "mix");
    }

    @Override
    protected SassListItem computeForArgumentList(ScssContext context,
            LexicalUnitImpl function, FormalArgumentList actualArguments) {
        LexicalUnitImpl color1 = checkAndGetColor(actualArguments, "color1",
                function);
        LexicalUnitImpl color2 = checkAndGetColor(actualArguments, "color2",
                function);
        float weight = getParam(actualArguments, "weight").getContainedValue()
                .getFloatValue();
        float alpha1 = ColorUtil.getAlpha(color1);
        float alpha2 = ColorUtil.getAlpha(color2);
        float p = weight / 100;
        float w = p * 2 - 1;
        float a = alpha1 - alpha2;
        boolean transparent = (w * a == -1);
        if (transparent) {
            // p is 0 or 1 here, return one of the input colors depending on the
            // value of p
            return p <= 0.5 ? color2 : color1;
        }
        float w1 = ((w + a) / (1 + w * a) + 1) / 2.0f;
        float w2 = 1 - w1;
        int[] rgb1 = ColorUtil.colorToRgb(color1);
        int[] rgb2 = ColorUtil.colorToRgb(color2);
        int[] result = new int[3];
        for (int i = 0; i < 3; i++) {
            result[i] = Math.round(w1 * rgb1[i] + w2 * rgb2[i]);
        }
        float alpha = alpha1 * p + alpha2 * (1 - p);
        return ColorUtil.createRgbaOrHexColor(result, alpha,
                function.getLineNumber(), function.getColumnNumber());
    }

    private LexicalUnitImpl checkAndGetColor(
            FormalArgumentList actualArguments, String argName,
            LexicalUnitImpl function) {
        SassListItem colorItem = getParam(actualArguments, argName);
        if (!(colorItem instanceof LexicalUnitImpl)) {
            throw new ParseException(
                    "The color arguments of mix() must be valid colors. Actual argument: "
                            + colorItem.toString(), function);
        }
        LexicalUnitImpl color = (LexicalUnitImpl) colorItem;
        if (!ColorUtil.isColor(color) && !ColorUtil.isRgba(color)
                && !ColorUtil.isHsla(color)) {
            throw new ParseException(
                    "The color arguments of mix() must be valid colors. Actual argument: "
                            + colorItem.toString(), function);
        }
        return color;
    }
}
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

import static com.inet.sass.parser.SCSSLexicalUnit.SAC_CENTIMETER;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_EM;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_EX;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_INCH;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_MILLIMETER;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_PICA;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_PIXEL;
import static com.inet.sass.parser.SCSSLexicalUnit.SAC_POINT;

import java.util.ArrayList;
import java.util.List;

import com.inet.sass.ScssContext;
import com.inet.sass.parser.ActualArgumentList;
import com.inet.sass.parser.ArgumentList;
import com.inet.sass.parser.FormalArgumentList;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.ParseException;
import com.inet.sass.parser.SCSSLexicalUnit;
import com.inet.sass.parser.SassList;
import com.inet.sass.parser.SassListItem;

class RectFunctionGenerator extends AbstractFunctionGenerator {

    private static String[] argumentNames = { "value" }; // uses varargs, so
                                                         // this name is not
                                                         // actually used

    RectFunctionGenerator() {
        super(createArgumentList(argumentNames, true), "rect");
    }

    @Override
    protected SassListItem computeForArgumentList(ScssContext context,
            LexicalUnitImpl function, FormalArgumentList actualArguments) {

        ArgumentList actualParams = (ArgumentList) getParam(actualArguments, 0);
        List<SassListItem> resultParams = new ArrayList<SassListItem>();

        for (int i = 0; i < actualParams.size(); i++) {
            boolean paramOk = true;
            SassListItem item = actualParams.get(i);
            if (!(item instanceof LexicalUnitImpl)) {
                throw new ParseException(
                        "Only simple values are allowed as rect() parameters: "
                                + actualParams);
            }
            LexicalUnitImpl lui = (LexicalUnitImpl) item;
            if (lui.getItemType() == SCSSLexicalUnit.SAC_INTEGER) {
                if (lui.getIntegerValue() != 0) {
                    paramOk = false;
                }
            } else if (lui.getItemType() == SCSSLexicalUnit.SAC_IDENT) {
                if (!"auto".equals(lui.getStringValue())) {
                    paramOk = false;
                }
            } else if (!LexicalUnitImpl.checkLexicalUnitType(lui, SAC_EM,
                    SAC_EX, SAC_PIXEL, SAC_CENTIMETER, SAC_MILLIMETER,
                    SAC_INCH, SAC_POINT, SAC_PICA)) {
                paramOk = false;
            }
            if (!paramOk) {
                throw new ParseException(
                        "The following value is not accepted as a parameter for rect(): "
                                + item);
            }
            resultParams.add(item);
        }
        ActualArgumentList params = new ActualArgumentList(
                SassList.Separator.COMMA, resultParams);
        if( params.size() == 4 ) {
            return LexicalUnitImpl.createRect( function.getUri(), function.getLineNumber(), function.getColumnNumber(), params );
        } else {
            return LexicalUnitImpl.createFunction( function.getUri(), function.getLineNumber(), function.getColumnNumber(), "rect", params );
        }

    }
}

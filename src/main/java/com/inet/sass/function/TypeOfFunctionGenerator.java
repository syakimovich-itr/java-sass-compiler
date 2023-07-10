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
import com.inet.sass.parser.SassList;
import com.inet.sass.parser.SassListItem;
import com.inet.sass.parser.SassList.Separator;
import com.inet.sass.util.ColorUtil;

class TypeOfFunctionGenerator extends AbstractFunctionGenerator {

    private static String[] argumentNames = { "value" };

    TypeOfFunctionGenerator() {
        super(createArgumentList(argumentNames, false), "type-of");
    }

    @Override
    protected SassListItem computeForArgumentList(ScssContext context,
            LexicalUnitImpl function, FormalArgumentList actualArguments) {
        SassListItem param = getParam(actualArguments, 0);
        String type = "string";

        if (param instanceof SassList) {
            type = "list";
            SassList list = (SassList)param;
            if( list.size() > 0 ) {
                // we need only to check the first entry, the other has the parser already checked
                SassListItem first = list.get( 0 );
                if( first.getClass() == SassList.class && ((SassList)first).getSeparator() == Separator.COLON ) {
                    type = "map";
                }
            }
        } else if (param instanceof LexicalUnitImpl) {
            LexicalUnitImpl unit = (LexicalUnitImpl) param;
            if (unit.getLexicalUnitType() == LexicalUnitImpl.SCSS_NULL) {
                type = "null";
            } else if (isNumber(unit)) {
                type = "number";
            } else if (isBoolean(unit)) {
                type = "bool";
            } else if (unit.getLexicalUnitType() == LexicalUnitImpl.SAC_RGBCOLOR) {
                type = "color";
            } else if( ColorUtil.isColorName( unit ) ) {
                type = "color";
            } else if( ColorUtil.isHexColor( unit ) ) {
                type = "color";
            } else if (unit.getLexicalUnitType() == LexicalUnitImpl.SAC_FUNCTION) {
                if ("rgb".equals(unit.getFunctionName())
                        || "rgba".equals(unit.getFunctionName())
                        || "hsl".equals(unit.getFunctionName())
                        || "hsla".equals(unit.getFunctionName())) {
                    type = "color";
                }
            }
        }

        return createIdent(function, type);
    }

    private boolean isBoolean(LexicalUnitImpl unit) {
        if (unit.getLexicalUnitType() != LexicalUnitImpl.SAC_IDENT) {
            return false;
        }
        return "true".equals(unit.getStringValue())
                || "false".equals(unit.getStringValue());
    }

    private boolean isNumber(LexicalUnitImpl unit) {
        return unit.isNumber();
    }

    private LexicalUnitImpl createIdent(LexicalUnitImpl function,
            String paramType) {
        return LexicalUnitImpl.createRawIdent( function.getUri(), function.getLineNumber(), function.getColumnNumber(), paramType );
    }

}

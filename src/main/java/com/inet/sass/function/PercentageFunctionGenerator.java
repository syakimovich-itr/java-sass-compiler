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

/**
 * PercentageFunctionGenerator is used for converting values into percentages
 * with rounding.
 * 
 * @author Vaadin
 */
class PercentageFunctionGenerator extends
        AbstractSingleParameterFunctionGenerator {

    private static final long PERC_PRECISION_FACTOR = 100 * LexicalUnitImpl.PRECISION;

    PercentageFunctionGenerator() {
        super(createArgumentList( new String[]{ "value" }, false), "percentage");
    }

    @Override
    protected LexicalUnitImpl computeForParam( LexicalUnitImpl function, LexicalUnitImpl firstParam ) {
        float value = firstParam.getFloatValue();
        value *= PERC_PRECISION_FACTOR;
        int intValue = Math.round( value );
        value = ((float)intValue) / LexicalUnitImpl.PRECISION;

        return LexicalUnitImpl.createPercentage( firstParam.getUri(), firstParam.getLineNumber(), firstParam.getColumnNumber(), value );
    }
}
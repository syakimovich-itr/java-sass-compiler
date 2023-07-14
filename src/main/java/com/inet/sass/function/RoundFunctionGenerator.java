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

class RoundFunctionGenerator extends
        AbstractSingleParameterFunctionGenerator {

    private static String[] argumentNames = { "value" };

    RoundFunctionGenerator() {
        super(createArgumentList(argumentNames, false), "round");
    }

    @Override
    protected LexicalUnitImpl computeForParam( LexicalUnitImpl function, LexicalUnitImpl param ) {
        // duplicate the behavior of sass-lang implementation, as Math.round()
        // behaves differently for negative halves
        double value = param.getDoubleValue();
        return param.copyWithValue( Math.signum( value ) * Math.round( Math.abs( value ) ) );
    }
}

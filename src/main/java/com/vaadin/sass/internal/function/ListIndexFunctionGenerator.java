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
package com.vaadin.sass.internal.function;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.parser.FormalArgumentList;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.SassList;
import com.vaadin.sass.internal.parser.SassListItem;

class ListIndexFunctionGenerator extends AbstractFunctionGenerator {

    private static String[] argumentNames = { "list", "value" };

    ListIndexFunctionGenerator() {
        super(createArgumentList(argumentNames, false), "index");
    }

    @Override
    protected SassListItem computeForArgumentList(ScssContext context,
            LexicalUnitImpl function, FormalArgumentList actualArguments) {
        SassListItem listItem = getParam(actualArguments, "list");
        SassListItem item = getParam(actualArguments, "value");
        int index = -1;
        if (!(listItem instanceof SassList)) {
            index = listItem.equals(item) ? 1 : -1;
        } else {
            SassList list = (SassList) listItem;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equals(item)) {
                    index = i + 1;
                    break;
                }
            }
        }
        if( index == -1 ) {
            return LexicalUnitImpl.createIdent( function.getUri(), function.getLineNumber(), function.getColumnNumber(), "false" );
        } else {
            return LexicalUnitImpl.createInteger( function.getUri(), function.getLineNumber(), function.getColumnNumber(), index );
        }
    }
}
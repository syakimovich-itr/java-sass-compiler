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

import com.inet.sass.parser.FormalArgumentList;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.ParseException;
import com.inet.sass.parser.SassList;
import com.inet.sass.parser.SassListItem;
import com.inet.sass.parser.SassList.Separator;

abstract class ListFunctionGenerator extends AbstractFunctionGenerator {

    ListFunctionGenerator(FormalArgumentList formalArguments,
            String... functionNames) {
        super(formalArguments, functionNames);
    }

    protected SassList.Separator getAutoSeparator(SassList firstList,
            SassList secondList) {
        if (firstList.size() > 1) {
            return firstList.getSeparator();
        } else if (secondList.size() > 1) {
            return secondList.getSeparator();
        } else {
            return SassList.Separator.SPACE;
        }
    }

    protected SassList.Separator getAutoSeparator(SassList firstList) {
        if (firstList.size() > 1) {
            return firstList.getSeparator();
        } else {
            return SassList.Separator.SPACE;
        }
    }

    protected Separator getSeparator(SassListItem separatorItem) {
        if (!(separatorItem instanceof LexicalUnitImpl)) {
            throw new ParseException(
                    "The separator of "
                            + getFunctionNames()[0]
                            + "() must be one of 'auto', 'comma', 'space'. Actual value: "
                            + separatorItem);
        }
        String sepString = ((LexicalUnitImpl) separatorItem).getStringValue();
        if ("comma".equals(sepString)) {
            return SassList.Separator.COMMA;
        } else if ("space".equals(sepString)) {
            return SassList.Separator.SPACE;
        } else if (!("auto".equals(sepString))) {
            throw new ParseException(
                    "The separator of "
                            + getFunctionNames()[0]
                            + "() must be one of 'auto', 'comma', 'space'. Actual value: "
                            + sepString);
        }
        return null;
    }

    protected SassList asList(SassListItem item) {
        return (item instanceof SassList) ? (SassList) item
                : new SassList(item);
    }

}

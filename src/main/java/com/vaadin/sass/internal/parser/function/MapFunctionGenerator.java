/*
 * Copyright 2023 i-net software
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

package com.vaadin.sass.internal.parser.function;

import com.vaadin.sass.internal.parser.FormalArgumentList;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.ParseException;
import com.vaadin.sass.internal.parser.SassList;
import com.vaadin.sass.internal.parser.SassList.Separator;
import com.vaadin.sass.internal.parser.SassListItem;

/**
 * Base class for Map functions
 */
public abstract class MapFunctionGenerator extends AbstractFunctionGenerator {

    public MapFunctionGenerator( FormalArgumentList formalArguments, String... functionNames ) {
        super( formalArguments, functionNames );
    }

    static SassList getMapParam( String paramName, LexicalUnitImpl function, FormalArgumentList actualArguments ) {
        SassListItem map = getParam( actualArguments, paramName );
        if( map instanceof SassList ) {
            SassList list = (SassList)map;
            if( list.size() > 0 ) {
                // we need only to check the first entry, the other has the parser already checked
                SassListItem first = list.get( 0 );
                if( first.getClass() == SassList.class && ((SassList)first).getSeparator() == Separator.COLON ) {
                    return list;
                }
            } else {
                return list;
            }
        }
        throw new ParseException( "Param " + paramName + " of function map-merge(map1,map2) is not a map: " + map.printState(), function );
    }
}

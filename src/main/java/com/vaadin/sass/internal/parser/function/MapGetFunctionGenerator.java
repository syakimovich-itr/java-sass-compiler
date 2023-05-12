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

import java.util.Objects;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.parser.FormalArgumentList;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.SassList;
import com.vaadin.sass.internal.parser.SassListItem;

/**
 * The SASS function "map-get(map,x)".
 */
class MapGetFunctionGenerator extends MapFunctionGenerator {

    MapGetFunctionGenerator() {
        super( createArgumentList( new String[] { "map", "key" }, false ), "map-get" );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SassListItem computeForArgumentList( ScssContext context, LexicalUnitImpl function, FormalArgumentList actualArguments ) {
        SassList map = getMapParam( "map", function, actualArguments );
        String key = getParam( actualArguments, "key" ).unquotedString();

        for( SassListItem item : map ) {
            SassListItem itemKey = ((SassList)item).get( 0 );
            String keyStr = itemKey.unquotedString();
            if( Objects.equals( key, keyStr )) {
                return ((SassList)item).get( 1 );
            }
        }
        return LexicalUnitImpl.createNull( function.getUri(), function.getLineNumber(), function.getColumnNumber() );
    }
}

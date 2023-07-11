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

package com.inet.sass.function;

import java.util.ArrayList;

import com.inet.sass.ScssContext;
import com.inet.sass.parser.FormalArgumentList;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.SassList;
import com.inet.sass.parser.SassListItem;
import com.inet.sass.parser.SassList.Separator;

class MapValuesFunctionGenerator extends MapFunctionGenerator {

    MapValuesFunctionGenerator() {
        super( createArgumentList( new String[] { "map" }, false ), "map-values" );
    }

    @Override
    protected SassListItem computeForArgumentList( ScssContext context, LexicalUnitImpl function, FormalArgumentList actualArguments ) {
        SassList map = getMapParam( "map", function, actualArguments );

        ArrayList<SassListItem> items = new ArrayList<>();
        int size = map.size();
        for( int i = 0; i < size; i++ ) {
            SassList item = (SassList)map.get( i );
            items.add( item.get( 1 ) );
        }
        return new SassList( Separator.COMMA, items );
    }
}

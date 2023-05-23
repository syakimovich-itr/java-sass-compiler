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

class MapMergeFunctionGenerator extends MapFunctionGenerator {

    MapMergeFunctionGenerator() {
        super( createArgumentList( new String[] { "map1", "map2" }, false ), "map-merge" );
    }

    @Override
    protected SassListItem computeForArgumentList( ScssContext context, LexicalUnitImpl function, FormalArgumentList actualArguments ) {
        SassList x = getMapParam( "map1", function, actualArguments );
        SassList y = getMapParam( "map2", function, actualArguments );

        ArrayList<SassListItem> items = new ArrayList<>();
        x.addAllTo( items );
        y.addAllTo( items );
        //a map is a COMMA separated list, which contains n lists with 2 items and COLON as separator
        return new SassList( Separator.COMMA, items );
    }
}

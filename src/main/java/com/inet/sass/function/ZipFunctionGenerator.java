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
import java.util.List;

import com.inet.sass.ScssContext;
import com.inet.sass.parser.FormalArgumentList;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.SassList;
import com.inet.sass.parser.SassList.Separator;
import com.inet.sass.parser.SassListItem;

/**
 * The SASS function "call(x,args)".
 */
class ZipFunctionGenerator extends AbstractFunctionGenerator {

    ZipFunctionGenerator() {
        super( createArgumentList( new String[] { "lists" }, true ), "zip" );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SassListItem computeForArgumentList( ScssContext context, LexicalUnitImpl function, FormalArgumentList actualArguments ) {
        SassList lists = (SassList)getParam( actualArguments, "lists" );

        List<SassListItem> result = new ArrayList<>();

        if( lists.size() > 0 ) {
            ITEMS: for( int i = 0;; i++ ) {
                List<SassListItem> resultItem = new ArrayList<>();
                for( SassListItem list : lists ) {
                    if( list instanceof SassList ) {
                        SassList subList = (SassList)list;
                        if( i >= subList.size() ) {
                            break ITEMS;
                        }
                        resultItem.add( subList.get( i ) );
                    } else if( i == 0 ) {
                        resultItem.add( list );
                    } else {
                        break ITEMS;
                    }
                }
                result.add( new SassList( Separator.SPACE, resultItem ) );
            }
        }
        return new SassList( Separator.COMMA, result );
    }
}

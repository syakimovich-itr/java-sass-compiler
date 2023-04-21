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
package com.vaadin.sass.internal.parser;

import com.vaadin.sass.internal.tree.Node.BuildStringStrategy;

public class MapItem extends SassList {

    /**
     * Create an map entry
     * @param key the key
     * @param value the value
     */
    public MapItem( SassListItem key, SassListItem value ) {
        super( SassList.Separator.COMMA, key, value );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildString( BuildStringStrategy strategy ) {
        return strategy.build( get( 0 ) ) + ':' + strategy.build( get( 1 ) );
    }
}

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
package com.vaadin.sass.internal.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.SassList;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.parser.Variable;
import com.vaadin.sass.internal.tree.Node;
import com.vaadin.sass.internal.tree.controldirective.EachDefNode;

public class EachNodeHandler extends LoopNodeHandler {

    public static Collection<Node> traverse( ScssContext context, EachDefNode eachNode ) {
        List<String> names = eachNode.getVariableNames();
        int size = names.size();
        Collection<List<Variable>> loopVariables = new ArrayList<>();

        for( final SassListItem var : eachNode.getVariables() ) {
            if( size == 1 ) {
                loopVariables.add( Collections.singletonList( new Variable( names.get( 0 ), var ) ) );
            } else {
                List<Variable> eachVars = new ArrayList<>();
                loopVariables.add( eachVars );
                SassList varList = (SassList)var;
                int count = varList.size();
                for( int i = 0; i < size; i++ ) {
                    SassListItem value = count > i ? //
                        varList.get( i ) : // 
                        LexicalUnitImpl.createNull( var.getUri(), var.getLineNumber(), var.getColumnNumber() );
                    eachVars.add( new Variable( names.get( i ), value ) );
                }
            }
        }
        return replaceLoopNode( context, eachNode, loopVariables );
    }
}

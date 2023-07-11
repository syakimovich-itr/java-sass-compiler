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
package com.inet.sass.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.inet.sass.ScssContext;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.ParseException;
import com.inet.sass.parser.SassList;
import com.inet.sass.parser.SassListItem;
import com.inet.sass.parser.Variable;
import com.inet.sass.tree.Node;
import com.inet.sass.tree.controldirective.EachDefNode;

public class EachNodeHandler extends LoopNodeHandler {

    public static Collection<Node> traverse( ScssContext context, EachDefNode eachNode ) {
        List<String> names = eachNode.getVariableNames();
        int size = names.size();
        Collection<List<Variable>> loopVariables = new ArrayList<>();

        SassListItem variables = eachNode.getVariables();
        variables = variables.evaluateFunctionsAndExpressions( context, true );
        SassList list = variables instanceof SassList ? (SassList) variables : new SassList(variables);

        for( final SassListItem var : list ) {
            if( size == 1 ) {
                loopVariables.add( Collections.singletonList( new Variable( names.get( 0 ), var ) ) );
            } else {
                List<Variable> eachVars = new ArrayList<>();
                loopVariables.add( eachVars );
                SassList varList;
                try {
                    varList = (SassList)var;
                } catch( Exception e ) {
                    throw new ParseException( "Each item is not a list and can't expand to multiple variables " + names, var );
                }
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

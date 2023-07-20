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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.inet.sass.ScssContext;
import com.inet.sass.expression.BinaryOperator;
import com.inet.sass.handler.SCSSErrorHandler;
import com.inet.sass.parser.ParseException;
import com.inet.sass.parser.SassListItem;
import com.inet.sass.tree.Node;
import com.inet.sass.tree.controldirective.TemporaryNode;
import com.inet.sass.tree.controldirective.WhileNode;

public class WhileNodeHandler {

    /**
     * Replace a WhileNode with the expanded set of nodes.
     * 
     * @param context
     *            current compilation context
     * @param whileNode
     *            node to replace
     */
    public static Collection<Node> traverse( ScssContext context, WhileNode whileNode ) {
        TemporaryNode tempParent = new TemporaryNode( whileNode.getParentNode() );
        List<Node> children = whileNode.getChildren();
        while( evaluateCondition( context, whileNode ) ) {
            if( children.size() == 0 ) {
                SCSSErrorHandler.get().error( new ParseException( "@while loop iteration did nothing, infinite loop", whileNode ) );
                return children;
            }
            LoopNodeHandler.iteration( context, children, tempParent, Collections.emptyList() );
        }
        return tempParent.getChildren();
    }

    private static boolean evaluateCondition( ScssContext context, WhileNode whileNode ) {
        SassListItem condition = whileNode.getCondition();
        condition = condition.evaluateFunctionsAndExpressions( context, true );
        return BinaryOperator.isTrue( condition );
    }
}

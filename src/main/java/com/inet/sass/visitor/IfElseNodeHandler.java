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

import com.inet.sass.ScssContext;
import com.inet.sass.expression.BinaryOperator;
import com.inet.sass.parser.ParseException;
import com.inet.sass.parser.SassListItem;
import com.inet.sass.tree.Node;
import com.inet.sass.tree.controldirective.ElseNode;
import com.inet.sass.tree.controldirective.IfElseDefNode;
import com.inet.sass.tree.controldirective.IfNode;
import com.inet.sass.tree.controldirective.TemporaryNode;

public class IfElseNodeHandler {

    public static Collection<Node> traverse(ScssContext context,
            IfElseDefNode node) throws Exception {
        for (final Node child : node.getChildren()) {
            if (child instanceof IfNode) {
                SassListItem expression = ((IfNode) child).getExpression();
                expression = expression.evaluateFunctionsAndExpressions(
                        context, true);

                if (BinaryOperator.isTrue(expression)) {
                    return traverseChild(context, node.getParentNode(), child);
                }
            } else {
                if (!(child instanceof ElseNode)
                        && node.getChildren().indexOf(child) == node
                                .getChildren().size() - 1) {
                    throw new ParseException( "Invalid @if/@else in scss file for " + node, node );
                } else {
                    return traverseChild(context, node.getParentNode(), child);
                }
            }
        }
        // no matching branch
        return Collections.emptyList();
    }

    private static Collection<Node> traverseChild(ScssContext context,
            Node parent, Node child) {
        TemporaryNode tempParent = new TemporaryNode(parent,
                child.getChildren());
        return tempParent.traverse(context);
    }

}

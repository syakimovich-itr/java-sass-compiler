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

import com.inet.sass.Scope;
import com.inet.sass.ScssContext;
import com.inet.sass.handler.SCSSErrorHandler;
import com.inet.sass.parser.ParseException;
import com.inet.sass.parser.Variable;
import com.inet.sass.tree.MixinDefNode;
import com.inet.sass.tree.MixinNode;
import com.inet.sass.tree.Node;
import com.inet.sass.tree.controldirective.TemporaryNode;

public class MixinNodeHandler {

    public static Collection<Node> traverse(ScssContext context, MixinNode node) {
        return replaceMixins(context, node);
    }

    private static Collection<Node> replaceMixins(ScssContext context,
            MixinNode node) {
        MixinDefNode mixinDef = context.getMixinDefinition(node.getName());
        if (mixinDef == null) {
            SCSSErrorHandler.get().error( new ParseException( "Mixin Definition: " + node.getName() + " not found", node ) );
            return Collections.emptyList();
        }
        return replaceMixinNode(context, node, mixinDef);
    }

    private static Collection<Node> replaceMixinNode( ScssContext context, MixinNode mixinNode, MixinDefNode mixinDef ) {
        MixinDefNode defClone = mixinDef.copy();

        defClone.replaceContentDirective( context, mixinNode );

        if( !mixinDef.getArglist().isEmpty() ) {
            defClone.replacePossibleArguments( mixinNode.getArglist() );
        }

        // parameters have been evaluated in parent scope, rest should be
        // in the scope where the mixin was defined
        // every evaluated parameter can also be input for evaluating the next parameters
        Scope previousScope = context.openVariableScope( defClone.getDefinitionScope() );
        try {
            // add variables from argList
            for( Variable var : defClone.getArglist().getArguments() ) {
                Variable evaluated = new Variable( var.getName(), var.getExpr().evaluateFunctionsAndExpressions( context, true ) );
                context.addVariable( evaluated );
            }
            // traverse child nodes in this scope
            // use correct parent with intermediate TemporaryNode
            Node tempParent = new TemporaryNode( mixinNode.getParentNode(), defClone.getChildren() );
            return tempParent.traverse( context );
        } finally {
            context.closeVariableScope( previousScope );
        }
    }
}

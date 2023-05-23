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
package com.inet.sass.tree.controldirective;

import java.util.Collection;
import java.util.Collections;

import com.inet.sass.ScssContext;
import com.inet.sass.handler.SCSSErrorHandler;
import com.inet.sass.tree.Node;
import com.inet.sass.visitor.IfElseNodeHandler;

public class IfElseDefNode extends Node {

    public IfElseDefNode() {
    }

    private IfElseDefNode(IfElseDefNode nodeToCopy) {
        super(nodeToCopy);
    }

    @Override
    public String printState() {
        return buildString(PRINT_STRATEGY);
    }

    @Override
    public String toString() {
        return "IfElseDef node [" + buildString(TO_STRING_STRATEGY) + "]";
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        try {
            return IfElseNodeHandler.traverse(context, this);
        } catch (Exception e) {
            SCSSErrorHandler.get().error( e );
            return Collections.emptyList();
        }
    }

    private String buildString(BuildStringStrategy strategy) {
        StringBuilder b = new StringBuilder();
        for (final Node child : getChildren()) {
            b.append(strategy.build(child));
            b.append("\n");
        }
        return b.toString();
    }

    @Override
    public IfElseDefNode copy() {
        return new IfElseDefNode(this);
    }

}

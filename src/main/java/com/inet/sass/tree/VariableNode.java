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

package com.inet.sass.tree;

import java.util.Collection;
import java.util.Collections;

import com.inet.sass.Definition;
import com.inet.sass.ScssContext;
import com.inet.sass.parser.SassListItem;
import com.inet.sass.parser.Variable;
import com.inet.sass.visitor.VariableNodeHandler;

public class VariableNode extends Node implements Definition, NodeWithUrlContent {

    private final Variable variable;

    public VariableNode(String name, SassListItem expr, boolean guarded) {
        super();
        variable = new Variable(name, expr, guarded);
    }

    public SassListItem getExpr() {
        return variable.getExpr();
    }

    public void setExpr(SassListItem expr) {
        variable.setExpr(expr);
    }

    public String getName() {
        return variable.getName();
    }

    public boolean isGuarded() {
        return variable.isGuarded();
    }

    public Variable getVariable() {
        return variable;
    }

    @Override
    public String printState() {
        return buildString(PRINT_STRATEGY);
    }

    @Override
    public String toString() {
        return "Variable node [" + buildString(TO_STRING_STRATEGY) + "]";
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        /*
         * containsArithmeticalOperator() must be called before
         * replaceVariables. Because for the "/" operator, it needs to see if
         * its predecessor or successor is a Variable or not, to determine it is
         * an arithmetic operator.
         */
        boolean hasOperator = variable.getExpr().containsArithmeticalOperator();
        variable.setExpr(variable.getExpr().evaluateFunctionsAndExpressions(
                context, hasOperator));
        VariableNodeHandler.traverse(context, this);
        return Collections.emptyList();
    }

    private String buildString(BuildStringStrategy strategy) {
        StringBuilder builder = new StringBuilder("$");
        builder.append(getName()).append(": ")
                .append(strategy.build(getExpr()));
        return builder.toString();
    }

    @Override
    public VariableNode copy() {
        return new VariableNode(getName(), getExpr(), isGuarded());
    }

    @Override
    public VariableNode updateUrl(String prefix) {
        if (getExpr() != null) {
            SassListItem newExpr = variable.getExpr().updateUrl(prefix);
            return new VariableNode(getName(), newExpr, isGuarded());
        }
        return this;
    }
}

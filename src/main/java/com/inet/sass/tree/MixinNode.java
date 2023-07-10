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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.inet.sass.ScssContext;
import com.inet.sass.handler.SCSSErrorHandler;
import com.inet.sass.parser.ActualArgumentList;
import com.inet.sass.parser.SassList;
import com.inet.sass.parser.Variable;
import com.inet.sass.visitor.MixinNodeHandler;

/**
 * Node for including a Mixin.
 * 
 * MixinNode handles argument lists with support for variable arguments. When
 * variable arguments are used, a MixinNode expands a list into separate
 * arguments, whereas a DefNode packs several arguments into a list. The
 * corresponding definition node is {@link MixinDefNode}.
 * 
 * @author Vaadin
 */
public class MixinNode extends Node implements IVariableNode,
        NodeWithUrlContent {

    // these are the actual parameter values, not whether the definition node
    // uses varargs
    private ActualArgumentList arglist;
    private String name;

    public MixinNode( String uri, int line, int column, String name, ActualArgumentList args ) {
        super( uri, line, column );
        this.name = name;
        this.arglist = args;
    }

    private MixinNode(MixinNode nodeToCopy) {
        super(nodeToCopy);
        arglist = nodeToCopy.arglist;
        name = nodeToCopy.name;
    }

    public ActualArgumentList getArglist() {
        return arglist;
    }

    protected void expandVariableArguments() {
        arglist = arglist.expandVariableArguments();
    }

    public String getName() {
        return name;
    }

    /**
     * Replace variable references with their values in the argument list and
     * name.
     */
    @Override
    public void replaceVariables(ScssContext context) {
        arglist = arglist.replaceVariables(context);
        arglist = arglist.evaluateFunctionsAndExpressions(context, true);
    }

    @Override
    public String printState() {
        return "name: " + getName() + " args: " + getArglist();
    }

    @Override
    public String toString() {
        return "Mixin node [" + printState() + "]";
    }

    @Override
    public Collection<Node> traverse( ScssContext context ) {
        try {
            replaceVariables( context );
            expandVariableArguments();
            // inner scope is managed by MixinNodeHandler
            return MixinNodeHandler.traverse( context, this );
        } catch( Exception ex ) {
            SCSSErrorHandler.get().error( ex );
            // TODO is ignoring this exception appropriate?
            return Collections.emptyList();
        }
    }

    @Override
    public MixinNode copy() {
        return new MixinNode(this);
    }

    @Override
    public NodeWithUrlContent updateUrl(String prefix) {
        MixinNode newInstance = copy();
        newInstance.arglist = arglist.updateUrl(prefix);
        return newInstance;
    }

}

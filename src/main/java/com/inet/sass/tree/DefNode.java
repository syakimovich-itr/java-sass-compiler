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

import com.inet.sass.Definition;
import com.inet.sass.Scope;
import com.inet.sass.ScssContext;
import com.inet.sass.parser.ActualArgumentList;
import com.inet.sass.parser.FormalArgumentList;

/**
 * DefNode defines the shared functionality of mixin and function definition
 * nodes. This includes the handling of parameter lists.
 * 
 * @author Vaadin
 * 
 */
public abstract class DefNode extends Node implements Definition,
        IVariableNode, NodeWithUrlContent {
    private String name;
    private FormalArgumentList arglist;
    private Scope definitionScope;

    public DefNode(String name, FormalArgumentList args ) {
        super();
        this.name = name;
        this.arglist = args;
    }

    protected DefNode(DefNode nodeToCopy) {
        super(nodeToCopy);
        name = nodeToCopy.name;
        arglist = nodeToCopy.arglist;
        definitionScope = nodeToCopy.definitionScope;
    }

    public String getName() {
        return name;
    }

    public FormalArgumentList getArglist() {
        return arglist;
    }

    public boolean hasVariableArguments() {
        return arglist.hasVariableArguments();
    }

    @Override
    public void replaceVariables(ScssContext context) {
    }

    public void replacePossibleArguments(ActualArgumentList actualArgumentList) {
        // TODO instead of modifying def, return a VariableArgumentList?
        arglist = arglist.replaceFormalArguments(actualArgumentList, true);
    }

    public Scope getDefinitionScope() {
        return definitionScope;
    }

    protected void setDefinitionScope(Scope scope) {
        definitionScope = scope;
    }

    public DefNode updateUrl(String prefix) {
        DefNode newInstance = (DefNode) copy();
        newInstance.arglist = arglist.updateUrl(prefix);
        return newInstance;
    }
}

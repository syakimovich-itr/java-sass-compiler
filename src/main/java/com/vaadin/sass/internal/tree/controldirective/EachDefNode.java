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

package com.vaadin.sass.internal.tree.controldirective;

import java.util.Collection;
import java.util.List;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.parser.SassList;
import com.vaadin.sass.internal.parser.SassListItem;
import com.vaadin.sass.internal.tree.IVariableNode;
import com.vaadin.sass.internal.tree.Node;
import com.vaadin.sass.internal.tree.NodeWithUrlContent;
import com.vaadin.sass.internal.visitor.EachNodeHandler;

public class EachDefNode extends Node implements IVariableNode,
        NodeWithUrlContent {
    private static final long serialVersionUID = 7943948981204906221L;

    private List<String> variableNames;
    private SassListItem list;

    public EachDefNode( List<String> variableNames, SassListItem list) {
        super();
        this.variableNames = variableNames;
        this.list = list;
    }

    private EachDefNode(EachDefNode nodeToCopy) {
        super(nodeToCopy);
        variableNames = nodeToCopy.variableNames;
        list = nodeToCopy.list;
    }

    public SassListItem getVariables() {
        return list;
    }

    public List<String> getVariableNames() {
        return variableNames;
    }

    @Override
    public String toString() {
        return "Each Definition Node: {variable : " + variableNames + ", "
                + "children : " + list + "}";
    }

    @Override
    public void replaceVariables(ScssContext context) {
        list = list.replaceVariables(context);
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        replaceVariables(context);
        return EachNodeHandler.traverse(context, this);
    }

    @Override
    public EachDefNode copy() {
        return new EachDefNode(this);
    }

    @Override
    public NodeWithUrlContent updateUrl(String prefix) {
        EachDefNode copy = copy();
        copy.list = list.updateUrl(prefix);
        return copy;
    }
}

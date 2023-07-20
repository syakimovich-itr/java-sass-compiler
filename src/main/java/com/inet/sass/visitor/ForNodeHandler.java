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
import com.inet.sass.parser.SassListItem;
import com.inet.sass.parser.Variable;
import com.inet.sass.tree.Node;
import com.inet.sass.tree.controldirective.ForNode;

public class ForNodeHandler extends LoopNodeHandler {

    public static Collection<Node> traverse(ScssContext context, ForNode forNode) {
        int fromInt = getInt(context, forNode.getFrom());
        int toInt = getInt(context, forNode.getTo());
        if (forNode.isExclusive()) {
            toInt = toInt - 1;
        }
        Collection<List<Variable>> indices = new ArrayList<>();
        for( int idx = fromInt; idx <= toInt; ++idx ) {
            SassListItem from = forNode.getFrom();
            LexicalUnitImpl idxUnit = LexicalUnitImpl.createInteger( from.getUri(), //
                                                                     from.getLineNumber(), //
                                                                     from.getColumnNumber(), //
                                                                     idx );
            indices.add( Collections.singletonList( new Variable( forNode.getVariableName(), idxUnit ) ) );
        }
        return replaceLoopNode( context, forNode, indices );
    }

    private static int getInt(ScssContext context, SassListItem item) {
        SassListItem value = item
                .evaluateFunctionsAndExpressions(context, true);
        if (value instanceof LexicalUnitImpl
                && ((LexicalUnitImpl) value).isNumber()) {
            return ((LexicalUnitImpl) value).getIntegerValue();
        }
        throw new ParseException(
                "The loop indices of @for must evaluate to integer values",
                item);
    }

}

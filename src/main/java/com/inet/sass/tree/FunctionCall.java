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

import com.inet.sass.Scope;
import com.inet.sass.ScssContext;
import com.inet.sass.handler.SCSSErrorHandler;
import com.inet.sass.parser.ActualArgumentList;
import com.inet.sass.parser.FormalArgumentList;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.ParseException;
import com.inet.sass.parser.SassListItem;
import com.inet.sass.parser.Variable;

/**
 * Transient class representing a function call to a custom (user-defined)
 * function. This class is used to evaluate the function call and is discarded
 * after use. A FunctionCall does not have a parent in the stylesheet node tree.
 */
public class FunctionCall {

    public static SassListItem evaluate(ScssContext context,
            FunctionDefNode def, LexicalUnitImpl invocation) {
        ActualArgumentList invocationArglist = invocation.getParameterList()
                .expandVariableArguments();
        SassListItem value = null;
        Exception cause = null;
        // only parameters are evaluated in current scope, body in
        // top-level scope
        try {
            FormalArgumentList arglist = def.getArglist();
            arglist = arglist.replaceFormalArguments(invocationArglist, true);

            // copying is necessary as traversal modifies the parent of the
            // node
            // TODO in the long term, should avoid full copy
            FunctionDefNode defCopy = def.copy();

            // limit variable scope to the scope where the function was defined
            Scope previousScope = context.openVariableScope(def
                    .getDefinitionScope());
            try {
                // replace variables in default values of parameters
                for (Variable arg : arglist) {
                    SassListItem expr = arg.getExpr();
                    if (expr != null) {
                        expr = expr.evaluateFunctionsAndExpressions( context, true );
                    }
                    context.addVariable(new Variable(arg.getName(), expr));
                }

                // only contains variable nodes, return nodes and control
                // structures
                while (defCopy.getChildren().size() > 0) {
                    Node firstChild = defCopy.getChildren().get(0);
                    if (firstChild instanceof ReturnNode) {
                        ReturnNode returnNode = ((ReturnNode) firstChild);
                        value = returnNode.evaluate(context);
                        break;
                    }
                    defCopy.replaceNode(firstChild, new ArrayList<Node>(
                            firstChild.traverse(context)));
                }
            } finally {
                context.closeVariableScope(previousScope);
            }
        } catch( Exception ex ) {
            cause = ex;
            SCSSErrorHandler.get().warning( ex );
        }
        if (value == null) {
            ParseException pex = new ParseException( "Function " + invocation.getFunctionName() + " did not return a value", invocation );
            pex.initCause( cause );
            throw pex;
        }
        return value;
    }

}

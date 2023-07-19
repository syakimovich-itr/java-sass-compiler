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

import com.inet.sass.ScssContext;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.SCSSLexicalUnit;
import com.inet.sass.parser.SassListItem;
import com.inet.sass.parser.Variable;
import com.inet.sass.tree.VariableNode;

public class VariableNodeHandler {

    public static void traverse( ScssContext context, VariableNode node ) {
        if( !node.isGuarded() ) {
            context.setVariable( node.getVariable() );
            return;
        }
        Variable variable = context.getVariable( node.getName() );
        if( variable == null || variable.getExpr() == null ) {
            context.setVariable( node.getVariable() );
        } else { // Handle the case where a variable has the value SCSS_NULL
            SassListItem value = variable.getExpr();
            if( value.getItemType() == SCSSLexicalUnit.SCSS_NULL ) {
                context.setVariable( node.getVariable() );
            }
        }
    }
}

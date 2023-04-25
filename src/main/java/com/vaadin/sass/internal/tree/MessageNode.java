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
package com.vaadin.sass.internal.tree;

import java.util.Collection;
import java.util.Collections;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.handler.SCSSErrorHandler;
import com.vaadin.sass.internal.parser.SassListItem;

public class MessageNode extends Node implements IVariableNode {

    public static enum MessageLevel {
        debug, warn, error;
    }

    private SassListItem message;
    private MessageLevel level;

    public MessageNode( SassListItem message, MessageLevel level ) {
        this.message = message;
        this.level = level;
    }

    @Override
    public void replaceVariables( ScssContext context ) {
        message = message.replaceVariables( context );
    }

    @Override
    public String printState() {
        return "";
    }

    @Override
    public String toString() {
        return "@" + level + ' ' + message;
    }

    @Override
    public Collection<Node> traverse( ScssContext context ) {
        SCSSErrorHandler handler = SCSSErrorHandler.get();
        replaceVariables( context );
        String msg = message.unquotedString();
        switch( level ) {
            case debug:
                handler.debug( msg );
                break;
            case warn:
                handler.warning( msg );
                break;
            case error:
                handler.error( msg );
                break;
        }
        return Collections.emptyList();
    }

    @Override
    public MessageNode copy() {
        return new MessageNode( message, level );
    }
}

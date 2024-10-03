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
package com.inet.sass;

import java.util.HashMap;

import com.inet.sass.parser.Variable;
import com.inet.sass.tree.FunctionDefNode;
import com.inet.sass.tree.MixinDefNode;

/**
 * Nestable scope for variables, functions and mixins.
 */
public class Scope {

    private static final Definition MISSING = new Variable( null, null );

    private static class DefinitionScope<T extends Definition> {
        private DefinitionScope<T> parent;
        // optimization: create map only when needed
        private HashMap<String, T> definitions;

        public DefinitionScope( DefinitionScope<T> parent ) {
            this.parent = parent;
        }

        /**
         * Sets a definition value in the largest scope where it is already defined. If the variable isn't defined, set it in the current scope.
         * @param node definition to set
         */
        public void set( T node ) {
            if( parent == null || !parent.setIfPresent( node ) ) {
                add( node );
            }
        }

        /**
         * Sets a definition in the current scope without checking parent scopes.
         * @param node definition to set
         */
        public void add( T node ) {
            HashMap<String, T> definitions = this.definitions;
            if( definitions == null ) {
                definitions = this.definitions = new HashMap<String, T>();
            }
            final String unifiedName = getUnifiedName(node.getName());
            definitions.put( unifiedName, node );
        }

        /**
         * Sets a definition and returns true if it is already defined in the scope or its parents. Otherwise returns false.
         * @param node definition to set
         * @return true if the definition was set
         */
        private boolean setIfPresent( T node ) {
            if( parent != null && parent.setIfPresent( node ) ) {
                return true;
            }
            HashMap<String, T> definitions = this.definitions;
            if( definitions != null ) {
                final String unifiedName = getUnifiedName(node.getName());
                return definitions.replace( unifiedName, node ) != null;
            }
            return false;
        }

        public T get( String name ) {
            final String unifiedName = getUnifiedName(name);
            HashMap<String, T> definitions = this.definitions;
            if( definitions != null ) {
                T value = definitions.getOrDefault( unifiedName, (T)MISSING );
                if( value != MISSING ) {
                    return value;
                }
            }
            DefinitionScope<T> parent = this.parent;
            if( parent != null ) {
                return parent.get( unifiedName );
            } else {
                return null;
            }
        }

        @Override
        public String toString() {
            if( definitions != null ) {
                return definitions.keySet().toString() + ", parent = " + parent;
            } else {
                return "{}, parent = " + parent;
            }
        }

        /**
         * Sass treat hyphens and underscores as identical in  identifiers.
         * This means that reset-list and reset_list both refer to the same mixin.
         * This is a historical holdover from the very early days of Sass,
         * when it only allowed underscores in identifier names.
         * Once Sass added support for hyphens to match CSS’s syntax,
         * the two were made equivalent to make migration easier.
         */
        private String getUnifiedName(final String name) {
            return name != null
                    ? name.replace("_", "-")
                    : null;
        }
    }

    private Scope                                  parent;
    private final DefinitionScope<Variable>        variables;
    private final DefinitionScope<FunctionDefNode> functions;
    private final DefinitionScope<MixinDefNode>    mixins;

    public Scope() {
        variables = new DefinitionScope<Variable>( null );
        functions = new DefinitionScope<FunctionDefNode>( null );
        mixins = new DefinitionScope<MixinDefNode>( null );
    }

    public Scope( Scope parent ) {
        this.parent = parent;
        variables = new DefinitionScope<Variable>( parent.variables );
        functions = new DefinitionScope<FunctionDefNode>( parent.functions );
        mixins = new DefinitionScope<MixinDefNode>( parent.mixins );
    }

    public Scope getParent() {
        return parent;
    }

    /**
     * Sets a variable value in the largest scope where it is already defined. If the variable isn't defined, set it in the current scope.
     * @param node variable to set
     */
    public void setVariable( Variable node ) {
        variables.set( node );
    }

    /**
     * Sets a variable in the current scope without checking parent scopes.
     * @param node variable to set
     */
    public void addVariable( Variable node ) {
        variables.add( node );
    }

    public Variable getVariable( String name ) {
        return variables.get( name );
    }

    public void defineFunction( FunctionDefNode function ) {
        functions.add( function );
    }

    public void defineMixin( MixinDefNode mixin ) {
        mixins.add( mixin );
    }

    public FunctionDefNode getFunctionDefinition( String name ) {
        return functions.get( name );
    }

    public MixinDefNode getMixinDefinition( String name ) {
        return mixins.get( name );
    }

    @Override
    public String toString() {
        return "Variables: " + variables.toString() + "\nFunctions: " + functions.toString() + "\nMixins: " + mixins.toString();
    }

}

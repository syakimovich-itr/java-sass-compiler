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
package com.inet.sass.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.inet.sass.ScssContext;
import com.inet.sass.parser.LexicalUnitImpl;
import com.inet.sass.parser.SassListItem;

/**
 * Generator class is used to handle SCSS functions. Generator is applied to the
 * function lexical unit if its method {@link #getFunctionNames()} returns name
 * of the function.
 * 
 * If there are no dedicated generator for the function then default generator
 * is used.
 * 
 * @author Vaadin Ltd
 */
public interface SCSSFunctionGenerator {

    public static SCSSFunctionGenerator getGenerator( String funcName ) {
        return Registry.FUNCTIONS.get( funcName );
    }

    /**
     * Register a custom sass function.
     * @param generator the implementation of the custom function
     */
    public static void registerCustomFunction( SCSSFunctionGenerator generator ) {
        for( String functionName : generator.getFunctionNames() ) {
            Registry.FUNCTIONS.put( functionName, generator );
        }
    }

    /**
     * Returns function names handled by this generator. Default generator
     * returns <code>null</code> and is used if there is no dedicated generator
     * for given function.
     * 
     * @return
     */
    String[] getFunctionNames();

    /**
     * Computes the value of the function. The parameters should be evaluated
     * before this method is called.
     * 
     * Both the input and the output of the method should be separate from any
     * chain of lexical units.
     * 
     * @param context
     *            current compilation context
     * @param function
     *            Function lexical unit to print its state
     * @return SassListItem the value of the function
     */
    SassListItem compute( ScssContext context, LexicalUnitImpl function );

    abstract class Registry {
        static final Map<String, SCSSFunctionGenerator> FUNCTIONS = new HashMap<>();

        static {
            for( SCSSFunctionGenerator serializer : defaultFunctions() ) {
                registerCustomFunction( serializer );
            }
        }

        private static List<SCSSFunctionGenerator> defaultFunctions() {
            List<SCSSFunctionGenerator> list = new ArrayList<SCSSFunctionGenerator>();
            list.add( new AbsFunctionGenerator() );
            list.add( new AdjustColorFunctionGenerator() );
            list.add( new CallFunctionGenerator() );
            list.add( new CeilFunctionGenerator() );
            list.add( new ComparableFunctionGenerator() );
            list.add( new DarkenFunctionGenerator() );
            list.add( new FloorFunctionGenerator() );
            list.add( new GetFunctionFunctionGenerator() );
            list.add( new GrayscaleFunctionGenerator() );
            list.add( new IfFunctionGenerator() );
            list.add( new InspectFunctionGenerator() );
            list.add( new LightenFunctionGenerator() );
            list.add( new ListAppendFunctionGenerator() );
            list.add( new ListIndexFunctionGenerator() );
            list.add( new ListJoinFunctionGenerator() );
            list.add( new ListLengthFunctionGenerator() );
            list.add( new ListNthFunctionGenerator() );
            list.add( new MapGetFunctionGenerator() );
            list.add( new MapKeysFunctionGenerator() );
            list.add( new MapMergeFunctionGenerator() );
            list.add( new MinMaxFunctionGenerator() );
            list.add( new MixFunctionGenerator() );
            list.add( new PercentageFunctionGenerator() );
            list.add( new RectFunctionGenerator() );
            list.add( new RGBFunctionGenerator() );
            list.add( new RoundFunctionGenerator() );
            list.add( new SaturationModificationFunctionGenerator() );
            list.add( new StrIndexFunctionGenerator() );
            list.add( new StrLengthFunctionGenerator() );
            list.add( new StrSliceFunctionGenerator() );
            list.add( new TypeOfFunctionGenerator() );
            list.add( new AlphaFunctionGenerator() );
            list.add( new TransparencyModificationFunctionGenerator() );
            list.add( new ColorComponentFunctionGenerator() );
            list.add( new UnitFunctionGenerator() );
            list.add( new UnitlessFunctionGenerator() );
            list.add( new QuoteUnquoteFunctionGenerator() );
            list.add( new VariableExistsFunctionGenerator() );
            return list;
        }
    }
}

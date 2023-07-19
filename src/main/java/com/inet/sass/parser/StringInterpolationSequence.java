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
package com.inet.sass.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.inet.sass.ScssContext;

/**
 * StringInterpolationSequence is used for representing sequences consisting of
 * strings and interpolation.
 * 
 * An unevaluated StringInterpolationSequence consists of StringItem (a wrapper
 * for a String) and Interpolation objects. The result of calling
 * replaceInterpolation is a StringInterpolationSequence where occurrences of
 * interpolation have been replaced with the contents of the interpolation.
 * 
 * @author Vaadin
 * 
 */
public class StringInterpolationSequence {
    private boolean containsInterpolation;
    private List<SassListItem> items;
    private String toString;

    /**
     * Creates a new StringInterpolationSequence containing only the given item without interpolation.
     * @param value single string value
     */
    public StringInterpolationSequence( String value ) {
        items = Collections.<SassListItem> singletonList( new StringItem( value ) );
    }

    /**
     * Creates a new StringInterpolationSequence. The list sequence should only
     * contain StringItem and Interpolation objects.
     * 
     * @param sequence
     *            A list of StringItem and Interpolation objects.
     */
    public StringInterpolationSequence(List<SassListItem> sequence) {
        for( SassListItem item : sequence ) {
            if( item.getItemType() == SCSSLexicalUnit.SCSS_INTERPOLATION ) {
                containsInterpolation = true;
                break;
            }
        }
        items = sequence;
    }

    /**
     * Creates a new sequence that is obtained from this by replacing all
     * variables occurring in expressions. Also replaces functions, arithmetic
     * expressions and interpolation if all variables have been set.
     * 
     * @param context
     *            current compilation context
     * @return A new StringInterpolationSequence.
     */
    public StringInterpolationSequence replaceVariables( ScssContext context ) {
        if( !containsInterpolation ) {
            return this;
        }

        return new StringInterpolationSequence( SassList.replaceVariables( context, items ) );
    }

    /**
     * Creates a new StringInterpolationSequence that contains all items of this
     * and other. Does not modify this or other.
     * 
     * @param other
     *            The StringInterpolationSequence to be appended to the end of
     *            this.
     * @return The appended StringInterpolationSequence.
     */
    public StringInterpolationSequence append(StringInterpolationSequence other) {
        ArrayList<SassListItem> result = new ArrayList<SassListItem>(
                items);
        result.addAll(other.items);
        return new StringInterpolationSequence(result);
    }

    @Override
    public String toString() {
        if( toString == null ) {
            List<SassListItem> items = this.items;
            if( items.size() == 1 ) {
                return items.get( 0 ).printState();
            }
            StringBuilder result = new StringBuilder();
            for( SassListItem item : items ) {
                result.append( item.printState() );
            }
            toString = result.toString();
        }
        return toString;
    }

    /**
     * Returns true if this sequence contains interpolation, i.e. either an
     * interpolation object or a string containing interpolation. This method is
     * intended to be used as a quick test for avoiding repeated evaluation of
     * interpolation when none appear in the StringInterpolationSequence. As
     * such, the result false is always exact but if this method returns true,
     * it is still possible that there is no interpolation.
     * 
     * @return whether this sequence contains an Interpolation object or a
     *         string containing interpolation.
     */
    public boolean containsInterpolation() {
        return containsInterpolation;
    }

    public List<SassListItem> getItems() {
        return items;
    }

    public StringInterpolationSequence updateUrl(String prefix) {
        if( containsInterpolation ) {
            return new StringInterpolationSequence( SassList.updateUrl( items, prefix ) );
        }
        return this;
    }
}
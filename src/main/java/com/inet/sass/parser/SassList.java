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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.inet.sass.ScssContext;
import com.inet.sass.tree.Node;
import com.inet.sass.tree.Node.BuildStringStrategy;

/**
 * SassList is a list that has a specified separator character (comma or space)
 * and data items. The data items can be lists.
 */
public class SassList implements SassListItem, Iterable<SassListItem> {

    public enum Separator {
        COMMA(", "), SPACE(" "), 
        /** used for map items */
        COLON(":");
        private String separator;

        private Separator(String sep) {
            separator = sep;
        }

        @Override
        public String toString() {
            return separator;
        }
    }

    // The position where the list occurs in the source scss file.
    private int line = -1;
    private int column = -1;

    private Separator separator;

    private final List<SassListItem> items;

    public SassList() {
        this(Separator.SPACE);
    }

    public SassList(SassListItem... items) {
        this(Separator.SPACE, items);
    }

    public SassList(Separator sep, SassListItem... items) {
        separator = sep;
        this.items = Arrays.asList(items);
    }

    public SassList(Separator sep, List<SassListItem> items) {
        separator = sep;
        this.items = items;
    }

    @Override
    public int getLineNumber() {
        return line < 0 && items.size() > 0 ? items.get(  0 ).getLineNumber() : line;
    }

    @Override
    public int getColumnNumber() {
        return column < 0 && items.size() > 0 ? items.get(  0 ).getColumnNumber() : column;
    }

    public void setSourcePosition(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public Separator getSeparator() {
        return separator;
    }

    public void setSeparator(Separator separator) {
        this.separator = separator;
    }

    /**
     * Returns the only LexicalUnitImpl contained in this list.
     * 
     * @throws ParseException
     *             if this.size() != 1 or if the type of this.get(0) is not
     *             LexicalUnitImpl.
     */
    @Override
    public LexicalUnitImpl getContainedValue() {
        if (size() != 1 || !(get(0) instanceof LexicalUnitImpl)) {
            throw new ParseException(
                    "Expected a one-element list, actual value: "
                            + getStringWithNesting());
        }
        return (LexicalUnitImpl) get(0);
    }

    // A helper method for sass interpolation
    public String unquotedString() {
        if (size() != 1) {
            // preserve quotes if the list contains several elements
            return printState();
        }
        if (get(0) instanceof SassList) {
            // A nested list may contain one or more elements, handle
            // recursively.
            return ((SassList) get(0)).unquotedString();
        }

        // Handle a list with one element that is not a list.
        String result = printState();
        if (result.length() >= 2
                && ((result.charAt(0) == '"' && result
                        .charAt(result.length() - 1) == '"') || (result
                        .charAt(0) == '\'' && result
                        .charAt(result.length() - 1) == '\''))) {
            result = result.substring(1, result.length() - 1);
        }
        return result;
    }

    /**
     * Returns a SassListItem whose textual (CSS) representation is the same as
     * that of this list. Any extra nesting is recursively removed. Nesting is
     * extra if a list contains only one element. A list with extra nesting is
     * replaced by its contents (a SassList or a SassListItem). The flattened
     * representation of an empty list is the item itself.
     * 
     * For a non-empty list the definition of flatten is recursive. The
     * flattened representation of a list containing a single value is the
     * flattened representation of the value. For a list containing multiple
     * values, the flattened representation is obtained by replacing all
     * elements of the list by their flattened representations.
     * 
     * Examples of flattened representations: a) (1) -> 1 b) (1 (2) ((3)) ) ->
     * (1 2 3) c) (1, (2, 3), 4) -> (1, (2, 3), 4) (i.e., no change).
     * 
     * Note that the flattened representation of a list can be a single value
     * instead of a list, as in the example (a) above.
     * 
     * This method should only be called by the parser.
     * 
     * @return A flattened representation of this item.
     */
    public SassListItem flatten() {
        return flatten(this);
    }

    private static SassListItem flatten( SassListItem item ) {
        if( item instanceof SassList ) {
            SassList sassList = (SassList)item;
            int size = sassList.size();
            if( size == 1 ) {
                SassListItem first = sassList.get( 0 );
                if( !(first instanceof SassList) ) {
                    return first;
                }
                // does not destroy a map with a single entry
                if( ((SassList)first).getSeparator() != Separator.COLON ) {
                    return flatten( first );
                }
            }
            List<SassListItem> items = sassList.items;
            for( int i = 0; i < size; i++ ) {
                items.set( i, flatten( items.get( i ) ) );
            }
            return sassList;
        } else {
            return item;
        }
    }

    @Override
    public boolean containsArithmeticalOperator() {
        for (SassListItem item : this) {
            if (item.containsArithmeticalOperator()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public SassList evaluateFunctionsAndExpressions(ScssContext context,
            boolean evaluateArithmetics) {
        List<SassListItem> list = new ArrayList<SassListItem>();
        for (SassListItem item : this) {
            list.add(item.evaluateFunctionsAndExpressions(context,
                    evaluateArithmetics));
        }
        return new SassList(getSeparator(), list);
    }

    @Override
    public SassList replaceVariables(ScssContext context) {
        // The actual replacing happens in LexicalUnitImpl, which also
        // implements SassListItem.
        List<SassListItem> list = new ArrayList<SassListItem>();
        for (SassListItem item : this) {
            list.add(item.replaceVariables(context));
        }
        return new SassList(getSeparator(), list);
    }

    @Override
    public String buildString( BuildStringStrategy strategy ) {
        StringBuilder result = new StringBuilder();
        List<SassListItem> items = this.items;
        int size = items.size();
        boolean first = true;
        boolean isMap = false;
        for( int i = 0; i < size; i++ ) {
            SassListItem itemValue = items.get( i );
            String item = itemValue.buildString( strategy );
            if( item.isEmpty() ) {
                // skip empty items
                continue;
            }
            if( first ) {
                if( isMap = (itemValue.getClass() == SassList.class && ((SassList)itemValue).separator == Separator.COLON )) {
                    result.append( '(' );
                }
            } else {
                result.append( separator );
            }
            result.append( item );
            first = false;
        }
        if( isMap ) {
            result.append( ')' );
        }
        return result.toString();
    }

    public String printState() {
        return buildString(Node.PRINT_STRATEGY);
    }

    @Override
    public String toString() {
        return "SassList [" + getStringWithNesting() + "]";
    }

    /**
     * Returns a string representation of the list where nesting is indicated
     * using parentheses. Such a representation is mainly useful for debugging.
     */
    public String getStringWithNesting() {
        String result = "(";
        for (int i = 0; i < size(); i++) {
            SassListItem item = get(i);
            if (item instanceof SassList) {
                result += ((SassList) item).getStringWithNesting();
            } else {
                result += item.printState();
            }
            if (i < size() - 1) {
                result += separator;
            }
        }
        result += ")";
        return result;
    }

    @Override
    public SassList updateUrl(String prefix) {
        if (size() > 0) {
            ArrayList<SassListItem> newItems = new ArrayList<SassListItem>(
                    size());
            for (SassListItem item : this) {
                newItems.add(item.updateUrl(prefix));
            }
            SassList result = new SassList(getSeparator(), newItems);
            result.setSourcePosition(getLineNumber(), getColumnNumber());
            return result;
        } else {
            return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SassList)) {
            return false;
        }
        SassList other = (SassList) o;
        if (size() != other.size()) {
            return false;
        }
        if (size() > 1 && !getSeparator().equals(other.getSeparator())) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (!get(i).equals(other.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < size(); i++) {
            int currentHash = 0;
            if (get(i) != null) {
                currentHash = get(i).hashCode();
            }
            result = 41 * result + currentHash;
        }
        return result;
    }

    public int size() {
        return items.size();
    }

    public SassListItem get(int index) {
        return items.get(index);
    }

    @Override
    public Iterator<SassListItem> iterator() {
        return items.iterator();
    }

    protected List<SassListItem> getItems() {
        return items;
    }

    @Override
    public boolean containsVariable() {
        for (SassListItem item : this) {
            if (item.containsVariable()) {
                return true;
            }
        }
        return false;
    }
}

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
/*
 * (c) COPYRIGHT 1999 World Wide Web Consortium
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 *
 * $Id: MediaListImpl.java,v 1.4 2000/04/26 13:40:19 plehegar Exp $
 */
package com.inet.sass.parser;

import com.inet.sass.ScssContext;

/**
 * @author Philippe Le Hegaret
 */
public class MediaList {

    Object[] array = new Object[10];
    int current;

    public int getLength() {
        return current;
    }

    public String item( int index ) {
        if ((index < 0) || (index >= current)) {
            return null;
        }
        return array[index].toString();
    }

    public void addItem( String medium ) {
        addItemImpl( medium );
    }

    public void addItem( StringInterpolationSequence medium ) {
        addItemImpl( medium );
    }

    private void addItemImpl( Object medium ) {
        if (medium.equals("all")) {
            array[0] = "all";
            current = 1;
            return;
        }
        for (int i = 0; i < current; i++) {
            if (medium.equals(array[i])) {
                return;
            }
        }
        if (current == array.length) {
            Object[] old = array;
            array = new Object[current + current];
            System.arraycopy(old, 0, array, 0, current);
        }
        array[current++] = medium;
    }

    /**
     * Returns a string representation of this object.
     */
    @Override
    public String toString() {
        switch (current) {
        case 0:
            return "";
        case 1:
            return array[0].toString();
        default:
            boolean not_done = true;
            int i = 0;
            StringBuffer buf = new StringBuffer(50);
            do {
                buf.append(array[i++]);
                if (i == current) {
                    not_done = false;
                } else {
                    buf.append(", ");
                }
            } while (not_done);
            return buf.toString();
        }
    }

    public void replaceVariables( ScssContext context ) {
        for( int i = 0; i < current; i++ ) {
            Object medium = array[i];
            if( medium.getClass() == StringInterpolationSequence.class ) {
                array[i] = ((StringInterpolationSequence)medium).replaceVariables( context );
            }
        }
    }
}

/*
 * Copyright 2023 i-net software
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
 * Copyright (c) 1999 World Wide Web Consortium
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 *
 */
package com.inet.sass.tree;

public interface SourceLocation {

    /**
     * Return the URI for the current document event.
     * <p>
     * The parser must resolve the URI fully before passing it to the application.
     * </p>
     * @return A string containing the URI, or null if none is available.
     */
    public String getUri();

    /**
     * Return the line number where the current document event ends. Note that this is the line position of the first character after the text associated with the document event.
     * @return The line number, or -1 if none is available.
     * @see #getColumnNumber
     */
    public int getLineNumber();

    /**
     * Return the column number where the current document event ends. Note that this is the column number of the first character after the text associated with the document event. The first column in
     * a line is position 1.
     * @return The column number, or -1 if none is available.
     * @see #getLineNumber
     */
    public int getColumnNumber();
}

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
package com.inet.sass.handler;

public abstract class SCSSErrorHandler {

    private static ThreadLocal<SCSSErrorHandler> current = new ThreadLocal<SCSSErrorHandler>();

    public static void set(SCSSErrorHandler h) {
        current.set(h);
    }

    public static SCSSErrorHandler get() {
        return current.get();
    }

    public void error( Throwable th ) {
    }

    public void error( String msg ) {
    }

    public void warning( Throwable obj ) {
    }

    public void warning( String msg ) {
    }

    public void debug( String msg ) {
    }
}

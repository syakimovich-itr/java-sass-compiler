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
package com.inet.sass.resolver;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

import com.inet.sass.InputSource;
import com.inet.sass.ScssStylesheet;

public class FilesystemResolver extends AbstractResolver {

    private Charset  cs;
    private String[] customPaths;

    public FilesystemResolver( Charset cs, String... customPaths ) {
        this.cs = cs;
        this.customPaths = customPaths;
    }

    @Override
    protected List<String> getPotentialParentPaths( ScssStylesheet parentStyleSheet, String identifier ) {
        List<String> potentialPaths = super.getPotentialParentPaths( parentStyleSheet, identifier );
        if( customPaths != null ) {
            for( String path : customPaths ) {
                potentialPaths.add( extractFullPath( path, identifier ) );
            }
        }

        return potentialPaths;
    }

    @Override
    public InputSource resolveNormalized( String identifier ) {
        String fileName = identifier;
        if( !fileName.endsWith( ".css" ) && !fileName.endsWith( ".png" ) ) {
            fileName += ".scss";
        }

        try {
            InputStream is = new FileInputStream( fileName );
            InputSource source = new InputSource();
            source.setByteStream( is ); // for images
            source.setCharacterStream( new BufferedReader( new InputStreamReader( is, cs ) ) );
            source.setURI( fileName );
            return source;

        } catch( FileNotFoundException e ) {
            // not found, try something else
            return null;
        }
    }
}

/*
 * Copyright 2023 i-net software
 * Copyright (c) 1999 World Wide Web Consortium
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 *
 * $Id: InputSource.java,v 1.2 1999/09/26 10:09:48 plehegar Exp $
 */
package com.inet.sass;

import java.io.InputStream;
import java.io.Reader;

/**
 * A single input source for a CSS source.
 * <p>
 * This class allows a CSS application to encapsulate information about an input source in a single object, which may include a URI, a byte stream (possibly with a specified encoding), and/or a
 * character stream.
 * </p>
 * <p>
 * The CSS parser will use the InputSource object to determine how to read CSS input. If there is a character stream available, the parser will read that stream directly; if not, the parser will use a
 * byte stream, if available; if neither a character stream nor a byte stream is available, the parser will attempt to open a URI connection to the resource identified by the URI.
 * </p>
 * <p>
 * An InputSource object belongs to the application: the CSS parser shall never modify it in any way (it may modify a copy if necessary).
 * </p>
 * @author Philippe Le Hegaret
 */
public class InputSource {

    private String      uri;
    private InputStream byteStream;
    private Reader      characterStream;

    /**
     * Zero-argument default constructor.
     * @see #setURI
     * @see #setByteStream
     * @see #setCharacterStream
     */
    public InputSource() {
    }

    /**
     * Create a new input source with a character stream.
     * <p>
     * Application writers may use setURI() to provide a base for resolving relative URIs, and setPublicId to include a public identifier.
     * </p>
     * <p>
     * The character stream shall not include a byte order mark.
     * </p>
     * @param characterStream the stream
     * @see #setURI
     * @see #setByteStream
     * @see #setCharacterStream
     */
    public InputSource( Reader characterStream ) {
        setCharacterStream( characterStream );
    }

    /**
     * Set the URI for this input source.
     * <p>
     * The URI is optional if there is a byte stream or a character stream, but it is still useful to provide one, since the application can use it to resolve relative URIs and can include it in error
     * messages and warnings (the parser will attempt to open a connection to the URI only if there is no byte stream or character stream specified).
     * </p>
     * <p>
     * If the application knows the character encoding of the object pointed to by the URI, it can register the encoding using the setEncoding method.
     * </p>
     * <p>
     * The URI must be fully resolved.
     * </p>
     * @param uri The URI as a string.
     * @see #getURI
     */
    public void setURI( String uri ) {
        this.uri = uri;
    }

    /**
     * Get the URI for this input source.
     * <p>
     * The getEncoding method will return the character encoding of the object pointed to, or null if unknown.
     * </p>
     * <p>
     * The URI will be fully resolved.
     * </p>
     * @return The URI.
     * @see #setURI
     */
    public String getURI() {
        return uri;
    }

    /**
     * Set the byte stream for this input source.
     * <p>
     * The SAX parser will ignore this if there is also a character stream specified, but it will use a byte stream in preference to opening a URI connection itself.
     * </p>
     * <p>
     * If the application knows the character encoding of the byte stream, it should set it with the setEncoding method.
     * </p>
     * @param byteStream A byte stream containing an CSS document or other entity.
     * @see #getByteStream
     */
    public void setByteStream( InputStream byteStream ) {
        this.byteStream = byteStream;
    }

    /**
     * Get the byte stream for this input source.
     * <p>
     * The getEncoding method will return the character encoding for this byte stream, or null if unknown.
     * </p>
     * @return The byte stream, or null if none was supplied.
     * @see #setByteStream
     */
    public InputStream getByteStream() {
        return byteStream;
    }

    /**
     * Set the character stream for this input source.
     * <p>
     * If there is a character stream specified, the SAX parser will ignore any byte stream and will not attempt to open a URI connection to the URI.
     * </p>
     * @param characterStream The character stream containing the CSS document or other entity.
     * @see #getCharacterStream
     */
    public void setCharacterStream( Reader characterStream ) {
        this.characterStream = characterStream;
    }

    /**
     * Get the character stream for this input source.
     * @return The character stream, or null if none was supplied.
     * @see #setCharacterStream
     */
    public Reader getCharacterStream() {
        return characterStream;
    }
}

package com.inet.sass.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class ScssLookAheadReader {

    private final Reader        reader;

    private String              uri;

    private final StringBuilder cache = new StringBuilder();

    private StringReader        cache2;

    private int                 cachePos;

    private int                 column;

    private int                 line;

    ScssLookAheadReader( Reader reader, String uri ) {
        this.uri = uri;
        this.reader = reader.markSupported() ? reader : new BufferedReader( reader );
    }

    /**
     * Read the next character in the method nextBlockMarker()
     * @return next char
     * @throws IOException if an I/O error occur
     */
    private int readCharBlockMarker() throws IOException {
        if( cache2 != null ) {
            int ch = cache2.read();
            if( ch != -1 ) {
                return ch;
            }
            cache2 = null;
        }
        return reader.read();
    }

    /**
     * The type of the next code construct
     * @return one of the characters "{};:" or -1
     */
    int nextBlockMarker() {
        try {
            if( cachePos < cache.length() ) {
                if( cache2 != null ) {
                    for( ;; ) {
                        int ch = cache2.read();
                        if( ch < 0 ) {
                            break;
                        }
                        cache.setCharAt( cachePos, (char)ch );
                    }
                }
                String str = cache.substring( cachePos );
                cache2 = new StringReader( str );
            }
            cache.setLength( cachePos = 0 );

            int parenthesis = 0;
            boolean isSlash = false;
            for( ;; ) {
                int ch = readCharBlockMarker();
                if( ch < 0 ) {
                    for( int i = 0; i < cache.length(); i++ ) {
                        if( !Character.isWhitespace( cache.charAt( i ) ) ) {
                            return ';'; // a not terminated line is like a lime with semicolon
                        }
                    }
                    return -1;
                }

                cache.append( (char)ch );
                switch( ch ) {
                    case '/':
                        if( isSlash ) {
                            if( parenthesis > 0 && cache.indexOf( "url" ) > 0 ) {
                                break; // url function with unquoted url like url(http://xyz)
                            }
                            cache.setLength( cache.length() - 2 ); // remove the slashes
                            do {
                                ch = readCharBlockMarker();
                            } while( ch != '\n' && ch != -1 );
                            cache.append( '\n' );
                        }
                        break;
                    case '*':
                        if( isSlash ) {
                            boolean isAsterix = false;
                            for( ;; ) {
                                ch = reader.read();
                                switch( ch ) {
                                    case -1:
                                        throw createException( "Unexpected end of Scss data" );
                                }
                                cache.append( (char)ch );
                                if( ch == '/' && isAsterix ) {
                                    ch = 0;
                                    break;
                                }
                                isAsterix = ch == '*';
                            }
                        }
                        break;
                    case '{':
                        if( cache.charAt( cache.length() - 2 ) == '#' ) {
                            // interpolation
                            for( ;; ) {
                                ch = readCharBlockMarker();
                                switch( ch ) {
                                    case -1:
                                        throw createException( "Unexpected end of Scss data" );
                                }
                                cache.append( (char)ch );
                                if( ch == '}' ) {
                                    ch = 0;
                                    break;
                                }
                            }
                            break;
                        }
                        for( int i = cache.length() - 2; i > 0; i-- ) {
                            ch = cache.charAt( i );
                            if( ch <= ' ' ) {
                                continue;
                            }
                            if( ch == ':' ) {
                                return ':'; // nested property
                            }
                            break;
                        }
                        return '{';
                    case '}':
                    case ';':
                        if( parenthesis == 0 ) {
                            return ch;
                        }
                        break;
                    case '(':
                        parenthesis++;
                        break;
                    case ')':
                        if( --parenthesis < 0 ) {
                            throw createException( "Unrecognized input: '" + cache.toString().trim() + "'" );
                        }
                        break;
                    case '"':
                    case '\'':
                        isSlash = false;
                        for( ;; ) {
                            int ch2 = readCharBlockMarker();
                            switch( ch2 ) {
                                case -1:
                                case '\n':
                                case '\r':
                                    throw createException( "Missing string quote: " + (char)ch );
                            }
                            cache.append( (char)ch2 );
                            if( ch == ch2 && !isSlash ) {
                                break;
                            }
                            isSlash = ch2 == '\\' && !isSlash;
                        }
                        break;
                }
                isSlash = ch == '/';
            }
        } catch( IOException ex ) {
            throw createException( ex );
        }
    }

    String getLookAhead() {
        return cache.toString();
    }

    char read() {
        try {
            if( cachePos < cache.length() ) {
                return incLineColumn( cache.charAt( cachePos++ ) );
            }
            int ch = reader.read();
            if( ch == -1 ) {
                throw createException( "Unexpected end of Scss data" );
            }
            if( ch == '/' ) {
                int ch2 = reader.read();
                switch( ch2 ) {
                    case -1:
                        break;
                    case '/':
                        do {
                            ch = reader.read();
                        } while( ch != '\n' && ch != -1 );
                        ch = '\n';
                        break;
                    default:
                        back( (char)ch2 );
                }
            }
            return incLineColumn( ch );
        } catch( IOException ex ) {
            throw createException( ex );
        }
    }

    /**
     * Push a char back to the stream
     * @param ch the char
     */
    void back( char ch ) {
        if( cachePos > 0 ) {
            cachePos--;
            cache.setCharAt( cachePos, ch );
        } else {
            cache.insert( 0, ch );
        }
        column--; // reverse of incLineColumn()
    }

    /**
     * Push the string back to the stream
     * @param str the characters
     */
    void back( CharSequence str ) {
        int length = str.length();
        for( int i = length - 1; i >= 0; i-- ) {
            back( str.charAt( i ) );
        }
    }

    /**
     * Increment the line and column count depending on the character.
     * @param ch current character
     * @return the character parameter
     */
    private char incLineColumn( int ch ) {
        if( ch == '\n' ) {
            line++;
            column = 0;
        } else {
            column++;
        }
        return (char)ch;
    }

    int getLine() {
        return line + 1;
    }

    int getColumn() {
        return column + 1;
    }

    ParseException createException( Throwable th ) {
        ParseException pex = new ParseException( th.toString(), uri, getLine(), getColumn() );
        pex.initCause( th );
        return pex;
    }

    ParseException createException( String msg ) {
        return new ParseException( msg, uri, getLine(), getColumn() );
    }
}

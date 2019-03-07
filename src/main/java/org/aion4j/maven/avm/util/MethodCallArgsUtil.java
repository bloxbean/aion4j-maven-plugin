package org.aion4j.maven.avm.util;

import org.aion.avm.core.util.Helpers;
import org.aion4j.maven.avm.exception.MethodArgsParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MethodCallArgsUtil {

    public static Object[] parseMethodArgs(String argsString) throws Exception {
        if(argsString == null || argsString.isEmpty())
            return new Object[0];

        String[] tokens = translateCommandline(argsString);

        List<Object> args = new ArrayList<>();

        boolean isType = true;
        String type = null;
        for(String token: tokens) {
            if(isType) {
                type = token;
                isType = false;
            } else {
                isType = true;

                if(type != null) {
                    if(type.equals("-I")) args.add(Integer.parseInt(token));
                    else if(type.equals("-J")) args.add(Long.valueOf(token));
                    else if(type.equals("-S")) args.add(Short.valueOf(token));
                    else if(type.equals("-C")) args.add(Character.valueOf(token.charAt(0)));
                    else if(type.equals("-F")) args.add(Float.valueOf(token));
                    else if(type.equals("-D")) args.add(Double.valueOf(token));
                    else if(type.equals("-B")) args.add(Byte.valueOf(token));
                    else if(type.equals("-Z")) args.add(Boolean.valueOf(token));
                    else if(type.equals("-A")) args.add(new org.aion.avm.api.Address(Helpers.hexStringToBytes(token)));
                    else if(type.equals("-T")) args.add(token);
                }
            }
        }

        return args.toArray();
    }

    /**
     * Taken from maven-shared-utils package
     * @param toProcess The command line to translate.
     * @return The array of translated parts.
     * @throws MethodArgsParseException in case of unbalanced quotes.
     */
    public static String[] translateCommandline( String toProcess ) throws MethodArgsParseException
    {
        if ( ( toProcess == null ) || ( toProcess.length() == 0 ) )
        {
            return new String[0];
        }

        // parse with a simple finite state machine

        final int normal = 0;
        final int inQuote = 1;
        final int inDoubleQuote = 2;
        boolean inEscape = false;
        int state = normal;
        final StringTokenizer tok = new StringTokenizer( toProcess, "\"\' \\", true );
        List<String> tokens = new ArrayList<String>();
        StringBuilder current = new StringBuilder();

        while ( tok.hasMoreTokens() )
        {
            String nextTok = tok.nextToken();
            switch ( state )
            {
                case inQuote:
                    if ( "\'".equals( nextTok ) )
                    {
                        if ( inEscape )
                        {
                            current.append( nextTok );
                            inEscape = false;
                        }
                        else
                        {
                            state = normal;
                        }
                    }
                    else
                    {
                        current.append( nextTok );
                        inEscape = "\\".equals( nextTok );
                    }
                    break;
                case inDoubleQuote:
                    if ( "\"".equals( nextTok ) )
                    {
                        if ( inEscape )
                        {
                            current.append( nextTok );
                            inEscape = false;
                        }
                        else
                        {
                            state = normal;
                        }
                    }
                    else
                    {
                        current.append( nextTok );
                        inEscape = "\\".equals( nextTok );
                    }
                    break;
                default:
                    if ( "\'".equals( nextTok ) )
                    {
                        if ( inEscape )
                        {
                            inEscape = false;
                            current.append( nextTok );
                        }
                        else
                        {
                            state = inQuote;
                        }
                    }
                    else if ( "\"".equals( nextTok ) )
                    {
                        if ( inEscape )
                        {
                            inEscape = false;
                            current.append( nextTok );
                        }
                        else
                        {
                            state = inDoubleQuote;
                        }
                    }
                    else if ( " ".equals( nextTok ) )
                    {
                        if ( current.length() != 0 )
                        {
                            tokens.add( current.toString() );
                            current.setLength( 0 );
                        }
                    }
                    else
                    {
                        current.append( nextTok );
                        inEscape = "\\".equals( nextTok );
                    }
                    break;
            }
        }

        if ( current.length() != 0 )
        {
            tokens.add( current.toString() );
        }

        if ( ( state == inQuote ) || ( state == inDoubleQuote ) )
        {
            throw new MethodArgsParseException( "unbalanced quotes in " + toProcess );
        }

        return tokens.toArray( new String[tokens.size()] );
    }

    public static void main(String[] args) throws Exception {

        String argsStr = "-A 0x1122334455667788112233445566778811223344556677881122334455667788 -J 100 -I 45 -B -1 -T hello";

        Object[] objs = parseMethodArgs(argsStr);

        for(Object obj: objs) {
            System.out.println(obj);
            System.out.println(obj.getClass());
        }
    }
}

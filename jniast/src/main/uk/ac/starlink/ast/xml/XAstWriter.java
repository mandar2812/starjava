package uk.ac.starlink.ast.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Stack;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.ac.starlink.ast.AstObject;
import uk.ac.starlink.ast.Channel;
import uk.ac.starlink.util.SourceReader;


/**
 * Provides a method of getting the XML representation for an AstObject.
 *
 * @author Mark Taylor
 * @author Peter W. Draper
 */
public class XAstWriter {

    /**
     * Turns an AstObject into an Element.
     *
     * @param   obj  the AstObject to be serialised
     * @param   prefix the namespace prefix for elements and
     *                 attributes, null for none (include :)
     * @return  an Element representing <tt>obj</tt>
     */
    public Element makeElement( AstObject obj, String prefix ) {
        ChannelReader chan;
        try {
            chan = new ChannelReader( prefix );
        }
        catch ( ParserConfigurationException e ) {
            throw (Error) new AssertionError( e.getMessage() ).initCause( e );
        }
        chan.setComment( false );
        chan.setFull( -1 );
        try {
            chan.write( obj );
        }
        catch ( IOException e ) {
            throw (Error) new AssertionError( e.getMessage() ).initCause( e );
        }
        return chan.readElement();
    }

    /**
     * Turns an AstObject into an XML Source.
     *
     * @param   obj     the AstObject to be serialised
     * @param   prefix  the namespace prefix for elements and attributes,
     *                  null for none (include :)
     * @return  a Source representing <tt>obj</tt>
     */
    public Source makeSource( AstObject obj, String prefix ) {
        Element el = makeElement( obj, prefix );
        return new DOMSource( el );
    }

    /**
     * Convenience method to write the XML representation of an AstObject
     * to an output stream.
     *
     * @param  obj  the AstObject to write
     * @param  out  the stream to write the XML to
     * @throws  IOException  if there is some I/O error
     */
    public static void trace( AstObject obj, OutputStream out )
            throws IOException {
        SourceReader sr = new SourceReader().setIncludeDeclaration( false )
                                            .setIndent( 2 );
        Source xsrc = new XAstWriter().makeSource( obj, null );
        try {
            sr.writeSource( xsrc, out );
        }
        catch ( TransformerException e ) {
            throw (IOException) new IOException( e.getMessage() )
                               .initCause( e );
        }
    }

    /*
     * Handles the work of turning an AstObject into XML.
     */
    private static class ChannelReader extends Channel {

        private Document doc;
        private Stack eStack = new Stack();
        private String nextLabel;
        private Element topEl;
        private String prefix = null;

        // Instance versions of fixed names (may need namespace
        // qualification).
        private String attributeName = XAstNames.ATTRIBUTE;
        private String isaName = XAstNames.ISA;
        private String labelName = XAstNames.LABEL;
        private String nameName = XAstNames.NAME;
        private String valueName = XAstNames.VALUE;
        private String className = XAstNames.CLASS;
        private String quotedName = XAstNames.QUOTED;

        ChannelReader( String prefix ) throws ParserConfigurationException {
            this.prefix = prefix;
            if ( prefix != null ) {
                attributeName = prefix + attributeName;
                isaName = prefix + isaName;
                labelName = prefix + labelName;
                nameName = prefix + nameName;
                valueName = prefix + valueName;
                className = prefix + className;
                quotedName = prefix + quotedName;
            }
            doc = DocumentBuilderFactory.newInstance()
                 .newDocumentBuilder().newDocument();
        }

        /*
         * Gets an element which has been read from the channel.
         * A read call must have been made on this channel prior to calling
         * this method.
         */
        synchronized Element readElement() {
            return topEl;
        }

        /*
         * Takes a line in AST text channel format and uses it to
         * construct the next bit of the XML element.
         */
        protected synchronized void sink( String line ) throws IOException {

            /* Get the two or three tokens consituting the line. */
            line = line.trim();
            int s1 = line.indexOf( ' ' );
            int s2 = line.indexOf( ' ', s1 + 1);
            if ( s2 == -1 ) s2 = line.length();
            String w1 = line.substring( 0, s1 );
            String w2 = line.substring( s1 + 1, s2 );
            String w3 = line.substring( s2 ).trim();

            /* Element start. */
            if ( w1.equals( "Begin" ) ) {
                if ( prefix != null ) w2 = prefix + w2;
                Element el = doc.createElement( w2 );
                if ( eStack.empty() ) {
                    topEl = el;
                }
                else {
                    el.setAttribute( labelName, nextLabel );
                }
                eStack.push( el );
            }

            /* Element end. */
            else if ( w1.equals( "End" ) ) {
                Element child = (Element) eStack.pop();
                if ( ! eStack.empty() ) {
                    Element parent = (Element) eStack.peek();
                    parent.appendChild( child );
                }
            }

            /* Attribute setting. */
            else if ( w2.equals( "=" ) && ! w3.equals( "" ) ) {
                Element attel = doc.createElement( attributeName );
                String attName = w1;
                String attValue = w3;
                boolean quoted = false;
                int leng = attValue.length();
                if ( attValue.charAt( 0 ) == '"' &&
                     attValue.charAt( leng - 1 ) == '"' ) {
                    quoted = true;
                    attValue = attValue.substring( 1, leng - 1 );
                }
                attel.setAttribute( nameName, attName );
                attel.setAttribute( valueName, attValue );
                if ( quoted ) {
                    attel.setAttribute( quotedName, "true" );
                }
                ( (Element) eStack.peek() ).appendChild( attel );
            }

            /* Introduce child element. */
            else if ( w2.equals( "=" ) && w3.equals( "" ) ) {
                nextLabel = w1;
            }

            /* Superclass membership. */
            else if ( w1.equals( "IsA" ) ) {
                Element isael = doc.createElement( isaName );
                isael.setAttribute( className, w2 );
                ( (Element) eStack.peek() ).appendChild( isael );
            }
        }
    }
}

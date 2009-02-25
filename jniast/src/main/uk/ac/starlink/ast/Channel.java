/* ********************************************************
 * This file automatically generated by Channel.pl.
 *                   Do not edit.                         *
 **********************************************************/

package uk.ac.starlink.ast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Java interface to the AST Channel class
 *  - basic (textual) I/O channel. 
 * <p>
 *       This class is used for reading and writing AST objects from/to
 *       external media.  The <code>Channel</code> class itself reads
 *       from <code>System.in</code> or another <code>InputStream</code>
 *       and writes to <code>System.out</code> or another 
 *       <code>OutputStream</code>.
 *       To perform I/O to some other object, extend this clas and
 *       override the <code>source</code> and <code>sink</code> methods.
 *    
 * 
 * @see  <a href='http://star-www.rl.ac.uk/cgi-bin/htxserver/sun211.htx/?xref_Channel'>AST Channel</a>  
 */

public class Channel extends AstObject {

    /* Holds the C pointer to a data structure used by native code. */
    private long chaninfo;

    private BufferedReader inreader;
    private OutputStream outstream;

    /**
     * Creates a channel which reads from the given <code>InputStream</code>
     * and writes to the given <code>OutputStream</code>.
     *
     * @param   in   a stream to read AST objects from.  If <code>null</code>,
     *               then <code>System.in</code> is used.
     * @param   out  a stream to write AST objects to.  If <code>null</code>,
     *               then <code>System.out</code> is used.
     */
    public Channel( InputStream in, OutputStream out ) {
        if ( in == null ) {
            in = System.in;
        }
        if ( out == null ) {
            out = System.out;
        }
        inreader = new BufferedReader( new InputStreamReader( in ) );
        outstream = out;
        construct();
    }

    /**
     * This constructor does not do all the required construction to
     * create a valid Channel object, but is required for inheritance
     * by user subclasses of Channel.
     */
    protected Channel() {
        construct();
    }

    /**
     * This is a dummy constructor which does nothing at all.  It is invoked
     * by the FitsChan constructor to prevent it having to use the
     * no-argument constructor which does things FitsChan does not want.
     */
    protected Channel( Channel dummy ) {
    }

    /**
     * Finalizes the object.  Certain resources allocated by the native
     * code are freed, and the finalizer of the superclass is called.
     */
    public void finalize() throws Throwable {
        try {
            destroy();
        }
        finally {
            super.finalize();
        }
    }

    /**
     * Reads a <code>String</code> which forms one line of the textual
     * representation of an AST object from the channel's input stream.
     * If the end of the stream is reached, <code>null</code> is returned.
     * If an <code>IOException</code> occurs during the reading,
     * it is thrown.
     * <p>
     * This method is called by the <code>read</code> method.
     * To implement a channel which reads from a source other than
     * an <code>InputStream</code>, override this method.  The method
     * should return <code>null</code> when there is no more input,
     * and may throw an IOException in case of error.
     *
     * @return  a line of text read from the input stream, as a
     *          <code>String</code>.  If the end of the stream has been
     *          reached, <code>null</code> is returned.
     * @throws  IOException  if an I/O error occurs during reading
     */
    protected String source() throws IOException {
        return inreader.readLine();
    }

    /**
     * Writes a <code>String</code> which forms one line of the textual
     * representation of an AST object to the channel's output stream.
     * If an <code>IOException</code> occurs during the writing,
     * it is thrown.
     * <p>
     * This method is called by the <code>write</code> method.
     * To implement a channel which writes to a source other than
     * an <code>OutputStream</code>, override this method.  The method
     * can do anything it likes with its argument, and may throw
     * an exception in case of error.
     *
     * @param  line  a <code>String</code> which forms one line of the
     *         textual description of an AST object which is being
     *         written.
     * @throws IOException  if an I/O error occurs during writing.
     */
    protected void sink( String line ) throws IOException {
        outstream.write( line.getBytes() );
        outstream.write( (byte) '\n' );
    }

    /**
     * Performs native operations required for construction of a valid
     * Channel.
     */
    private void construct() {
        if ( this instanceof XmlChan ) {
            constructXmlChan();
        }
        else {
            constructChannel();
        }
    }

    private native void constructChannel();
    private native void constructXmlChan();
    private native void destroy();

    /**
     * Reads an AST object from this channel.  The <code>source</code>
     * method is invoked to obtain the textual representation.
     *
     * @return   the <code>AstObject</code> which has been read.
     *           <code>null</code> is returned, without error, if no
     *           further objects remain to be read on the stream
     * @throws   IOException  if such an exception was generated by the
     *                        <code>source</code> method
     * @throws   AstException  if an error occurs in the AST library
     */
    public native AstObject read() throws IOException;

    /**
     * Writes an AST object to this channel.  The <code>sink</code>
     * method is invoked to send the textual representation.
     *
     * @param    obj  an <code>AstObject</code> to be written
     * @return   number of objects written (1 on success)
     * @throws   IOException   if such an exception was generated by the
     *                         <code>sink</code> method
     * @throws   AstException  if an error occurs in the AST library
     */
    public native int write( AstObject obj ) throws IOException;

    /**
     * This method is currently unsupported for Channel and its subclasses
     * because of difficulties in its implementation, and because it is
     * probably not that useful.
     *
     * @throws   UnsupportedOperationException
     */
    public AstObject copy() {
        /* It might not be all that hard to support, but you'd need to
         * make sure that the native data structures were filled appropriately.
         * Leave implementation until someone actually wants to use it. */
        throw new UnsupportedOperationException(
            "Sorry - copy not currently supported for Channels" );
    }

    /**
     * Get 
     * include textual comments in output.  
     * This is a boolean attribute which controls whether textual
     * comments are to be included in the output generated by a
     * Channel. If included, they will describe what each item of
     * output represents.
     * <p>
     * If Comment is non-zero, then comments will be included. If 
     * it is zero, comments will be omitted.
     * 
     *
     * @return  this object's Comment attribute
     */
    public boolean getComment() {
        return getB( "Comment" );
    }

    /**
     * Set 
     * include textual comments in output.  
     * This is a boolean attribute which controls whether textual
     * comments are to be included in the output generated by a
     * Channel. If included, they will describe what each item of
     * output represents.
     * <p>
     * If Comment is non-zero, then comments will be included. If 
     * it is zero, comments will be omitted.
     * 
     *
     * @param  comment   the Comment attribute of this object
     */
    public void setComment( boolean comment ) {
       setB( "Comment", comment );
    }

    /**
     * Get 
     * set level of output detail.  
     * This attribute is a three-state flag and takes values of -1, 0
     * or +1.  It controls the amount of information included in the
     * output generated by a Channel.
     * <p>
     * If Full is zero, then a modest amount of
     * non-essential but useful information will be included in the
     * output. If Full is negative, all non-essential information will
     * be suppressed to minimise the amount of output, while if it is
     * positive, the output will include the maximum amount of detailed
     * information about the Object being written.
     * <h4>Notes</h4>
     * <br> - All positive values supplied for this attribute are converted
     * to +1 and all negative values are converted to -1.
     *
     * @return  this object's Full attribute
     */
    public int getFull() {
        return getI( "Full" );
    }

    /**
     * Set 
     * set level of output detail.  
     * This attribute is a three-state flag and takes values of -1, 0
     * or +1.  It controls the amount of information included in the
     * output generated by a Channel.
     * <p>
     * If Full is zero, then a modest amount of
     * non-essential but useful information will be included in the
     * output. If Full is negative, all non-essential information will
     * be suppressed to minimise the amount of output, while if it is
     * positive, the output will include the maximum amount of detailed
     * information about the Object being written.
     * <h4>Notes</h4>
     * <br> - All positive values supplied for this attribute are converted
     * to +1 and all negative values are converted to -1.
     *
     * @param  full   the Full attribute of this object
     */
    public void setFull( int full ) {
       setI( "Full", full );
    }

    /**
     * Get 
     * determines which read/write conditions are reported.  
     * This attribute determines which, if any, of the conditions that occur
     * whilst reading or writing an Object should be reported. These
     * conditions will generate either a fatal error or a warning, as
     * determined by attribute Strict. ReportLevel can take any of the
     * following values:
     * <p>
     * 0 - Do not report any conditions.
     * <p>
     * 1 - Report only conditions where significant information content has been
     * changed. For instance, an unsupported time-scale has been replaced by a
     * supported near-equivalent time-scale. Another example is if a basic
     * Channel unexpected encounters data items that may have been introduced 
     * by later versions of AST.
     * <p>
     * 2 - Report the above, and in addition report significant default
     * values. For instance, if no time-scale was specified when reading an
     * Object from an external data source, report the default time-scale
     * that is being used.
     * <p>
     * 2 - Report the above, and in addition report any other potentially
     * interesting conditions that have no significant effect on the
     * conversion. For instance, report if a time-scale of "TT"
     * (terrestrial time) is used in place of "ET" (ephemeris time). This
     * change has no signficiant effect because ET is the predecessor of, 
     * and is continuous with, TT. Synonyms such as "IAT" and "TAI" are
     * another example.
     * <p>
     * The default value is 1. Note, there are many other conditions that
     * can occur whilst reading or writing an Object that completely
     * prevent the conversion taking place. Such conditions will always 
     * generate errors, irrespective of the ReportLevel and Strict attributes.
     * 
     *
     * @return  this object's ReportLevel attribute
     */
    public int getReportLevel() {
        return getI( "ReportLevel" );
    }

    /**
     * Set 
     * determines which read/write conditions are reported.  
     * This attribute determines which, if any, of the conditions that occur
     * whilst reading or writing an Object should be reported. These
     * conditions will generate either a fatal error or a warning, as
     * determined by attribute Strict. ReportLevel can take any of the
     * following values:
     * <p>
     * 0 - Do not report any conditions.
     * <p>
     * 1 - Report only conditions where significant information content has been
     * changed. For instance, an unsupported time-scale has been replaced by a
     * supported near-equivalent time-scale. Another example is if a basic
     * Channel unexpected encounters data items that may have been introduced 
     * by later versions of AST.
     * <p>
     * 2 - Report the above, and in addition report significant default
     * values. For instance, if no time-scale was specified when reading an
     * Object from an external data source, report the default time-scale
     * that is being used.
     * <p>
     * 2 - Report the above, and in addition report any other potentially
     * interesting conditions that have no significant effect on the
     * conversion. For instance, report if a time-scale of "TT"
     * (terrestrial time) is used in place of "ET" (ephemeris time). This
     * change has no signficiant effect because ET is the predecessor of, 
     * and is continuous with, TT. Synonyms such as "IAT" and "TAI" are
     * another example.
     * <p>
     * The default value is 1. Note, there are many other conditions that
     * can occur whilst reading or writing an Object that completely
     * prevent the conversion taking place. Such conditions will always 
     * generate errors, irrespective of the ReportLevel and Strict attributes.
     * 
     *
     * @param  reportLevel   the ReportLevel attribute of this object
     */
    public void setReportLevel( int reportLevel ) {
       setI( "ReportLevel", reportLevel );
    }

    /**
     * Get 
     * skip irrelevant data.  
     * This is a boolean attribute which indicates whether the Object
     * data being read through a Channel are inter-mixed with other,
     * irrelevant, external data.
     * <p>
     * If Skip is zero (the default), then the source of input data is
     * expected to contain descriptions of AST Objects and comments and
     * nothing else (if anything else is read, an error will
     * result). If Skip is non-zero, then any non-Object data
     * encountered between Objects will be ignored and simply skipped
     * over in order to reach the next Object.
     * 
     *
     * @return  this object's Skip attribute
     */
    public boolean getSkip() {
        return getB( "Skip" );
    }

    /**
     * Set 
     * skip irrelevant data.  
     * This is a boolean attribute which indicates whether the Object
     * data being read through a Channel are inter-mixed with other,
     * irrelevant, external data.
     * <p>
     * If Skip is zero (the default), then the source of input data is
     * expected to contain descriptions of AST Objects and comments and
     * nothing else (if anything else is read, an error will
     * result). If Skip is non-zero, then any non-Object data
     * encountered between Objects will be ignored and simply skipped
     * over in order to reach the next Object.
     * 
     *
     * @param  skip   the Skip attribute of this object
     */
    public void setSkip( boolean skip ) {
       setB( "Skip", skip );
    }

    /**
     * Get 
     * report an error if any unexpeted data items are found.  
     * This is a boolean attribute which indicates whether a warning
     * rather than an error should be issed for insignificant conversion
     * problems. If it is set non-zero, then fatal errors are issued
     * instead of warnings, resulting in the 
     * AST error status being set.
     * If Strict is zero (the default), then execution continues after minor
     * conversion problems, and a warning message is added to the Channel
     * structure. Such messages can be retrieved using the 
     * astWarnings
     * function.
     * <h4>Notes</h4>
     * <br> - This attribute was introduced in AST version 5.0. Prior to this
     * version of AST unexpected data items read by a basic Channel always 
     * caused an error to be reported. So applications linked against 
     * versions of AST prior to version 5.0 may not be able to read Object 
     * descriptions created by later versions of AST, if the Object's class 
     * description has changed.
     * 
     *
     * @return  this object's Strict attribute
     */
    public boolean getStrict() {
        return getB( "Strict" );
    }

    /**
     * Set 
     * report an error if any unexpeted data items are found.  
     * This is a boolean attribute which indicates whether a warning
     * rather than an error should be issed for insignificant conversion
     * problems. If it is set non-zero, then fatal errors are issued
     * instead of warnings, resulting in the 
     * AST error status being set.
     * If Strict is zero (the default), then execution continues after minor
     * conversion problems, and a warning message is added to the Channel
     * structure. Such messages can be retrieved using the 
     * astWarnings
     * function.
     * <h4>Notes</h4>
     * <br> - This attribute was introduced in AST version 5.0. Prior to this
     * version of AST unexpected data items read by a basic Channel always 
     * caused an error to be reported. So applications linked against 
     * versions of AST prior to version 5.0 may not be able to read Object 
     * descriptions created by later versions of AST, if the Object's class 
     * description has changed.
     * 
     *
     * @param  strict   the Strict attribute of this object
     */
    public void setStrict( boolean strict ) {
       setB( "Strict", strict );
    }

}

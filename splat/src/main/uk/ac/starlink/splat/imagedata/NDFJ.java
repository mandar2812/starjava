/*
 * Copyright (C) 2003 Central Laboratory of the Research Councils
 *
 *  History:
 *     02-SEP-1999 (Peter W. Draper):
 *       Original version.
 */
package uk.ac.starlink.splat.imagedata;

import java.io.Serializable;
import java.util.logging.Logger;

import uk.ac.starlink.ast.FrameSet;
import uk.ac.starlink.ast.Channel;
import uk.ac.starlink.splat.ast.ASTJ;
import uk.ac.starlink.splat.ast.ASTChannel;
import uk.ac.starlink.util.Loader;

import uk.ac.starlink.splat.util.Utilities;

/**
 * Java interface for accessing data stored in NDFs.
 *
 * @author Peter W. Draper
 * @version $Id$
 */
public class NDFJ 
    implements Serializable
{
    //
    //  =========================
    //  Constructors and finalize
    //  =========================
    //

    /**
     *  Default constructor.
     */
    public NDFJ()
        throws UnsupportedOperationException
    {
        // Do nothing.
    }

    /**
     *  Construct from an opened NDF.
     *
     *  @param ident NDF identifier.
     */
    public NDFJ( int ident )
    {
        if ( supported ) {
            initialize( ident );
        }
    }

    /**
     *  Finalize method. Make sure NDF is closed.
     */
    protected void finalize() throws Throwable
    {
        super.finalize();
        close();
    }

    //
    //  =============
    //  Static method
    //  =============
    //

    private static Logger logger = Logger.getLogger( "uk.ac.starlink.splat" );
    private static boolean supported = true;
    /**
     *  Load the shareable libraries that contains the NDF code and all
     *  its dependencies. Note this will never work for JWS, unless
     *  the shareable library has its dependency on JNIAST removed.
     */
    static {
        try {
            Loader.loadLibrary( "splat" );
        }
        catch (SecurityException se) {
            supported = false;
        }
        catch (UnsatisfiedLinkError ue) {
            supported = false;
        }
        catch (Exception ge) {
            //  Unexpected error.
            ge.printStackTrace();
            supported = false;
        }
        if ( ! supported ) {
            logger.warning( ( "Failed to load the " + 
                              Utilities.getReleaseName() + 
                              " JNI library" ) );
            logger.warning( "No native NDF support available" );
        }
    }

    /**
     *  Serialization version ID string (generated by serialver on
     *  original star.jspec.imagedata.NDFJ class).
     */
    static final long serialVersionUID = 7112558037504787639L;

    //
    //  ================
    //  Public constants
    //  ================
    //

    /**
     * Floating point blank pixel value.
     */
    public static final float BADF = -Float.MAX_VALUE;

    /**
     * Double precision blank pixel value.
     */
    public static final double BADD = -Double.MAX_VALUE;

    /**
     * Integer blank pixel value.
     */
    public static final int BADI = Integer.MIN_VALUE;

    /**
     * Short blank pixel value.
     */
    public static final short BADS = Short.MIN_VALUE;

    /**
     * Byte blank pixel value.
     */
    public static final byte BADB = Byte.MIN_VALUE;

    /**
     * Data is double precision.
     */
    public static final int DOUBLE = 0;

    /**
     * Data is single precision floating point.
     */
    public static final int FLOAT = 1;

    /**
     * Data is integer.
     */
    public static final int INTEGER = 2;

    /**
     * Data is short.
     */
    public static final int SHORT = 3;

    /**
     * Data is byte.
     */
    public static final int BYTE = 4;

    //
    //  ===================
    //  Protected variables
    //  ===================
    //

    /**
     * NDF identifier.
     *
     */
    protected int ident = 0;

    /**
     * NDF data type.
     */
    protected int type = FLOAT;

    /**
     * Number of dimensions.
     */
    protected int ndim = 0;

    /**
     * NDF dimensions
     */
    protected int[] dims;

    /**
     * AST FrameSet (if accessed). This is release by close().
     */
    protected volatile FrameSet wcs = null;


    //
    //  ====================
    //  Class public methods
    //  ====================
    //

    /**
     * See if NDF support is available.
     */
    public static boolean supported()
    {
        return supported;
    }

    /**
     * Open an NDF by name.
     *
     * @param name The NDF name.
     *
     * @return boolean true if NDF opened, false, otherwise.
     */
    public boolean open( String name )
    {
        if ( ident != 0 ) {
	    close();     //  NDF already opened. So close it.
        }
	int id = nOpen( name );
	if ( id != 0 ) {
            initialize( id );
            return true;
        }
        return false;
    }

    /**
     * Do one-off initializations of an NDF.
     *
     * @param id the NDF identifier.
     */
    protected void initialize( int id )
    {
        //  Keep reference to the NDF.
        ident = id;

        // Initialise various parameters.
        // Data type.
        type = nGetType( ident, "data" );

        // Dimensionality
        dims = nGetDims( ident );
        ndim = dims.length;
    }

    /**
     * Close NDF, if opened.
     */
    public void close()
    {
        if ( ident != 0 ) {
            nClose( ident );
            ident = 0;
	}
        wcs = null;
	type = FLOAT;
	ndim = 0;
        releaseFitsHeaders();
    }

    /**
     * Get the "natural" data type of the NDF.
     *
     * @return int The data type. One of the static variables "NDFJ.FLOAT",
     *             "NDFJ.DOUBLE", "NDFJ.SHORT" or "NDFJ.BYTE".
     */
    public int getType()
    {
        return type;
    }

    /**
     * Get the dimensions of the NDF.
     *
     * @return integer array of dimensions
     */
    public int[] getDims()
    {
        if ( ident != 0 ) {
            return dims;
        }
        else {
            return null;
        }
    }

    /**
     * Get the number of NDF dimensions.
     *
     * @return number of dimensions
     */
    public int getNDim()
    {
        if ( ident != 0 ) {
            return ndim;
        }
        else {
            return 0;
        }
    }

    /**
     * Create a temporary copy this NDF. The returned NDFJ references
     * an NDF that will be deleted when it is closed.
     *
     * @return NDFJ reference to the copy.
     *
     * @see "ndfTemp & ndfCopy in SUN/33"
     */
    public NDFJ getTempCopy()
    {
        if  ( ident != 0 ) {
            int place = nGetTemp();
            int copyIdent = nGetCopy( ident, place );
            return new NDFJ( copyIdent );
        }
        else {
            return null;
        }
    }

    /**
     * Create a copy this NDF with the given name.
     *
     * @return NDFJ reference to the copy.
     *
     * @see "ndfCopy in SUN/33"
     */
    public NDFJ getCopy( String name )
    {
        if  ( ident != 0 ) {
            int place = nOpenNew( name );
            if ( place != 0 ) {
                int copyIdent = nGetCopy( ident, place );
                return new NDFJ( copyIdent );
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    /**
     * Create a temporary double precision 1D NDF. The returned NDFJ
     * references an NDF that will be deleted when it is closed.
     *
     * @param size the number of elements in the NDF.
     *
     * @return NDFJ reference to the copy.
     *
     * @see "ndfTemp in SUN/33"
     */
    static public NDFJ get1DTempDouble( int size )
    {
        int place = nGetTemp();
        int ident = nGet1DNewDouble( place, size );
        return new NDFJ( ident );
    }

    /**
     * Read a 1D data array associated with the NDF.
     *
     * @param component The component to return (data or variance).
     * @param complete Return complete data (including extra dimensions)
     *
     * @return double[]  The NDF data array
     */
    public double[] get1DDouble( String component, boolean complete )
    {
        if ( ident != 0 ) {
            return nGet1DDouble( ident, component, complete );
        }
        else {
            return null;
        }
    }

    /**
     * Set the data array associated with the NDF to the values of a
     * given 1D array. If the array is larger than the destination
     * then only values up to the components current size are used.
     *
     * @param component The component to set (data or variance).
     * @param values the data values.
     */
    public void set1DDouble( String component, double[] values )
    {
        if ( ident != 0 ) {
            nSet1DDouble( ident, component, values );
        }
    }

    /**
     * Read a 1D data array associated with the NDF.
     *
     * @param component The component to return (data or variance).
     * @param complete Return complete data (including extra dimensions)
     *
     * @return float[]  The NDF data array
     */
    public float[] get1DFloat( String component, boolean complete )
    {
        if ( ident != 0 ) {
            return nGet1DFloat( ident, component, complete );
        }
        else {
            return null;
        }
    }

    /**
     * Read a 1D data array associated with the NDF.
     *
     * @param component The component to return (data or variance).
     * @param complete Return complete data (including extra dimensions)
     *
     * @return int[]  The NDF data array
     */
    public int[] get1DInt( String component, boolean complete )
    {
        if ( ident != 0 ) {
            return nGet1DInt( ident, component, complete );
        }
        else {
            return null;
        }
    }

    /**
     * Read a 1D data array associated with the NDF.
     *
     * @param component The component to return (data or variance).
     * @param complete Return complete data (including extra dimensions)
     *
     * @return short[]  The NDF data array
     */
    public short[] get1DShort( String component, boolean complete )
    {
        if ( ident != 0 ) {
            return nGet1DShort( ident, component, complete );
        }
        else {
            return null;
        }
    }

    /**
     * Read a 1D data array associated with the NDF.
     *
     * @param component The component to return (data or variance).
     * @param complete Return complete data (including extra dimensions)
     *
     * @return byte[]  The NDF data array
     */
    public byte[] get1DByte( String component, boolean complete )
    {
        if ( ident != 0 ) {
            return nGet1DByte( ident, component, complete );
        }
        else {
            return null;
        }
    }

    /**
     * Read a 2D data array associated with the NDF.
     *
     * @param component The component to return (data or variance).
     * @param complete Return complete data (including extra dimensions)
     *
     * @return double[][]  The 2D NDF data array
     */
    public double[][] get2DDouble( String component, boolean complete )
    {
        if ( ident != 0 ) {
            return nGet2DDouble( ident, component, complete );
        }
        else {
            return null;
        }
    }

    /**
     * Read a 2D data array associated with the NDF.
     *
     * @param component The component to return (data or variance).
     * @param complete Return complete data (including extra dimensions)
     *
     * @return float[][]  The 2D NDF data array
     */
    public float[][] get2DFloat( String component, boolean complete )
    {
        if ( ident != 0 ) {
            return nGet2DFloat( ident, component, complete );
        }
        else {
            return null;
        }
    }

    /**
     * Read a 2D data array associated with the NDF.
     *
     * @param component The component to return (data or variance).
     * @param complete Return complete data (including extra dimensions)
     *
     * @return int[][]  The 2D NDF data array
     */
    public int[][] get2DInt( String component, boolean complete )
    {
        if ( ident != 0 ) {
            return nGet2DInt( ident, component, complete );
        }
        else {
            return null;
        }
    }

    /**
     * Read a 2D data array associated with the NDF.
     *
     * @param component The component to return (data or variance).
     * @param complete Return complete data (including extra dimensions)
     *
     * @return short[][]  The 2D NDF data array
     */
    public short[][] get2DShort( String component, boolean complete )
    {
        if ( ident != 0 ) {
            return nGet2DShort( ident, component, complete );
        }
        else {
            return null;
        }
    }

    /**
     * Read a 2D data array associated with the NDF.
     *
     * @param component The component to return (data or variance).
     * @param complete Return complete data (including extra dimensions)
     *
     * @return byte[][]  The 2D NDF data array
     */
    public byte[][] get2DByte( String component, boolean complete )
    {
        if ( ident != 0 ) {
            return nGet2DByte( ident, component, complete );
        }
        else {
            return null;
        }
    }

    /**
     * Check for the existence of an NDF data component
     *
     * @param component The component to check
     *
     * @return boolean true if component exists
     */
    public boolean has( String component )
    {
        if ( ident != 0 ) {
            return nHas( ident, component );
        }
        else {
            return false;
        }
    }

    /**
     * Obtain a reference to an AST FrameSet that describes the NDF
     * coordinate systems
     *
     * @return the FrameSet
     */
    public FrameSet getAst()
    {
        if ( ident != 0 ) {
            if ( wcs == null ) {
                wcs = getAst( ident );
            }
        }
        return wcs;
    }

    /**
     * Set reference to an AST FrameSet that describes the NDF
     * coordinate systems (not read from NDF). 
     *
     * @param newwcs AST FrameSet
     */
    public void setAst( FrameSet newwcs )
    {
        wcs = (FrameSet) newwcs.clone();
    }

    /**
     * Write a new AST frameset to the NDF. The frameset should map
     * between pixel and axis coordinates. This frameset also becomes
     * the current AST frameset.
     *
     * @param wcs the AST frameset to be used.
     */
    public void saveAst( FrameSet wcs )
    {
        if ( ident != 0 ) {
            setAst( ident, wcs );
            setAst( wcs );
        }
    }

    /**
     *  Return the value of a character component.
     *
     *  @param comp name of the component.
     */
    public String getCharComp( String comp )
    {
        if ( ident != 0 ) {
            return nGetCharComp( ident, comp );
        }
        else {
            return "";
        }
    }

    /**
     *  Set the value of a character component.
     *
     *  @param comp name of the component.
     *  @param value the value of the component.
     */
    public void setCharComp( String comp, String value )
    {
        if ( ident != 0 ) {
            nSetCharComp( ident, comp, value );
        }
    }

    // ===================
    // AST FrameSet access
    // ===================

    /**
     * Create an AST FrameSet from the NDF WCS component. This is
     * passed as a long pointer reference through the JNI interface
     * for speed (encoding/decoding this as a character was too
     * slow) and is then assigned to a local FrameSet sub-class.
     */
    protected FrameSet getAst( int ident )
    {
//         try {
//             String[] astArray = nGetAstArray( ident );
//             if ( astArray != null ) {
//                 ASTChannel chan = new ASTChannel( astArray );
//                 return (FrameSet) chan.read();
//             }
//         }
//         catch (Exception e) {
//             e.printStackTrace();
//         }
//         return null;

        long pointer = nGetAst( ident );
        return new NDFJFrameSet( pointer );
    }

    /**
     * Write an AST FrameSet into an NDF.
     */
    protected void setAst( int ident, FrameSet wcs )
    {
        // We need to create an String array version of an AST
        // FrameSet and write this safe version, if the FrameSet is
        // not a instance of NDFJFrameSet. In that case we can cheat
        // by getting at the internal pointer.
        if ( wcs instanceof NDFJFrameSet ) {
            nSetAst( ident, ((NDFJFrameSet) wcs).getPointer() );
        }
        else {
            try {
                String[] astArray = new String[1];
                ASTChannel chan = new ASTChannel( astArray );
                chan.write( wcs );

                //  The dummy channel now knows how much space is
                //  required.
                astArray = new String[chan.getIndex()];
                chan.setArray( astArray );
                chan.write( wcs );
                nSetAstArray( ident, astArray );
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //  ==================
    //  FITS header access
    //  ==================

    /**
     * Reference to the FITS extension if currently mapped.
     */
    protected volatile long fitsref = 0;

    /**
     * Number of header cards available.
     */
    protected volatile int fitscards = 0;

    /**
     * Return if a FITS extension is available.
     *
     * @return true if available, false otherwise and it NDF not yet
     *         opened.
     */
    public boolean hasFitsExtension()
    {
        if ( ident != 0 ) {
            return nHasExtension( ident, "FITS" );
        }
        else {
            return false;
        }
    }

    /**
     * Determine if the NDF has any FITS headers and return a count of
     * them. Must be invoked before attempting to retrieve the headers.
     */
    protected void accessFitsHeaders()
    {
        if ( fitsref == 0 && ident != 0 ) {
            if ( hasFitsExtension() ) {
                fitsref = nAccessFitsHeaders( ident );
                fitscards = nCountFitsHeaders( fitsref );
            }
        }
    }

    /**
     * Return the number of FITS header cards available.
     *
     * @return number of cards.
     */
    public int countFitsHeaders()
    {
        accessFitsHeaders();
        return fitscards;
    }

    /**
     * Return a specific FITS card as a String.
     *
     * @param index index of the card.
     *
     * @return null if the card doesn't exist, or if FITS headers not
     *         available.
     */
    public String getFitsHeader( int index )
    {
        accessFitsHeaders();
        if ( fitsref != 0 ) {
            if ( index < fitscards ) {
                return nGetFitsHeader( fitsref, index );
            }
        }
        return null;
    }

    /**
     * Create and write a FITS extension.
     *
     * @param cards the FITS cards to put in the extension.
     */
    public void createFitsExtension( String[] cards )
    {
        if ( ident != 0 ) {
            if ( fitsref != 0 ) {
                releaseFitsHeaders();
            }
            nCreateFitsExtension( ident, cards );
        }
    }

    /**
     * Release any resources associated with accessing the NDF FITS
     * headers. Also performed as part of close.
     */
    public void releaseFitsHeaders()
    {
        if ( fitsref != 0 ) {
            nReleaseFitsHeaders( fitsref );
            fitsref = 0;
        }
    }

    //
    //  =================
    //  Native interface.
    //  =================
    //
    //  Notes: these are all synchronized as NDF has loads of global
    //  data areas that should not be accessed by different Threads.

    /**
     * Open an existing NDF by name
     *
     * @param name The NDF name.
     *
     * @return NDF identifier (reference)
     */
    protected synchronized static native int nOpen( String name );

    /**
     * Create an new NDF by name
     *
     * @param name The NDF name.
     *
     * @return NDF placeholder
     */
    protected synchronized static native int nOpenNew( String name );

    /**
     * Close the opened NDF.
     *
     * @param ident NDF identifier
     */
    protected synchronized static native void nClose( int ident );

    /**
     * Get the data type that an NDF data component will be returned
     * in. Note that NDF actually supports more data types than are
     * allowed, and these are converted as required.
     *
     * @param component NDF component.
     *
     * @return int The data type.
     */
    protected synchronized static native int nGetType( int ident,
                                                       String component );

    /**
     * Check if a named NDF component exists.
     *
     * @param component NDF component.
     *
     * @return boolean true if component exists.
     */
    protected synchronized static native boolean nHas( int ident,
                                                       String component );

    /**
     * Get the dimensionality of the NDF.
     *
     * @return Integer array of dimensions.
     */
    protected synchronized static native int[] nGetDims( int indent );

    /**
     * Read a 1D data array associated with the NDF.
     *
     * @param ident The NDF identifier.
     * @param component The component to return (data or variance).
     * @param complete Return complete data (including extra dimensions)
     *
     * @return double[]  The 1D NDF data array.
     */
    protected synchronized static native double[]
        nGet1DDouble( int ident, String component, boolean complete );

    /**
     * Set a 1D data array associated with the NDF.
     *
     * @param ident The NDF identifier.
     * @param component The component to return (data or variance).
     * @param values The data values to use.
     */
    protected synchronized static native void nSet1DDouble( int ident,
                                                            String component,
                                                            double[] values );

    /**
     * Read a 1D data array associated with the NDF.
     *
     * @param ident The NDF identifier.
     * @param component The component to return (data or variance).
     * @param complete Return complete data (including extra dimensions)
     *
     * @return float[]  The 1D NDF data array.
     */
    protected synchronized static native float[]
        nGet1DFloat( int ident, String component, boolean complete);

    /**
     * Read a 1D data array associated with the NDF.
     *
     * @param ident The NDF identifier.
     * @param component The component to return (data or variance).
     * @param complete Return complete data (including extra dimensions)
     *
     * @return int[]  The 1D NDF data array.
     */
    protected synchronized static native int[] nGet1DInt( int ident,
                                                          String component,
                                                          boolean complete );

    /**
     * Read a 1D data array associated with the NDF.
     *
     * @param ident The NDF identifier.
     * @param component The component to return (data or variance).
     * @param complete Return complete data (including extra dimensions)
     *
     * @return short[]  The 1D NDF data array.
     */
    protected synchronized static native short[]
        nGet1DShort( int ident, String component, boolean complete );

    /**
     * Read a 1D data array associated with the NDF.
     *
     * @param ident The NDF identifier.
     * @param component The component to return (data or variance).
     * @param complete Return complete data (including extra dimensions)
     *
     * @return byte[]  The 1D NDF data array.
     */
    protected synchronized static native byte[] nGet1DByte( int ident,
                                                            String component,
                                                            boolean complete );

    /**
     * Read a 2D data array associated with the NDF.
     *
     * @param ident The NDF identifier.
     * @param component The component to return (data or variance).
     * @param complete Return complete data (including extra dimensions)
     *
     * @return double[][]  The 2D NDF data array.
     */
    protected synchronized static native double[][]
        nGet2DDouble( int ident, String component, boolean complete );

    /**
     * Read a 2D data array associated with the NDF.
     *
     * @param ident The NDF identifier.
     * @param component The component to return (data or variance).
     * @param complete Return complete data (including extra dimensions)
     *
     * @return float[][]  The 2D NDF data array.
     */
    protected synchronized static native float[][]
        nGet2DFloat( int ident, String component, boolean complete );

    /**
     * Read a 2D data array associated with the NDF.
     *
     * @param ident The NDF identifier.
     * @param component The component to return (data or variance).
     * @param complete Return complete data (including extra dimensions)
     *
     * @return int[][]  The 2D NDF data array.
     */
    protected synchronized static native int[][] nGet2DInt( int ident,
                                                            String component,
                                                            boolean complete );

    /**
     * Read a 2D data array associated with the NDF.
     *
     * @param ident The NDF identifier.
     * @param component The component to return (data or variance).
     * @param complete Return complete data (including extra dimensions)
     *
     * @return short[][]  The 2D NDF data array.
     */
    protected synchronized static native short[][]
        nGet2DShort( int ident, String component, boolean complete );

    /**
     * Read a 2D data array associated with the NDF.
     *
     * @param ident The NDF identifier.
     * @param component The component to return (data or variance).
     * @param complete Return complete data (including extra dimensions)
     *
     * @return byte[][]  The 2D NDF data array.
     */
    protected synchronized static native byte[][]
        nGet2DByte( int ident, String component, boolean complete );

    /**
     * Get a characater array that contains the NDF AST FrameSet.
     *
     * @param ident The NDF identifier.
     *
     * @return String[] the FrameSet as a character array (native
     *                  format).
     */
    protected synchronized static native String[] nGetAstArray( int ident );

    /**
     * Get a long value that holds a pointer to the NDF AST FrameSet.
     *
     * @param ident The NDF identifier.
     *
     * @return long the FrameSet pointer.
     */
    protected synchronized static native long nGetAst( int ident );

    /**
     * Set the NDF AST Frameset.
     *
     * @param ident the NDF identifier.
     * @param wcsArray the FrameSet encoded as a character array
     *                 (native format).
     */
    protected synchronized static native void 
        nSetAstArray( int ident, String[] wcsArray );

    /**
     * Set the NDF AST Frameset.
     *
     * @param ident the NDF identifier.
     * @param pointer the FrameSet pointer.
     */
    protected synchronized static native void nSetAst( int ident,
                                                       long pointer );

    /**
     *  Get the value of a character component.
     *
     *  @param ident The NDF identifier
     *  @param comp The NDF character component name
     *
     *  @return The character component value, blank if not located.
     */
    protected synchronized static native String nGetCharComp( int ident,
                                                              String comp );

    /**
     *  Set the value of a character component.
     *
     *  @param ident The NDF identifier
     *  @param comp The NDF character component name
     *  @param value The value
     */
    protected synchronized static native void nSetCharComp( int ident,
                                                            String comp,
                                                            String value );

    /**
     * Get an NDF placeholder for a temporary NDF.
     *
     * @return NDF placeholder (use this just once).
     */
    protected synchronized static native int nGetTemp();

    /**
     * Get a copy of this NDF.
     *
     * @param ident The NDF identifier.
     * @param placeHolder an NDF place holder for the NDF (i.e. where
     *                    the copy is to be created).
     *
     * @return new NDF identifier.
     */
    protected synchronized static native int nGetCopy( int ident,
                                                       int placeHolder );

    /**
     * Get a new double precision 1 dimensional NDF.
     *
     * @param placeHolder an NDF place holder for the NDF (i.e. where
     *                    the copy is to be created).
     * @param size number of elements in new NDF array components.
     *
     * @return new NDF identifier.
     */
    protected synchronized static native int nGet1DNewDouble( int placeHolder,
                                                              int size );

    /**
     * Check if a named NDF extension exists.
     *
     * @param extension NDF extension name (i.e. FITS).
     *
     * @return boolean true if extension exists.
     */
    protected synchronized static native boolean
        nHasExtension( int ident, String extension );

    /**
     * Access the NDF FITS headers. Returns a reference to the header
     * block that should be released.
     *
     * @param ident The NDF identifier.
     *
     * @return reference to the FITS block.
     */
    protected synchronized static native long nAccessFitsHeaders( int ident );

    /**
     * Count the cards available in the FITS headers.
     *
     * @param fitsref reference to the NDF fits headers.
     *
     * @return number of cards in FITS headers
     */
    protected synchronized static native int nCountFitsHeaders( long fitsref );

    /**
     * Return a card from the FITS headers.
     *
     * @param fitsref reference to the NDF fits headers.
     *
     * @return the header card or "".
     */
    protected synchronized static native String nGetFitsHeader( long fitsref,
                                                                int index );

    /**
     * Release resources allocated for the FITS headers.
     *
     * @param fitsref reference to the NDF fits headers.
     */
    protected synchronized static native void
        nReleaseFitsHeaders( long fitsref );

    /**
     * Create a FITS extension in the NDF using the given cards to
     * populate it.
     *
     * @param ident The NDF identifier.
     * @param cards String array of the cards.
     */
    protected synchronized static native void
        nCreateFitsExtension( int ident, String[] cards );
}

/* ********************************************************
 * This file automatically generated by FluxFrame.pl.
 *                   Do not edit.                         *
 **********************************************************/

package uk.ac.starlink.ast;


/**
 * Java interface to the AST FluxFrame class
 *  - measured flux description. 
 * A FluxFrame is a specialised form of one-dimensional Frame which 
 * represents various systems used to represent the signal level in an
 * observation. The particular coordinate system to be used is specified 
 * by setting the FluxFrame's System attribute qualified, as necessary, by 
 * other attributes such as the units, etc (see the description of the 
 * System attribute for details).
 * <p>
 * All flux values are assumed to be measured at the same frequency or
 * wavelength (as given by the SpecVal attribute). Thus this class is
 * more appropriate for use with images rather than spectra.
 * 
 * 
 * @see  <a href='http://star-www.rl.ac.uk/cgi-bin/htxserver/sun211.htx/?xref_FluxFrame'>AST FluxFrame</a>  
 */
public class FluxFrame extends Frame {
    /** 
     * Creates a FluxFrame with given spectral frame and value.   
     * @param  specval  The spectral value to which the flux values refer, given in the
     * spectral coordinate system specified by 
     * "specfrm". The value supplied for the "specval" 
     * parameter becomes the default value for the SpecVal attribute.
     * A value of AST__BAD may be supplied if the spectral position is
     * unknown, but this may result in it not being possible for the
     * astConvert
     * function to determine a Mapping between the new FluxFrame and
     * some other FluxFrame.
     * 
     * @param  specfrm  A pointer to a SpecFrame describing the spectral coordinate system 
     * in which the 
     * "specval" 
     * parameter is given. A deep copy of this object is taken, so any 
     * subsequent changes to the SpecFrame using the supplied pointer will 
     * have no effect on the new FluxFrame. 
     * A NULL pointer can be supplied if AST__BAD is supplied for "specval".
     * 
     * @throws  AstException  if an error occurred in the AST library
    */
    public FluxFrame( double specval, SpecFrame specfrm ) {
        construct( specval, specfrm );
    }
    private native void construct( double specval, SpecFrame specfrm );


    /**
     * Creates a FluxFrame with no default spectral value or frame.
     */
    public FluxFrame() {
        this( AST__BAD, null );
    }
    
    /**
     * Get 
     * the spectral position at which flux values are measured.  
     * This attribute specifies the spectral position (frequency, wavelength, 
     * etc.), at which the values described by the FluxFrame are measured.
     * It is used when determining the Mapping between between FluxFrames.
     * <p>
     * The default value and spectral system used for this attribute are
     * both specified when the FluxFrame is created.
     * 
     *
     * @return  this object's SpecVal attribute
     */
    public double getSpecVal() {
        return getD( "SpecVal" );
    }

    /**
     * Set 
     * the spectral position at which flux values are measured.  
     * This attribute specifies the spectral position (frequency, wavelength, 
     * etc.), at which the values described by the FluxFrame are measured.
     * It is used when determining the Mapping between between FluxFrames.
     * <p>
     * The default value and spectral system used for this attribute are
     * both specified when the FluxFrame is created.
     * 
     *
     * @param  specVal   the SpecVal attribute of this object
     */
    public void setSpecVal( double specVal ) {
       setD( "SpecVal", specVal );
    }

}

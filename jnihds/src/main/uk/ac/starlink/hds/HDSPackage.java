package uk.ac.starlink.hds;

import java.util.logging.Logger;

/**
 * Provides information about the status of the JNIHDS package.
 * This class offers a method which can be invoked to determine whether
 * the {@link HDSObject} class is available for use.
 *
 * @author   Mark Taylor (Starlink)
 */
public class HDSPackage {

    private static Boolean loaded;
    private static Logger logger = Logger.getLogger( "uk.ac.starlink.hds" );

    /** Private sol constructor to prevent instantiation. */
    private HDSPackage() {}

    /**
     * Indicates whether the HDSObject class is available or not.
     * This will return <tt>true</tt> if the JNIHDS classes can be used,
     * buf <tt>false</tt> if the requisite native code is not
     * available (the shared library is not on <tt>java.lang.path</tt>).
     * If the classes are not available, then the first time it is invoked
     * it will write a warning to that effect via the logger.
     *
     * @return  <tt>true</tt> iff the HDSObject class is available
     */
    public static boolean isAvailable() {
        if ( loaded == null ) {
            try {
                int i = HDSObject.DAT__SZNAM;
                loaded = Boolean.TRUE;
            }
            catch ( UnsatisfiedLinkError e ) {
                loaded = Boolean.FALSE;
            }
            catch ( LinkageError e ) {
                loaded = Boolean.FALSE;
                e.printStackTrace();
            }
            if ( ! loaded.booleanValue() ) {
                logger.warning( "JNIHDS native library not on java.lang.path "
                              + "- no NDF/HDS access" );
            }
        }
        return loaded.booleanValue();
    }
}

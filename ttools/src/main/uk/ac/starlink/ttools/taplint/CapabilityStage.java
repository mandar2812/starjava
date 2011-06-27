package uk.ac.starlink.ttools.taplint;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import uk.ac.starlink.vo.TapCapability;
import uk.ac.starlink.vo.TapQuery;
import org.xml.sax.SAXException;

/**
 * Stage for checking content of TAPRegExt capability metadata.
 *
 * @author   Mark Taylor
 * @since    3 Jun 2011
 */
public class CapabilityStage implements Stage {

    private TapCapability tcap_;

    public String getDescription() {
        return "Check content of TAPRegExt capabilities record";
    }

    /**
     * Returns the TAP capability record obtained by the last run of this stage.
     *
     * @return   tap capability object
     */
    public TapCapability getCapability() {
        return tcap_;
    }

    public void run( Reporter reporter, URL serviceUrl ) {
        final TapCapability tcap;
        try {
            tcap = TapQuery.readTapCapability( serviceUrl );
        }
        catch ( SAXException e ) {
            reporter.report( Reporter.Type.WARNING, "FLSX",
                             "Error parsing capabilities metadata", e );
            return;
        }
        catch ( IOException e ) {
            reporter.report( Reporter.Type.ERROR, "FLIO",
                             "Error reading capabilities metadata", e );
            return;
        }
        checkCapability( reporter, tcap );
        tcap_ = tcap;
    }

    private void checkCapability( Reporter reporter, TapCapability tcap ) {

        /* Check query languages. */
        String[] languages = tcap.getLanguages();
        if ( languages.length == 0 ) {
            reporter.report( Reporter.Type.ERROR, "NOQL",
                             "No query languages declared" );
        }
        else {
            boolean hasAdql2 = false;
            boolean hasAdql = false;
            for ( int il = 0; il < languages.length; il++ ) {
                String lang = languages[ il ];
                if ( lang.startsWith( "ADQL-" ) ) {
                    hasAdql = true;
                }
                if ( lang.equals( "ADQL-2.0" ) ) {
                    hasAdql2 = true;
                }
            }
            if ( ! hasAdql ) {
                reporter.report( Reporter.Type.ERROR, "ADQX",
                                 "ADQL not declared as a query language" );
            }
            else if ( ! hasAdql2 ) {
                reporter.report( Reporter.Type.WARNING, "AD2X",
                                 "ADQL-2.0 not declared as a query language" );
            }
        }

        /* Check upload methods. */
        String[] upMethods = tcap.getUploadMethods();
        String stdPrefix = TapCapability.UPLOADS_URI + "#";
        Collection<String> stdSuffixList =
            Arrays.asList( new String[] { "inline", "http", "https", "ftp" } );
        for ( int iu = 0; iu < upMethods.length; iu++ ) {
            String upMethod = upMethods[ iu ];
            if ( upMethod.startsWith( stdPrefix ) ) {
                String frag = upMethod.substring( stdPrefix.length() );
                if ( ! stdSuffixList.contains( frag ) ) {
                    reporter.report( Reporter.Type.ERROR, "UPBD",
                                     "Unknown suffix \"" + frag
                                   + "\" for upload method" );
                }
            }
            else {
                reporter.report( Reporter.Type.WARNING, "UPCS",
                                 "Custom upload method \"" + upMethod + "\"" );
            }
        }
    }
}

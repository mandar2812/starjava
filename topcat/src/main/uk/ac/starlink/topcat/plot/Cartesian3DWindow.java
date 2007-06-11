package uk.ac.starlink.topcat.plot;

import java.awt.Component;

/**
 * Graphics window for viewing 3D scatter plots using Cartesian coordinates.
 *
 * @author   Mark Taylor
 * @since    22 Nov 2005
 */
public class Cartesian3DWindow extends Plot3DWindow {

    private static final ErrorRenderer[] ERROR_RENDERERS =
        ErrorRenderer.getOptions3d();
    private static final String[] AXIS_NAMES = new String[] { "X", "Y", "Z" };

    /**
     * Constructs a new window.
     *
     * @param   parent  parent component (may be used for postioning)
     */
    public Cartesian3DWindow( Component parent ) {
        super( "3D", AXIS_NAMES, 3, parent, createErrorModeModels( AXIS_NAMES ),
               new CartesianPlot3D() );

        for ( int ierr = 0; ierr < 3; ierr++ ) {
            getToolBar().add( getErrorModeModels()[ ierr ]
                                                 .createOnOffToolbarButton() );
        }
        getJMenuBar().add( createErrorModeMenu() );
        getJMenuBar().add( createMarkerStyleMenu( PlotWindow.STYLE_SETS ) );
        getJMenuBar().add( createErrorRendererMenu( ERROR_RENDERERS ) );
        getToolBar().addSeparator();

        addHelp( "Cartesian3DWindow" );
    }

    protected StyleEditor createStyleEditor() {
        return new MarkStyleEditor( false, true, ERROR_RENDERERS,
                                    getErrorModeModels() );
    }
}

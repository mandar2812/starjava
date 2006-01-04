package uk.ac.starlink.topcat.plot;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 * Draws the legend for identifying points on a plot.
 *
 * @author   Mark Taylor
 * @since    4 Jan 2006
 */
public class Legend extends JPanel {

    private Style[] styles_;
    private String[] labels_;
    private Dimension size_;

    /**
     * Constructor.
     */
    public Legend() {
        setOpaque( false );
        styles_ = new Style[ 0 ];
        labels_ = new String[ 0 ];
        size_ = new Dimension( 30, 100 );
    }

    /**
     * Sets the plot styles and their associated text labels.
     * The two arrays must have the same length
     *
     * @param   styles   style array
     * @param   labels   label array
     */
    public void setStyles( Style[] styles, String[] labels ) {

        /* Validate. */
        if ( styles_.length != labels_.length ) {
            throw new IllegalArgumentException();
        }

        /* Store state. */
        styles_ = styles;
        labels_ = labels;

        /* Calculate the size and update the state of this component 
         * accordingly.  The drawing is done onto an ad hoc graphics context
         * just to work out the size; the drawing is then thrown away.
         * The actual plot is only done within the paintComponent method. */
        Dimension size = drawLegend( getGraphics() );
        boolean bigger = size.width > size_.width || size.height > size_.height;
        size_ = size;
        setSize( size );
        setPreferredSize( size );
        if ( bigger ) {
            revalidate();
        }
    }

    protected void paintComponent( Graphics g ) {
        super.paintComponent( g );
        drawLegend( g );
    }

    /**
     * Draws the current state of this component onto a given Graphics object
     * and returns the size of the resulting image.
     *
     * @param  g  graphics context
     */
    private Dimension drawLegend( Graphics g ) {
        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getHeight();
        int lineAscent = fm.getAscent();
        int gx = 10;
        int tx = 20;
        int y = 0;
        int maxWidth = size_.width;
        for ( int i = 0; i < styles_.length; i++ ) {
            String label = labels_[ i ];
            y += lineHeight;
            styles_[ i ].drawLegend( g, gx, y - lineAscent / 2 );
            g.drawString( label, tx, y );
            maxWidth = Math.max( maxWidth, tx + fm.stringWidth( label ) + 1 );
        }
        return new Dimension( maxWidth, y + lineHeight );
    }
}

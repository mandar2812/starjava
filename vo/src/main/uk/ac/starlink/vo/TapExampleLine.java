package uk.ac.starlink.vo;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Displays information about a current displayed ADQL example.
 *
 * @author   Mark Taylor
 * @since    10 Jun 2015
 */
public class TapExampleLine extends JPanel {

    private final UrlHandler urlHandler_;
    private final JLabel titleLabel_;
    private final JTextField nameField_;
    private final Action infoAct_;
    private AdqlExample example_;

    /**
     * Constructor.
     *
     * @param  urlHandler  handles URL clicks
     */
    public TapExampleLine( UrlHandler urlHandler ) {
        urlHandler_ = urlHandler;
        titleLabel_ = new JLabel();
        nameField_ = new JTextField();
        nameField_.setEditable( false );
        nameField_.setBorder( BorderFactory.createEmptyBorder() );
        infoAct_ = new AbstractAction( "Info" ) {
            public void actionPerformed( ActionEvent evt ) {
                urlHandler_.clickUrl( example_.getInfoUrl() );
            }
        };
        setLayout( new BoxLayout( this, BoxLayout.LINE_AXIS ) );
        JComponent textBox = Box.createHorizontalBox();
        textBox.setBorder( BorderFactory.createEtchedBorder() );
        textBox.add( Box.createHorizontalStrut( 5 ) );
        textBox.add( titleLabel_ );
        textBox.add( nameField_ );
        add( textBox );
        add( Box.createHorizontalGlue() );
        add( Box.createHorizontalStrut( 5 ) );
        add( new JButton( infoAct_ ) );
        setExample( null, null );
    }

    /**
     * Sets the example to be displayed in this line.
     *
     * @param   example  example
     * @param   groupName  subtitle for example, or null
     */
    public void setExample( AdqlExample example, String groupName ) {
        example_ = example;
        titleLabel_.setText( groupName == null ? null : groupName + ": " );
        titleLabel_.setEnabled( example != null );
        nameField_.setText( example == null ? null : example.getName() );
        nameField_.setToolTipText( example == null ? null
                                                   : example.getDescription() );
        boolean hasInfo = example != null && example.getInfoUrl() != null;
        infoAct_.setEnabled( urlHandler_ != null && hasInfo );
        String descrip = "Display example information in web browser";
        if ( hasInfo ) {
            descrip += " (" + example.getInfoUrl() + ")";
        }
        infoAct_.putValue( Action.SHORT_DESCRIPTION, descrip );
    }

    /**
     * Returns the currently displayed example.
     *
     * @return  example, may be null
     */
    public AdqlExample getExample() {
        return example_;
    }
}

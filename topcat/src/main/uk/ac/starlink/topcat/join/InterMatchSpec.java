package uk.ac.starlink.topcat.join;

import java.awt.Component;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import uk.ac.starlink.table.DefaultValueInfo;
import uk.ac.starlink.table.DescribedValue;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.Tables;
import uk.ac.starlink.table.ValueInfo;
import uk.ac.starlink.table.join.MatchEngine;
import uk.ac.starlink.table.join.MatchStarTables;
import uk.ac.starlink.table.join.ProgressIndicator;
import uk.ac.starlink.table.join.RowLink;
import uk.ac.starlink.table.join.RowMatcher;
import uk.ac.starlink.topcat.AuxWindow;
import uk.ac.starlink.topcat.BitsRowSubset;
import uk.ac.starlink.topcat.ControlWindow;
import uk.ac.starlink.topcat.RowSubset;
import uk.ac.starlink.topcat.TopcatModel;

/**
 * MatchSpec for performing matches between multiple tables.
 *
 * @author   Mark Taylor (Starlink)
 * @since    20 Mar 2004
 */
public class InterMatchSpec extends MatchSpec {

    private final int nTable;
    private final MatchEngine engine;
    private final TupleSelector[] tupleSelectors;
    private final OutputRequirements[] outReqs;
    private StarTable result;
    private int matchCount;
    private RowSubset[] matchSubsets;

    /**
     * Constructs a new InterMatchSpec.
     *
     * @param  engine   match algorithm object
     * @param  nTable   number of tables on which this InterMatch will operate
     */
    public InterMatchSpec( MatchEngine engine, int nTable ) {
        this.nTable = nTable;
        this.engine = engine;

        Box main = Box.createVerticalBox();
        add( main );

        /* Set up table/column selector panels. */
        tupleSelectors = new TupleSelector[ nTable ];
        for ( int i = 0; i < nTable; i++ ) {
            TupleSelector selector = new TupleSelector( engine );
            selector.setBorder( AuxWindow
                               .makeTitledBorder( "Table " + ( i + 1 ) ) );
            tupleSelectors[ i ] = selector;
            main.add( selector );
        }

        /* Set up components for specifying details of the output table. */
        outReqs = new OutputRequirements[ nTable ];
        for ( int i = 0; i < nTable; i++ ) {
            outReqs[ i ] = new OutputRequirements();
        }

        /* Set up selector for required output rows. */
        Box rowBox = Box.createVerticalBox();
        for ( int i = 0; i < nTable; i++ ) {
            Box line = Box.createHorizontalBox();
            line.add( new JLabel( "Table " + ( i + 1 ) + ": " ) );
            line.add( outReqs[ i ].getRowLine() );
            rowBox.add( line );
        }
        rowBox.setBorder( AuxWindow.makeTitledBorder( "Output Rows" ) );
        main.add( rowBox );
    }

    public void checkArguments() {
        for ( int i = 0; i < nTable; i++ ) {
            TupleSelector ts = tupleSelectors[ i ];
            try {
                StarTable st = ts.getEffectiveTable();
            }
            catch ( IllegalStateException e ) {
                throw new IllegalStateException( e.getMessage() + 
                                                 " for table " + ( i + 1 ) );
            }
        }
    }

    /**
     * Calculates the new matched table.
     */
    public void calculate( ProgressIndicator indicator )
            throws IOException, InterruptedException {
        matchSubsets = null;
        result = null;
        TopcatModel[] tcModels = new TopcatModel[ nTable ];
        StarTable[] tables = new StarTable[ nTable ];
        StarTable[] bases = new StarTable[ nTable ];
        for ( int i = 0; i < nTable; i++ ) {
            tcModels[ i ] = tupleSelectors[ i ].getTable();
            tables[ i ] = tupleSelectors[ i ].getEffectiveTable();
            bases[ i ] = tcModels[ i ].getApparentStarTable();
        }

        /* Do the matching. */
        boolean[] useAlls = getUseAlls();
        RowMatcher matcher = new RowMatcher( engine, tables );
        matcher.setIndicator( indicator );
        List matches;
        Map matchScores;
        if ( nTable == 2 ) {
            matchScores = matcher.findPairMatches( ! useAlls[ 0 ],
                                                   ! useAlls[ 1 ] );
            matches = new ArrayList( matchScores.keySet() );
            Collections.sort( matches );
        }
        else {
            matches = matcher.findGroupMatches( useAlls );
            matchScores = null;
        }
        int nrow = matches.size();

        /* Create a new table based on the matched lines we have identified. */
        result = MatchStarTables
                .makeJoinTable( bases, matches, matchScores,
                                getDefaultFixActions( nTable ) );
        addMatchMetadata( result, getDescription(), engine, tables );

        /* If it makes sense to do so, record which tables appear in which
         * rows. */
        BitSet[] bitsets = new BitSet[ nTable ];
        for ( int i = 0; i < nTable; i++ ) {
            bitsets[ i ] = new BitSet();
        }
        matchCount = 0;
        int irow = 0;
        for ( Iterator it = matches.iterator(); it.hasNext(); ) {
            RowLink link = (RowLink) it.next();
            int nref = link.size();
            for ( int i = 0; i < nref; i++ ) {
                int iTable = link.getRef( i ).getTableIndex();
                bitsets[ iTable ].set( irow );
            }
            if ( nref > 1 ) {
                matchCount++;
            }
            irow++;
        }
        assert irow == nrow;
        List subsetList = new ArrayList();
        for ( int i = 0; i < nTable; i++ ) {
            BitSet bset = bitsets[ i ];
            int ntrue = bset.cardinality();
            if ( ntrue > 0 && ntrue < nrow ) {
                RowSubset rset =
                    new BitsRowSubset( "match" + tcModels[ i ].getID(),
                                        bitsets[ i ] );
                subsetList.add( rset );
            }
        }
        matchSubsets = (RowSubset[]) subsetList.toArray( new RowSubset[ 0 ] );
    }

    public void matchSuccess( Component parent ) {
        Object msg;
        String title;
        int msgType;
        if ( result.getRowCount() == 0 || matchCount == 0 ) {
            msg = "Matched table contains no rows";
            title = "Match Failed";
            msgType = JOptionPane.ERROR_MESSAGE;
        }
        else {
            StringBuffer sbuf = new StringBuffer( "match" );
            for ( int i = 0; i < nTable; i++ ) {
                sbuf.append( i == 0 ? '(' : ',' );
                sbuf.append( tupleSelectors[ i ].getTable().getID() );
            }
            sbuf.append( ')' );
            TopcatModel tcModel = ControlWindow.getInstance()
                                 .addTable( result, sbuf.toString(), true );
            for ( int i = 0; i < matchSubsets.length; i++ ) {
                tcModel.addSubset( matchSubsets[ i ] );
            }
            msg = new String[] {
                matchCount + ( nTable == 2 ? " pairs" : " match groups" )
                           + " found",
                "New table created by match: " + tcModel,
            };
            title = "Match Successful";
            msgType = JOptionPane.INFORMATION_MESSAGE;
        }
        JOptionPane.showMessageDialog( parent, msg, title, msgType );
    }

    public String getDescription() {
        return toString();
    }

    /**
     * Returns an array of booleans indicating, for each input table, 
     * whether all rows or only the matched rows of that table should
     * appear in the output.
     *
     * @return  nTable-element array
     */
    private boolean[] getUseAlls() {
        boolean[] ua = new boolean[ nTable ];
        for ( int i = 0; i < nTable; i++ ) {
            ua[ i ] = outReqs[ i ].getRowOption() == MatchOption.ANY;
        }
        return ua;
    }

    /**
     * Doctors a table's metadata to record the details of a match which
     * has created it.  The 'effective tables' which formed the match are
     * specified.
     *          
     * @param  table  the new matched table
     * @param  matchType  a string indicating what sort of matching operation
     *                    was performed 
     * @param  effTables  effective tables used for the match
     */
    private static void addMatchMetadata( StarTable table, String matchType,
                                          MatchEngine engine,
                                          StarTable[] effTables ) {
        List params = table.getParameters();
        params.add( new DescribedValue( MATCHTYPE_INFO, matchType ) );
        params.add( new DescribedValue( ENGINE_INFO, engine.toString() ) );
        DescribedValue[] matchParams = engine.getMatchParameters();
        for ( int i = 0; i < matchParams.length; i++ ) {
            params.add( matchParams[ i ] );
        }
        for ( int i = 0; i < effTables.length; i++ ) {
            String id = null;
            if ( id == null ) {
                URL url = table.getURL();
                if ( url != null ) {
                    id = url.toString();
                }
            }
            if ( id == null ) {
                id = table.getName();
            }
            if ( id == null ) {
                id = "(virtual)";
            }
            ValueInfo idInfo =
                new DefaultValueInfo( "Matched table " + i, String.class,
                                      "Table on which the match was done" );
            params.add( new DescribedValue( idInfo, id ) );
        }
    }
}

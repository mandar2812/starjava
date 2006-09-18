package uk.ac.starlink.ttools.task;

import uk.ac.starlink.table.ColumnData;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.Tables;
import uk.ac.starlink.ttools.QuickTable;
import uk.ac.starlink.ttools.TableTestCase;

public class TableCat2Test extends TableTestCase {

    public TableCat2Test( String name ) {
        super( name );
    }

    public void test2() throws Exception {
        StarTable t1 = new QuickTable( 2, new ColumnData[] {
            col( "index", new int[] { 1, 2 } ),
            col( "name", new String[] { "milo", "theo", } ),
        } );

        StarTable t2 = new QuickTable( 2, new ColumnData[] {
            col( "ix", new int[] { 1, 2 } ),
            col( "atkname", new String[] { "charlotte", "jonathon", } ),
        } );

        MapEnvironment env2 = new MapEnvironment()
                             .setValue( "in1", t1 )
                             .setValue( "in2", t2 );
        new TableCat2().createExecutable( env2 ).execute();
        StarTable out2 = env2.getOutputTable( "omode" );

        MapEnvironment envN = new MapEnvironment()
                             .setValue( "in", new StarTable[] { t1, t2, } );
        new TableCat().createExecutable( envN ).execute();
        StarTable outN = envN.getOutputTable( "omode" );

        Tables.checkTable( out2 );
        Tables.checkTable( outN );

        assertSameData( out2, outN );

        assertArrayEquals( new String[] { "index", "name" },
                           getColNames( out2 ) );
        assertArrayEquals( box( new int[] { 1, 2, 1, 2, } ),
                           getColData( out2, 0 ) );
        assertArrayEquals( new Object[] { "milo", "theo",
                                          "charlotte", "jonathon", },
                           getColData( out2, 1 ) );
    }
}

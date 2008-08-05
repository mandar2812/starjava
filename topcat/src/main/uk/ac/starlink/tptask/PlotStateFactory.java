package uk.ac.starlink.tptask;

import gnu.jel.CompilationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.task.BooleanParameter;
import uk.ac.starlink.task.DoubleParameter;
import uk.ac.starlink.task.Environment;
import uk.ac.starlink.task.ExecutionException;
import uk.ac.starlink.task.Parameter;
import uk.ac.starlink.task.TaskException;
import uk.ac.starlink.tplot.DataBounds;
import uk.ac.starlink.tplot.MarkStyles;
import uk.ac.starlink.tplot.MultiPlotData;
import uk.ac.starlink.tplot.PlotData;
import uk.ac.starlink.tplot.PlotState;
import uk.ac.starlink.tplot.Style;
import uk.ac.starlink.tplot.StyleSet;
import uk.ac.starlink.tplot.TablePlot;
import uk.ac.starlink.ttools.task.ConsumerTask;
import uk.ac.starlink.ttools.task.FilterParameter;
import uk.ac.starlink.ttools.task.InputTableParameter;
import uk.ac.starlink.ttools.task.TableProducer;

/**
 * Obtains a {@link uk.ac.starlink.tplot.PlotState} and associated
 * {@link uk.ac.starlink.tplot.PlotData} from the execution environment.
 *
 * @author   Mark Taylor
 * @since    22 Apr 2008
 */
public class PlotStateFactory {

    private static final String TABLE_PREFIX = "in";
    private static final String FILTER_PREFIX = "cmd";
    private static final String SUBSET_PREFIX = "subset";
    private static final String TABLE_VARIABLE = "N";
    private static final String SUBSET_VARIABLE = "s";
    private static final double PAD_RATIO = 0.02;

    private final String[] dimNames_;
    private final int ndim_;

    private final DoubleParameter[] loParams_;
    private final DoubleParameter[] hiParams_;
    private final BooleanParameter[] logParams_;
    private final BooleanParameter[] flipParams_;
    private final Parameter[] axlabelParams_;

    /**
     * Constructor.
     *
     * @param  dimNames  names of plot dimensions (typically "X", "Y", etc);
     *                   number of elements gives dimensionality of plot
     */
    public PlotStateFactory( String[] dimNames ) {
        dimNames_ = dimNames;
        ndim_ = dimNames_.length;

        loParams_ = new DoubleParameter[ ndim_ ];
        hiParams_ = new DoubleParameter[ ndim_ ];
        logParams_ = new BooleanParameter[ ndim_ ];
        flipParams_ = new BooleanParameter[ ndim_ ];
        axlabelParams_ = new Parameter[ ndim_ ];
        for ( int idim = 0; idim < ndim_; idim++ ) {
            String dimName = dimNames_[ idim ].toLowerCase();
            loParams_[ idim ] = new DoubleParameter( dimName + "lo" );
            loParams_[ idim].setNullPermitted( true );
            hiParams_[ idim ] = new DoubleParameter( dimName + "hi" );
            hiParams_[ idim ].setNullPermitted( true );
            logParams_[ idim ] = new BooleanParameter( dimName + "log" );
            logParams_[ idim ].setDefault( "false" );
            flipParams_[ idim ] = new BooleanParameter( dimName + "flip" );
            flipParams_[ idim ].setDefault( "false" );
            axlabelParams_[ idim ] = new Parameter( dimName + "label" );
            axlabelParams_[ idim ].setNullPermitted( true );
        }
    }

    /**
     * Returns the parameters associated with this object.
     * The returned list is intended for external use in documentation;
     * the parameter objects returned may or may not be those used for
     * obtaining values from a particular execution environment.
     * For this reason they may have names which are symbolic,
     * that is, represent possible parameter names.  Since actual parameter
     * names are dynamically determined, it is not possible to return an
     * exhaustive list.
     *
     * @return   array of parameters to be used for documentation
     */
    public Parameter[] getParameters() {

        /* Create and return a list of parameters some of which have
         * "example" suffixes.  In some cases the parameters which 
         * supply actual values to this factory are constructed as 
         * required elsewhere in this class. */
        String tSuffix = TABLE_VARIABLE;
        String stSuffix = TABLE_VARIABLE + SUBSET_VARIABLE;
        InputTableParameter inParam = createTableParameter( tSuffix );
        FilterParameter filterParam = createFilterParameter( tSuffix );
        List paramList = new ArrayList();
        paramList.add( inParam );
        paramList.add( inParam.getFormatParameter() );
        paramList.add( inParam.getStreamParameter() );
        paramList.add( filterParam );
        for ( int idim = 0; idim < ndim_; idim++ ) {
            paramList.add( createCoordParameter( tSuffix, idim ) );
        }
        paramList.addAll( Arrays.asList( loParams_ ) );
        paramList.addAll( Arrays.asList( hiParams_ ) );
        paramList.addAll( Arrays.asList( logParams_ ) );
        paramList.addAll( Arrays.asList( flipParams_ ) );
        paramList.addAll( Arrays.asList( axlabelParams_ ) );
        paramList.add( createLabelParameter( tSuffix ) );
        paramList.add( createSubsetExpressionParameter( stSuffix ) );
        paramList.add( createSubsetNameParameter( stSuffix ) );
        return (Parameter[]) paramList.toArray( new Parameter[ 0 ] );
    }

    /**
     * Returns a configured PlotState obtained from parameter values
     * specified by the given execution environment.
     *
     * @param   env  execution environment
     */
    public PlotState getPlotState( Environment env ) throws TaskException {
        PlotState state = createPlotState();
        configurePlotState( state, env );
        return state;
    }

    /**
     * Configures the range attributes of the given state, ensuring that they
     * have non-NaN values.
     * The default implementation currently fills in zero for lower limits
     * and 1 for upper limits if no other values have been specified.
     *
     * @param   state  plot state whose ranges will to be configured
     * @param   plot   table plot for which configuration is to be done
     */
    public void configureRanges( PlotState state, TablePlot plot )
            throws TaskException, IOException {

        /* Work out which limits, if any, need calculating. */
        boolean[] loCalcs = new boolean[ ndim_ ];
        boolean[] hiCalcs = new boolean[ ndim_ ];
        int ncalc = 0;
        for ( int idim = 0; idim < ndim_; idim++ ) {
            double[] range = state.getRanges()[ idim ];
            if ( Double.isNaN( range[ 0 ] ) ) {
                loCalcs[ idim ] = true;
                ncalc++;
            }
            if ( Double.isNaN( range[ 1 ] ) ) {
                hiCalcs[ idim ] = true;
                ncalc++;
            }
        }

        /* If none need calculating (all have been specified explicitly),
         * return with no further work. */
        if ( ncalc == 0 ) {
            return;
        }

        /* Otherwise, work through the plot data to locate bounds. */
        DataBounds bounds = plot.calculateBounds( state.getPlotData(), state );

        /* And fill in the plot state limits with the results. */
        for ( int idim = 0; idim < ndim_; idim++ ) {
            double[] stateRange = state.getRanges()[ idim ];
            double[] calcRange = bounds.getRanges()[ idim ].getBounds();
            if ( loCalcs[ idim ] ) {
                if ( ! hiCalcs[ idim ] && stateRange[ 1 ] <= calcRange[ 0 ] ) {
                    String msg =
                        "Supplied " + dimNames_[ idim ] + " upper bound (" +
                        stateRange[ 1 ] +
                        ") is less than data lower bound (" +
                        calcRange[ 0 ] +
                        ")";
                    throw new ExecutionException( msg );
                }
                stateRange[ 0 ] = calcRange[ 0 ];
            }
            if ( hiCalcs[ idim ] ) {
                if ( ! loCalcs[ idim ] && stateRange[ 0 ] >= calcRange[ 1 ] ) {
                    String msg = 
                        "Supplied " + dimNames_[ idim ] + " lower bound (" +
                        stateRange[ 0 ] +
                        ") is greater than data upper bound (" +
                        calcRange[ 1 ] +
                        ")";
                    throw new ExecutionException( msg );
                }
                stateRange[ 1 ] = calcRange[ 1 ];
            }
            assert stateRange[ 0 ] <= stateRange[ 1 ];

            /* If lower and upper bounds are equal, nudge them down and up
             * respectively by an arbitrary amount. */
            if ( stateRange[ 0 ] == stateRange[ 1 ] ) {
                double val = stateRange[ 0 ];
                if ( val == Math.floor( val ) ) {
                    stateRange[ 0 ]--;
                    stateRange[ 1 ]++;
                }
                else {
                    stateRange[ 0 ] = Math.floor( val );
                    stateRange[ 1 ] = Math.ceil( val );
                }
            }

            /* Otherwise, introduce padding for calculated bounds. */
            else {
                double pad = ( stateRange[ 1 ] - stateRange[ 0 ] ) * PAD_RATIO;
                if ( loCalcs[ idim ] ) {
                    stateRange[ 0 ] -= pad;
                }
                if ( hiCalcs[ idim ] ) {
                    stateRange[ 1 ] += pad;
                }
            }
            assert state.getRanges()[ idim ][ 0 ] 
                 < state.getRanges()[ idim ][ 1 ];
        }
    }

    /**
     * Creates a new unconfigured PlotState object suitable for configuration
     * by this factory.
     *
     * @return   plot state
     */
    protected PlotState createPlotState() {
        return new PlotState();
    }

    /**
     * Configures a PlotState object by examining parameter values in a
     * given execution environment.  Such an object was presumably 
     * previously created by a call to {@link #createPlotState}.
     *
     * @param   state  plot state to configure
     * @param   env   execution environment
     */
    protected void configurePlotState( PlotState state, Environment env )
            throws TaskException {
        String[] paramNames = env.getNames();

        /* Work out which parameter suffixes are being used to identify
         * different tables.  This is done by finding all the parameters
         * which start "table" and pulling off their suffixes.
         * These suffixes are then applied to other parameter stems
         * for obtaining other per-table parameter values. */
        String tPrefix = TABLE_PREFIX;
        String[] tableLabels = getSuffixes( paramNames, tPrefix );
        int nTable = tableLabels.length;

        /* Construct a PlotData object for the data obtained from each table. */
        PlotData[] datas = new PlotData[ nTable ];
        String[] coordExprs0 = null;
        StyleDispenser styleDispenser =
            new StyleDispenser( getStyleSet( env ) );
        for ( int itab = 0; itab < nTable; itab++ ) {
            String tlabel = tableLabels[ itab ];
            StarTable table = getTable( env, tlabel );
            String[] coordExprs = getCoordExpressions( env, tlabel );
            String labelExpr = getLabelExpression( env, tlabel );
            SubsetDef[] subsetDefs =
                getSubsetDefinitions( env, tlabel, styleDispenser );
            int nset = subsetDefs.length;
            String[] setExprs = new String[ nset ];
            String[] setNames = new String[ nset ];
            Style[] setStyles = new Style[ nset ];
            for ( int is = 0; is < nset; is++ ) {
                SubsetDef sdef = subsetDefs[ is ];
                setExprs[ is ] = sdef.expression_;
                setNames[ is ] = sdef.name_;
                setStyles[ is ] = sdef.style_;
            }
            try {
                datas[ itab ] =
                    new TablePlotData( table, coordExprs, labelExpr, setExprs,
                                       setNames, setStyles );
            }
            catch ( CompilationException e ) {
                throw new TaskException( e.getMessage(), e ); 
            }
            if ( itab == 0 ) {
                coordExprs0 = coordExprs;
            }
        }

        /* Set the plot data object of the plot state as an aggregation of
         * the data objects from all the input tables. */
        state.setPlotData( new MultiPlotData( datas ) );

        /* Configure non-table-based properties of the plot state. */
        state.setMainNdim( ndim_ );
        boolean[] logFlags = new boolean[ ndim_ ];
        boolean[] flipFlags = new boolean[ ndim_ ];
        for ( int idim = 0; idim < ndim_; idim++ ) {
            logFlags[ idim ] = logParams_[ idim ].booleanValue( env );
            flipFlags[ idim ] = flipParams_[ idim ].booleanValue( env );
        }
        state.setLogFlags( logFlags );
        state.setFlipFlags( flipFlags );
        state.setAxisLabels( getAxisLabels( env, coordExprs0 ) );
        state.setRanges( getFixedRanges( env ) );
    }

    /**
     * Obtains a table with a given table label from the environment.
     *
     * @param   env   execution environmento
     * @param   tlabel  table parameter label
     * @return   input table table
     */
    private StarTable getTable( Environment env, String tlabel )
            throws TaskException {
        TableProducer producer =
            ConsumerTask.createProducer( env, createFilterParameter( tlabel ),
                                              createTableParameter( tlabel ) );
        try {
            return producer.getTable();
        }
        catch ( IOException e ) {
            throw new ExecutionException( "Table processing error", e );
        }
    }

    /**
     * Obtains the point coordinates expressions from the environment
     * for a given table.
     *
     * @param   env  execution environment
     * @param   tlabel  table parameter label
     * @return  ndim-element array of point coordinate JEL expressions
     */
    private String[] getCoordExpressions( Environment env, String tlabel )
            throws TaskException {
        String[] coordExprs = new String[ ndim_ ];
        for ( int idim = 0; idim < ndim_; idim++ ) {
            coordExprs[ idim ] = createCoordParameter( tlabel, idim )
                                .stringValue( env );
        }
        return coordExprs;
    }

    /**
     * Obtains the text label expression from the environment
     * for a given table.
     *
     * @param  env  execution environment
     * @param  tlabel   table parameter label
     * @return   text label JEL expression (may be null)
     */
    private String getLabelExpression( Environment env, String tlabel )
            throws TaskException {
        return createLabelParameter( tlabel ).stringValue( env );
    }

    /**
     * Obtains the subset definition object from the environment
     * for a given table.
     *
     * @param  env  execution environment
     * @param  tlabel   table parameter label
     * @param  styleDispenser  style factory object which can supply default
     *         plotting styles in the absence of explicitly selected ones
     */
    private SubsetDef[] getSubsetDefinitions( Environment env, String tlabel,
                                              StyleDispenser styleDispenser )
            throws TaskException {

        /* Work out which parameter suffixes are being used to identify
         * different subsets for the table with parameter suffix tlabel.
         * This is done by finding all the parameters which start 
         * "subset"+tlabel and pulling off their suffixes.  These suffixes
         * are then applied to other parameter stems for obtaining other
         * per-subset parameter values. */
        String[] paramNames = env.getNames();
        String stPrefix = SUBSET_PREFIX + tlabel;
        String[] subLabels = getSuffixes( paramNames, stPrefix );
        int nset = subLabels.length;

        /* If there are no subsets for this table, consider it the same as
         * a single subset with inclusion of all points. */
        if ( nset == 0 ) {
            return new SubsetDef[] {
                new SubsetDef( "true", tlabel, styleDispenser.getNextStyle() ),
            };
        }

        /* If there is at least one subset, gather the information required
         * to construct a SubsetDef object describing its characteristics. */
        else {
            SubsetDef[] sdefs = new SubsetDef[ nset ];
            for ( int is = 0; is < nset; is++ ) {
                String stLabel = tlabel + subLabels[ is ];
                String expr = createSubsetExpressionParameter( stLabel )
                             .stringValue( env );
                String name = createSubsetNameParameter( stLabel )
                             .stringValue( env );
                Style style = styleDispenser.getNextStyle();
                sdefs[ is ] = new SubsetDef( expr, name, style );
            }
            return sdefs;
        }
    }

    /**
     * Obtains the default ploting style set from the environment.
     * This determines plotting styles for subsets which do not specify 
     * style characteristics explicitly.
     *
     * @param   env  execution environment
     * @return   default plot style set
     */
    protected StyleSet getStyleSet( Environment env ) {

        /* Current implementation ignores the environment. */
        return MarkStyles.spots( "Spots", 2 );
    }

    /**
     * Obtains explicitly specified axis ranges from the environment.
     * Range values which are not explicitly specified are represented
     * as NaNs.
     *
     * @param  env  execution environment
     * @return  ndim-element array of per-dimension 
     *          2-element (low, high) fixed range limits
     */
    private double[][] getFixedRanges( Environment env ) throws TaskException {
        double[][] ranges = new double[ ndim_ ][];
        for ( int id = 0; id < ndim_; id++ ) {
            ranges[ id ] = new double[] {
                loParams_[ id ].doubleValue( env ),
                hiParams_[ id ].doubleValue( env ),
            };
        }
        return ranges;
    }

    /**
     * Obtains axis labels from the environment.
     *
     * @param   env  execution environment
     * @return  dflts  ndim-element array of default values for axis labels
     */
    private String[] getAxisLabels( Environment env, String[] dflts )
            throws TaskException {
        String[] labels = new String[ ndim_ ];
        for ( int id = 0; id < ndim_; id++ ) {
            String dflt = dflts[ id ];
            if ( dflt == null || dflt.trim().length() == 0 ) {
                dflt = dimNames_[ id ];
            }
            axlabelParams_[ id ].setDefault( dflt );
            labels[ id ] = axlabelParams_[ id ].stringValue( env );
        }
        return labels;
    }

    /**
     * Constructs an input table parameter with a given suffix.
     *
     * @param   tlabel  table parameter label
     * @return  new input table parameter
     */
    private InputTableParameter createTableParameter( String tlabel ) {
        return new InputTableParameter( TABLE_PREFIX + tlabel );
    }

    /**
     * Constructs an input filter parameter with a given suffix.
     *
     * @param   tlabel  table parameter label
     * @return  new table filter parameter
     */
    private FilterParameter createFilterParameter( String tlabel ) {
        return new FilterParameter( FILTER_PREFIX + tlabel );
    }

    /**
     * Constructs a coordinate expression parameter.
     *
     * @param   tlabel  table parameter label
     * @param   idim    dimension index
     * @return  new coord expression parameter
     */
    private Parameter createCoordParameter( String tlabel, int idim ) {
        return new Parameter( dimNames_[ idim ].toLowerCase()
                            + "data" + tlabel );
    }

    /**
     * Constructs a new text label expression parameter.
     *
     * @param  tlabel   table parameter label
     * @return  new text label expression parameter
     */
    private Parameter createLabelParameter( String tlabel ) {
        Parameter labelParam = new Parameter( "txtlabel" + tlabel );
        labelParam.setNullPermitted( true );
        return labelParam;
    }

    /**
     * Constructs a new subset inclusion expression parameter.
     *
     * @param  stlabel  table/subset parameter label
     * @return  new subset expression parameter
     */
    private Parameter createSubsetExpressionParameter( String stlabel ) {
        return new Parameter( SUBSET_PREFIX + stlabel );
    }

    /**
     * Constructs a new subset name parameter. 
     *
     * @param  stlabel  table/subset parameter label
     * @return  new subset name parameter
     */
    private Parameter createSubsetNameParameter( String stlabel ) {
        Parameter nameParam = new Parameter( "setname" + stlabel );
        nameParam.setNullPermitted( true );
        return nameParam;
    }

    /**
     * Returns an array of unique identifying suffixes associated with a
     * given prefix from a list of strings.  Each string in <code>names</code>
     * is examined, and if it starts with <code>prefix</code> the prefix
     * part is removed and the remaining part is stored in a list for return.
     *
     * @param  names  list of strings for analysis
     * @param  prefix  common prefix from which to take suffixes
     * @return  array of unique suffix values
     */
    private static String[] getSuffixes( String[] names, String prefix ) {
        List suffixList = new ArrayList();
        for ( int i = 0; i < names.length; i++ ) {
            if ( names[ i ].toLowerCase().startsWith( prefix.toLowerCase() ) ) {
                String suffix = names[ i ].substring( prefix.length() );
                if ( ! suffixList.contains( suffix ) ) {
                    suffixList.add( suffix );
                }
            }
        }
        return (String[]) suffixList.toArray( new String[ 0 ] );
    }

    /**
     * Utility class which aggregates information about a Row Subset.
     */
    private static class SubsetDef {
        final String expression_;
        final String name_;
        final Style style_;

        /**
         * Constructor.
         *
         * @param  expression   boolean JEL expression defining inclusion
         * @param  name     subset name
         * @param  style    subset style
         */
        SubsetDef( String expression, String name, Style style ) {
            expression_ = expression;
            name_ = name;
            style_ = style;
        }
    }

    private static class StyleDispenser {
        StyleSet styleSet_;
        int is_;
        StyleDispenser( StyleSet styleSet ) {
            styleSet_ = styleSet;
        }
        Style getNextStyle() {
            return styleSet_.getStyle( is_++ );
        }
    }
}

package uk.ac.starlink.treeview;

/**
 * An abstract DataNodeBuilder providing a template for builders which
 * build nodes from instances of a given class.  This class doesn't
 * do anything clever, it's just a convenience for subclasses.
 */
public abstract class SimpleDataNodeBuilder extends DataNodeBuilder {

    private Class argClass;
    private String name;

    /**
     * Construct a new builder which will turn out DataNodes from
     * objects of a given class (or its subclasses).
     * 
     * @param  name   the name of this builder - this should normally be
     *                the classname of the DataNodes it will produce
     * @param  argClass  the class on which this node builder will operate
     */
    protected SimpleDataNodeBuilder( String name, Class argClass ) {
        this.name = name;
        this.argClass = argClass;
    }

    /**
     * Construct a new builder which will turn out DataNode of a given
     * class from objects of a given class.
     * Just invokes
     * <tt>SimpleDataNodeBuilder(nodeClass.getName(),argClass)</tt>.
     *
     * @param  nodeClass  the class of DataNode objects which this builder
     *                    will be building
     * @param  argClass   the class on which this node bulider will operate
     */
    protected SimpleDataNodeBuilder( Class nodeClass, Class argClass ) {
        this( nodeClass.getName(), argClass );
    }

    abstract public DataNode buildNode( Object obj ) throws NoSuchDataException;

    public boolean suitable( Class objClass ) {
        return argClass.isAssignableFrom( objClass );
    }

    public String toString() {
        return name + "(" + argClass.getName() + ")";
    }

}

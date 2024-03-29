package ac.technion.geoinfo.ssnTrj.spatial;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.geotools.factory.FactoryRegistryException;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.neo4j.gis.spatial.AbstractSearch;
import org.neo4j.gis.spatial.Constants;
//import org.neo4j.gis.spatial.DefaultLayer;
import org.neo4j.gis.spatial.GeometryEncoder;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.Listener;

import org.neo4j.gis.spatial.SpatialDatabaseException;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.SpatialDataset;
import org.neo4j.gis.spatial.SpatialIndexReader;
import org.neo4j.gis.spatial.SpatialIndexWriter;
import org.neo4j.gis.spatial.SpatialRelationshipTypes;
import org.neo4j.gis.spatial.WKBGeometryEncoder;
//import org.neo4j.gis.spatial.DefaultLayer.GuessGeometryTypeSearch;
//import org.neo4j.gis.spatial.DefaultLayer.NodeToGeometryIterable;
//import org.neo4j.gis.spatial.DefaultLayer.NodeToGeometryIterable.GeometryIterator;
import org.neo4j.gis.spatial.encoders.Configurable;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.Traverser.Order;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;


/**
 * Instances of Layer provide the ability for developers to add/remove and edit geometries
 * associated with a single dataset (or layer). This includes support for several storage
 * mechanisms, like in-node (geometries in properties) and sub-graph (geometries describe by the
 * graph). A Layer can be associated with a dataset. In cases where the dataset contains only one
 * layer, the layer itself is the dataset.
 */
public class DefaultLayerFix implements Constants, Layer, SpatialDataset {

    // Public methods
    
    public String getName() {
        return name;
    }

    public SpatialDatabaseService getSpatialDatabase() {
        return spatialDatabase;
    }
    
    public SpatialIndexReader getIndex() {
        return index;
    }

    /**
     * Add the geometry encoded in the given Node. This causes the geometry to appear in the index.
     */
    public SpatialDatabaseRecord add(Node geomNode) {
        Geometry geometry = getGeometryEncoder().decodeGeometry(geomNode);      
        
        index.add(geomNode);
        //return new SpatialDatabaseRecord(this, geomNode, geometry);
        return new SpatialDatabaseRecord(this, geomNode);
    }

    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }

    public void setCoordinateReferenceSystem(CoordinateReferenceSystem crs) {
        Node layerNode = getLayerNode();
        layerNode.setProperty(PROP_CRS, crs.toWKT());
    }   
    
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        Node layerNode = getLayerNode();
        if (layerNode.hasProperty(PROP_CRS)) {
            try {
                return ReferencingFactoryFinder.getCRSFactory(null).createFromWKT((String) layerNode.getProperty(PROP_CRS));
            } catch (FactoryRegistryException e) {
                throw new SpatialDatabaseException(e);
            } catch (FactoryException e) {
                throw new SpatialDatabaseException(e);
            }
        } else {
            return null;
        }
    }
    
    public void setGeometryType(Integer geometryType) {
        Node layerNode = getLayerNode();
        if (geometryType != null) {
            if (geometryType.intValue() < GTYPE_POINT || geometryType.intValue() > GTYPE_MULTIPOLYGON) {
                throw new IllegalArgumentException("Unknown geometry type: " + geometryType);
            }
            
            layerNode.setProperty(PROP_TYPE, geometryType);
        } else {
            layerNode.removeProperty(PROP_TYPE);
        }
    }
    
    public Integer getGeometryType() {
        Node layerNode = getLayerNode();
        if (layerNode.hasProperty(PROP_TYPE)) {
            return (Integer) layerNode.getProperty(PROP_TYPE);
        } else {
            GuessGeometryTypeSearch geomTypeSearch = new GuessGeometryTypeSearch();
            index.executeSearch(geomTypeSearch);
            if (geomTypeSearch.firstFoundType != null) {
                return geomTypeSearch.firstFoundType;
            } else {
                // layer is empty
                return null;
            }
        }
    }

    private static class GuessGeometryTypeSearch extends AbstractSearch {

        Integer firstFoundType;
            
        public boolean needsToVisit(Envelope indexNodeEnvelope) {
            return firstFoundType == null;
        }

        public void onIndexReference(Node geomNode) {
            if (firstFoundType == null) {
                firstFoundType = (Integer) geomNode.getProperty(PROP_TYPE);
            }
        }
    }

    public String[] getExtraPropertyNames() {
        Node layerNode = getLayerNode();
        if (layerNode.hasProperty(PROP_LAYERNODEEXTRAPROPS)) {
            return (String[]) layerNode.getProperty(PROP_LAYERNODEEXTRAPROPS);
        } else {
            return new String[] {};
        }
    }
    
    public void setExtraPropertyNames(String[] names) {
        Transaction tx = getDatabase().beginTx();
        try {
            getLayerNode().setProperty(PROP_LAYERNODEEXTRAPROPS, names);
            tx.success();
        } finally {
            tx.finish();
        }
    }
    
    public void mergeExtraPropertyNames(String[] names) {
        Node layerNode = getLayerNode();
        if (layerNode.hasProperty(PROP_LAYERNODEEXTRAPROPS)) {
            String[] actualNames = (String[]) layerNode.getProperty(PROP_LAYERNODEEXTRAPROPS);
            
            Set<String> mergedNames = new HashSet<String>();
            for (String name : names) mergedNames.add(name);
            for (String name : actualNames) mergedNames.add(name);

            layerNode.setProperty(PROP_LAYERNODEEXTRAPROPS, (String[]) mergedNames.toArray(new String[mergedNames.size()]));
        } else {
            layerNode.setProperty(PROP_LAYERNODEEXTRAPROPS, names);
        }
    }
    
    // Protected constructor
    protected DefaultLayerFix() {
    }
    
    /**
     * Factory method to construct a layer from an existing layerNode. This will read the layer
     * class from the layer node properties and construct the correct class from that.
     * 
     * @param spatialDatabase
     * @param layerNode
     * @return new layer instance from existing layer node
     */
    @SuppressWarnings("unchecked")
    protected static Layer makeLayerFromNode(SpatialDatabaseService spatialDatabase, Node layerNode) {
        try {
            String name = (String) layerNode.getProperty(PROP_LAYER);
            if (name == null) {
                return null;
            }
            
            String className = null;
            if (layerNode.hasProperty(PROP_LAYER_CLASS)) {
                className = (String) layerNode.getProperty(PROP_LAYER_CLASS);
            }
            
            Class<? extends Layer> layerClass = className == null ? Layer.class : (Class<? extends Layer>) Class.forName(className);
            return makeLayerInstance(spatialDatabase, name, layerNode, layerClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Factory method to construct a layer with the specified layer class. This can be used when
     * creating a layer for the first time. It will also construct the underlying Node in the graph.
     * 
     * @param spatialDatabase
     * @param name
     * @param layerClass
     * @return new Layer instance based on newly created layer Node
     */
    protected static Layer makeLayerAndNode(SpatialDatabaseService spatialDatabase, String name,
            Class< ? extends GeometryEncoder> geometryEncoderClass, Class< ? extends Layer> layerClass) {
        try {
            Node layerNode = spatialDatabase.getDatabase().createNode();
            layerNode.setProperty(PROP_LAYER, name);
            layerNode.setProperty(PROP_CREATIONTIME, System.currentTimeMillis());
            layerNode.setProperty(PROP_GEOMENCODER, geometryEncoderClass.getCanonicalName());
            layerNode.setProperty(PROP_LAYER_CLASS, layerClass.getCanonicalName());
            return DefaultLayerFix.makeLayerInstance(spatialDatabase, name, layerNode, layerClass);
        } catch (Exception e) {
            throw new SpatialDatabaseException(e);
        }
    }

    private static Layer makeLayerInstance(SpatialDatabaseService spatialDatabase, String name, Node layerNode, Class<? extends Layer> layerClass) throws InstantiationException, IllegalAccessException {
        if(layerClass == null) layerClass = Layer.class;
        Layer layer = layerClass.newInstance();
        layer.initialize(spatialDatabase, name, layerNode);
        return layer;
    }

    public void initialize(SpatialDatabaseService spatialDatabase, String name, Node layerNode) {
        this.spatialDatabase = spatialDatabase;
        this.name = name;
        this.layerNode = layerNode;
        this.index = new RTreeIndexFix(spatialDatabase.getDatabase(), this);
        
        // TODO read Precision Model and SRID from layer properties and use them to construct GeometryFactory
        this.geometryFactory = new GeometryFactory();
        
        if (layerNode.hasProperty(PROP_GEOMENCODER)) {
            String encoderClassName = (String) layerNode.getProperty(PROP_GEOMENCODER);
            try {
                this.geometryEncoder = (GeometryEncoder) Class.forName(encoderClassName).newInstance();
            } catch (Exception e) {
                throw new SpatialDatabaseException(e);
            }
			if (this.geometryEncoder instanceof Configurable) {
				if (layerNode.hasProperty(PROP_GEOMENCODER_CONFIG)) {
					((Configurable) this.geometryEncoder).setConfiguration((String) layerNode.getProperty(PROP_GEOMENCODER_CONFIG));
				}
			}
        } else {
            this.geometryEncoder = new WKBGeometryEncoder();
        }
        this.geometryEncoder.init(this);
    }
    
    /**
     * All layers are associated with a single node in the database. This node will have properties,
     * relationships (sub-graph) or both to describe the contents of the layer
     */
    public Node getLayerNode() {
        return layerNode;
    }
    
    /**
     * Delete Layer
     */
    public void delete(Listener monitor) {
        index.removeAll(true, monitor);

        Transaction tx = getDatabase().beginTx();
        try {
            Node layerNode = getLayerNode();
            layerNode.getSingleRelationship(SpatialRelationshipTypes.LAYER, Direction.INCOMING).delete();
            layerNode.delete();
            
            tx.success();
        } finally {
            tx.finish();
        }
    }
    
    
    // Private methods
    
    protected GraphDatabaseService getDatabase() {
        return spatialDatabase.getDatabase();
    }
    
    
    // Attributes
    
    private SpatialDatabaseService spatialDatabase;
    private String name;
    protected Node layerNode;
    protected GeometryEncoder geometryEncoder;
    protected GeometryFactory geometryFactory;
    protected SpatialIndexWriter index;
    
    public SpatialDataset getDataset() {
        return this;
    }

    /**
     * Provides a method for iterating over all nodes that represent geometries in this dataset.
     * This is similar to the getAllNodes() methods from GraphDatabaseService but will only return
     * nodes that this dataset considers its own, and can be passed to the GeometryEncoder to
     * generate a Geometry. There is no restricting on a node belonging to multiple datasets, or
     * multiple layers within the same dataset.
     * 
     * @return iterable over geometry nodes in the dataset
     */
    //this method  will not work with ssnLayers
    public Iterable<Node> getAllGeometryNodes() {
        return layerNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL_BUT_START_NODE,
                SpatialRelationshipTypes.GEOMETRIES, Direction.OUTGOING, SpatialRelationshipTypes.NEXT_GEOM, Direction.OUTGOING);
    }

    /**
     * Provides a method for iterating over all geometries in this dataset. This is similar to the
     * getAllGeometryNodes() method but internally converts the Node to a Geometry.
     * 
     * @return iterable over geometries in the dataset
     */
    public Iterable<? extends Geometry> getAllGeometries() {
        return new NodeToGeometryIterable(getAllGeometryNodes());
    }
    
    /**
     * In order to wrap one iterable or iterator in another that converts the objects from one type
     * to another without loading all into memory, we need to use this ugly java-magic. Man, I miss
     * Ruby right now!
     * 
     * @author craig
     * @since 1.0.0
     */
    private class NodeToGeometryIterable implements Iterable<Geometry>  {
        private Iterator<Node> allGeometryNodeIterator;
        private class GeometryIterator implements Iterator<Geometry> {

            public boolean hasNext() {
                return NodeToGeometryIterable.this.allGeometryNodeIterator.hasNext();
            }

            public Geometry next() {
                return geometryEncoder.decodeGeometry(NodeToGeometryIterable.this.allGeometryNodeIterator.next());
            }

            public void remove() {
            }
            
        }
        public NodeToGeometryIterable(Iterable<Node> allGeometryNodes) {
            this.allGeometryNodeIterator = allGeometryNodes.iterator();
        }

        public Iterator<Geometry> iterator() {
            return new GeometryIterator();
        }
        
    }

    /**
     * Return the geometry encoder used by this SpatialDataset to convert individual geometries to
     * and from the database structure.
     * 
     * @return GeometryEncoder for this dataset
     */
    public GeometryEncoder getGeometryEncoder() {
        return geometryEncoder;
    }

    /**
     * This dataset contains only one layer, itself.
     * 
     * @return iterable over all Layers that can be viewed from this dataset
     */
    public Iterable< ? extends Layer> getLayers() {
        return Arrays.asList(new Layer[]{this});
    }

	/**
	 * Override this method to provide a style if your layer wishes to control
	 * its own rendering in the GIS. If a Style is returned, it is used. If a
	 * File is returned, it is opened and assumed to contain SLD contents. If a
	 * String is returned, it is assumed to contain SLD contents.
	 * 
	 * @return null
	 */
	public Object getStyle() {
		return null;
	}

}

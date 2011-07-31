package ac.technion.geoinfo.ssnTrj.domain;

import org.neo4j.gis.spatial.Constants;
import org.neo4j.graphdb.Node;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

public class SpatialEntityImpl extends NodeWarpperImpl implements SpatialEntity, Constants , Static {

	Geometry geometry = null;
	
	public SpatialEntityImpl(NodeWarpperImpl theNode) throws Exception
	{
		super(theNode);
	}
	
	public SpatialEntityImpl(Node theNode) throws Exception
	{
		super(new NodeWarpperImpl(theNode));
	}
	
	public String getGeometryAsString() throws Exception {
		// TODO Auto-generated method stub
		return this.getGeometry().toText();
	}

	public Geometry getGeometry() throws Exception
	{
		if (geometry == null)
		{
			if (!underlayingNode.hasProperty(PROP_WKT))
				throw new Exception("node " + underlayingNode.getId() + " has no geometry property");
			WKTReader reader = new WKTReader();
			this.geometry = reader.read((String)underlayingNode.getProperty(GEOMETRY));
		}
		return this.geometry;
	}

}

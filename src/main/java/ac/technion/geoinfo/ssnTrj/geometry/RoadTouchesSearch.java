package ac.technion.geoinfo.ssnTrj.geometry;

import org.neo4j.gis.spatial.query.AbstractSearchIntersection;
import org.neo4j.graphdb.Node;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class RoadTouchesSearch extends AbstractSearchIntersection {

	public RoadTouchesSearch(Geometry other) {
		super(other);
	}

	protected void onEnvelopeIntersection(Node geomNode, Envelope geomEnvelope) {
		Geometry geometry = decode(geomNode);
		if (geometry.getGeometryType().equalsIgnoreCase("LineString")) {
			if (geometry.touches(other)) {	
				add(geomNode, geometry);
			}
		}
	}

}

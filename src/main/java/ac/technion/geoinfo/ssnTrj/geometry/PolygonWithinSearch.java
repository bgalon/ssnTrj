package ac.technion.geoinfo.ssnTrj.geometry;

import org.neo4j.gis.spatial.query.AbstractSearchIntersection;
import org.neo4j.graphdb.Node;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class PolygonWithinSearch extends AbstractSearchIntersection {

	public PolygonWithinSearch(Geometry other) {
		super(other);
	}

	protected void onEnvelopeIntersection(Node geomNode, Envelope geomEnvelope) {
		Geometry geometry = decode(geomNode);
		if (geometry.getGeometryType().equalsIgnoreCase("polygon")) {
			if (geometry.within(other)) {	
				add(geomNode, geometry);
			}
		}
	}
}
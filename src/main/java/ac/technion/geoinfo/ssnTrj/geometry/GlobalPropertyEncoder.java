package ac.technion.geoinfo.ssnTrj.geometry;

import org.neo4j.gis.spatial.AbstractGeometryEncoder;
import org.neo4j.gis.spatial.Constants;
import org.neo4j.gis.spatial.SpatialDatabaseException;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.graphdb.PropertyContainer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class GlobalPropertyEncoder extends AbstractGeometryEncoder implements Constants {
	protected GeometryFactory geometryFactory;

	protected GeometryFactory getGeometryFactory() {
		if(geometryFactory==null) geometryFactory = new GeometryFactory();
		return geometryFactory;
	}

	@Override
	protected void encodeGeometryShape(Geometry geometry, PropertyContainer container) {
		
		container.setProperty("gtype", SpatialDatabaseService.convertJtsClassToGeometryType(geometry.getClass()));
		Coordinate[] coords = geometry.getCoordinates();
		float[] data = new float[coords.length * 2];
		for (int i = 0; i < coords.length; i++) {
			data[i * 2 + 0] = (float) coords[i].x;
			data[i * 2 + 1] = (float) coords[i].y;
		}
		container.setProperty("data", data);
	}

	public Geometry decodeGeometry(PropertyContainer container) {
		float[] data = (float[]) container.getProperty("data");
		Coordinate[] coordinates = new Coordinate[data.length / 2];
		for (int i = 0; i < data.length / 2; i++) {
			coordinates[i] = new Coordinate(data[2 * i + 0], data[2 * i + 1]);
		}
		int gtype = (Integer) container.getProperty("gtype");
		if (gtype == GTYPE_POINT)
		{
			return getGeometryFactory().createPoint(coordinates[0]);
		}
		else if (gtype == GTYPE_LINESTRING)
		{
			return getGeometryFactory().createLineString(coordinates);
		}
		else if (gtype == GTYPE_POLYGON)
		{
			return getGeometryFactory().createPolygon(getGeometryFactory().createLinearRing(coordinates), null);
		}
		else
		{
			throw new SpatialDatabaseException("fail to decode geometry of type" + gtype);
		}
	}	
}


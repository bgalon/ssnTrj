package ac.technion.geoinfo.ssnTrj.domain;

import com.vividsolutions.jts.geom.Geometry;

public interface SpatialEntity extends NodeWrapper {
	String getGeometryAsString() throws Exception;
	Geometry getGeometry() throws Exception;
	//String getSpatialId();
}

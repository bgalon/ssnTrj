package ac.technion.geoinfo.ssnTrj.domain;

import java.util.Collection;

public interface Route extends SpatialEntity{
	
	SpatialEntity getStart() throws Exception;
	SpatialEntity getEnd() throws Exception;
	
	Collection<SpatialEntity> getSegments() throws Exception;
	
	String RouteAsString() throws Exception;
}

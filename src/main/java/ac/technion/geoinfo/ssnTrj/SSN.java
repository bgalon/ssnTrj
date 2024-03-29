package ac.technion.geoinfo.ssnTrj;

import java.util.List;

import org.neo4j.gis.spatial.Search;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

import com.vividsolutions.jts.geom.GeometryFactory;

import ac.technion.geoinfo.ssnTrj.domain.Route;
import ac.technion.geoinfo.ssnTrj.domain.SpatialEntity;
import ac.technion.geoinfo.ssnTrj.domain.TimePattern;
import ac.technion.geoinfo.ssnTrj.domain.User;

public interface SSN {
	//List<SpatialEntity> AddLocation(String geom,String spatialType, String[] attributes, Object[] values) throws Exception;
	List<SpatialEntity> AddBuilding(String geom, String[] attributes, Object[] values) throws Exception;
	List<SpatialEntity> AddRoadSegment(String geom, String[] attributes, Object[] values) throws Exception;
	SpatialEntity AddSpatialGroup(String geom,String[] spatialTypes, String[] attributes, Object[] values) throws Exception;
	User AddUser(String uName, String[] friendsUname, String[] relationType, String[] attributes, String[] values) throws Exception;
	TimePattern addPattren(User theUser, SpatialEntity theSE, String TimePattrenAsStr, double confident) throws Exception;
	Route addRoute(SpatialEntity start, SpatialEntity end, SpatialEntity[] segments) throws Exception;
	
	void executeSpatialSearch(Search theSearch, String layer) throws Exception;
	
	GeometryFactory getGeometryFactory();
	
	Index<Node> getSpatialIndex();
	Index<Node> getSocialIndex();
	
	void Dispose();
}

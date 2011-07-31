package ac.technion.geoinfo.ssnTrj;

import java.util.List;

import org.neo4j.gis.spatial.Search;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

import com.vividsolutions.jts.geom.GeometryFactory;

import ac.technion.geoinfo.ssnTrj.domain.SpatialEntity;
import ac.technion.geoinfo.ssnTrj.domain.User;

public interface SSN {
	List<SpatialEntity> AddLocation(String geom, String[] attributes, Object[] values) throws Exception;
	User AddUser();
	
	void executeSpatialSearch(Search theSearch);
	
	GeometryFactory getGeometryFactory();
	
	Index<Node> getSpatialIndex();
	Index<Node> getSocialIndex();
	
	void Dispose();
}

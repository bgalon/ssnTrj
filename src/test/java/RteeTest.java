import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.EditableLayerImpl;
import org.neo4j.gis.spatial.RTreeIndex;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.WKTGeometryEncoder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import ac.technion.geoinfo.ssnTrj.SSN;
import ac.technion.geoinfo.ssnTrj.spatial.RTreeIndexFix;
import ac.technion.geoinfo.ssnTrj.spatial.SsnSpatialLayer;

import com.vividsolutions.jts.geom.Coordinate;


public class RteeTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final String dbFolder  = "c:\\RtreeTest";
		GraphDatabaseService graphDB = new EmbeddedGraphDatabase(dbFolder);
		SpatialDatabaseService sgDB = new SpatialDatabaseService(graphDB);
		
		EditableLayer testLayer = (EditableLayer)sgDB.createLayer("RtreeTest", WKTGeometryEncoder.class, SsnSpatialLayer.class); //EditableLayerImpl.class);
		
		for (int i = 0; i < 15; i++){
			for (int j = 0; j < 10; j++){
				testLayer.add(testLayer.getGeometryFactory().createPoint(new Coordinate(i, j)));
			}
		}
		
		((RTreeIndexFix)testLayer.getIndex()).debugIndexTree();
		testLayer.delete(60); // raise an java.lang.NullPointerException exception
		
		((RTreeIndexFix)testLayer.getIndex()).debugIndexTree();
		
		graphDB.shutdown();
	}

}

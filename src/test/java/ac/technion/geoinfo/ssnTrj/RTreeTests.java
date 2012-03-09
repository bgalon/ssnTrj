package ac.technion.geoinfo.ssnTrj;

import java.util.Date;

import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.query.SearchInRelation;
import org.neo4j.gis.spatial.query.SearchIntersectWindow;

import ac.technion.geoinfo.ssnTrj.domain.Static;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

public class RTreeTests implements Static {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		final String dbPath = "D:\\neo4j-enterprise-1.5\\data\\graph.db";
		
		SSNonGraph ssn = new SSNonGraph(dbPath); 
		EditableLayer thelayer = (EditableLayer)ssn.getLayer(SPATIAL_LAYER);
		String envelope = "37.807787308,-122.40671473,37.790764434,-122.441132836";
		
		String[] splitStr = envelope.split(",");
		Coordinate[] coordinates = new Coordinate[5];
		coordinates[0] = coordinates[4] = new Coordinate(Double.parseDouble(splitStr[2]),Double.parseDouble(splitStr[1]));
		coordinates[1] = new Coordinate(Double.parseDouble(splitStr[0]),Double.parseDouble(splitStr[1]));
		coordinates[2] = new Coordinate(Double.parseDouble(splitStr[0]),Double.parseDouble(splitStr[3]));
		coordinates[3] = new Coordinate(Double.parseDouble(splitStr[2]),Double.parseDouble(splitStr[3]));

		Polygon other = thelayer.getGeometryFactory().createPolygon(thelayer.getGeometryFactory().createLinearRing(coordinates),null);
		//SearchInRelation search = new SearchInRelation(other, "T*****T**");
		SearchIntersectWindow search = new SearchIntersectWindow(other.getEnvelopeInternal());
		
		System.out.println("Start");
		long startTime = System.currentTimeMillis(); 
		thelayer.getIndex().executeSearch(search);
		long totalTime = System.currentTimeMillis() - startTime;
		System.out.println("Done runing time is: " + totalTime / 1000 + " Sec");

	}

}

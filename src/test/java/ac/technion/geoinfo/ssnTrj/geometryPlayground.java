package ac.technion.geoinfo.ssnTrj;

import java.util.LinkedList;
import java.util.List;

import org.geotools.geometry.jts.JTSFactoryFinder;

import com.vividsolutions.jts.algorithm.distance.DistanceToPoint;
import com.vividsolutions.jts.algorithm.distance.PointPairDistance; 
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.linemerge.LineMerger;

public class geometryPlayground {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception { 
        GeometryFactory gf = JTSFactoryFinder.getGeometryFactory(null); 
        WKTReader reader = new WKTReader(gf); 
        Geometry line1 = reader.read("LINESTRING(0 0, 10 0, 10 10, 20 10)");
        Geometry line2 = reader.read("LINESTRING(20 10, 30 10, 30 0, 40 10)");
        Geometry line3 = reader.read("LINESTRING(40 10,30 0,30 10,20 10)");
        
        LineMerger lineMerger = new LineMerger();
        lineMerger.add(line1);
        lineMerger.add(line3);
        
        Geometry mergedLine = (Geometry) lineMerger.getMergedLineStrings().iterator().next();
        
        System.out.println((Geometry) lineMerger.getMergedLineStrings().iterator().next());
        
        Coordinate c = new Coordinate(5, 5);
        

        PointPairDistance ppd = new PointPairDistance(); 
        DistanceToPoint.computeDistance((Geometry) lineMerger.getMergedLineStrings().iterator().next(), c, ppd); 

        System.out.println(ppd.getDistance()); 

        for (Coordinate cc : ppd.getCoordinates()) { 
            System.out.println(cc); 
        } 
    } 
}

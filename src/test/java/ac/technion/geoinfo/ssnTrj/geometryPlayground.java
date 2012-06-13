package ac.technion.geoinfo.ssnTrj;


import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;


public class geometryPlayground {

	private static String GBNG  =  "PROJCS[\"British_National_Grid\",GEOGCS[\"GCS_OSGB_1936\",DATUM[\"D_OSGB_1936\",SPHEROID[\"Airy_1830\",6377563.396,299.3249646]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",400000],PARAMETER[\"False_Northing\",-100000],PARAMETER[\"Central_Meridian\",-2],PARAMETER[\"Scale_Factor\",0.999601272],PARAMETER[\"Latitude_Of_Origin\",49],UNIT[\"Meter\",1]]";
	
	public static void main(String[] args) throws Exception { 
		
		 // Example data: vertex coords of a unit rectangle
        double[] x = { 0.489, 0.489, 0.236, 0.236}; // longitudes
        double[] y = { 51.2, 51.686, 51.686, 51.2}; // latitudes

        // calculate area
        
        double area = getArea(x, y);
        System.out.println("Polygon area: " + area);

    } 

	
	private static double getArea(double[] lon, double[] lat) throws Exception {
        if (lon.length < 3 || lon.length != lat.length) {
            throw new IllegalArgumentException("Bummer: bad arguments");
        }
        final int N = lon.length;

        // Create the polygon
        GeometryFactory geomFactory = new GeometryFactory();
        Coordinate[] coords = new Coordinate[N + 1];

        for (int i = 0; i < N; i++) {
            // remember X = longitude, Y = latitude !
            coords[i] = new Coordinate(lon[i], lat[i]);
        }
        // closing coordinate (same as first coord
        coords[N] = new Coordinate(coords[0]);

        LinearRing polygonBoundary = geomFactory.createLinearRing(coords);
        LinearRing[] polygonHoles = null;
        Geometry polygon = geomFactory.createPolygon(polygonBoundary, polygonHoles);

    
        // Create a MathTransform to convert the vertex coordinates from
        // lat-lon (assumed to be WGS84 in this example) to an equal area
        // projection.
        //
        // If there is a suitable map projection availble in GeoTools you
        // can retrieve it like this...
        //     US National Atlas Equal Area projection (code EPSG:2163)
        //     CoordinateReferenceSystem equalAreaCRS = CRS.decode("EPSG:2163", true);
        //
        // In this example we brew one for Brazil using a non-standard projection
        // cooked up by ESRI which we read from a file (see bottom of code for file
        // contents)
        
        CoordinateReferenceSystem equalAreaCRS = CRS.parseWKT(GBNG);
        MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, equalAreaCRS, true);

        // Reproject the polygon and return its area
        Geometry transformedPolygon = JTS.transform(polygon, transform);
        return transformedPolygon.getArea();
    }
}




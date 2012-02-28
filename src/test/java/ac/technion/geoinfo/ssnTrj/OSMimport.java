package ac.technion.geoinfo.ssnTrj;

import ac.technion.geoinfo.ssnTrj.domain.Static;
import ac.technion.geoinfo.ssnTrj.osm.OSMimporter;

public class OSMimport {

	/**
	 * @param args
	 * @throws Exception /
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		final String dbPath = "C:\\neo4j-enterprise-1.5\\data\\graph.db";
		
		//String osmFileNPath = "C:\\osmData\\Washington-border1.osm";
		//String osmFileNPath = "C:\\osmData\\Washington_varySmall.osm";
		//String osmFileNPath = "C:\\osmData\\Washington_testCase.osm";
		
		System.out.println("heap size is:" + Runtime.getRuntime().totalMemory() + " Bytes");
		
		String osmFileNPath = "C:\\osmData\\sfverySmall.osm";
//		String osmFileNPath = "C:\\osmData\\sf-bay-area.osm";

		
//		OSMimporter myOsmImporter = new OSMimporter(osmFileNPath,dbPath);
//		myOsmImporter.ImportRoads(0);
//		System.out.println("Done Roads");
//		System.out.println("heap size is:" + Runtime.getRuntime().totalMemory() + " Bytes");
//		
//		myOsmImporter.ImportBulidings(0);
//		System.out.println("Done Bulidings");
//		System.out.println("heap size is:" + Runtime.getRuntime().totalMemory() + " Bytes");
//		myOsmImporter.Dispose();
		
		OSMimporter myOsmImporter = new OSMimporter(osmFileNPath,dbPath);
		myOsmImporter.StreamImportRoadsNBuildings(1000, 0);
		System.out.println("Done Strem Import");
		myOsmImporter.Dispose();
		
		osmFileNPath = "C:\\osmData\\sf_neighborhood.osm";
		myOsmImporter = new OSMimporter(osmFileNPath,dbPath);
		myOsmImporter.ImportBorders(new String[]{Static.BULIDING, Static.ROAD_SEGMENT});
		myOsmImporter.Dispose();
		System.out.println("heap size is:" + Runtime.getRuntime().totalMemory() + " Bytes");
		
		osmFileNPath = "C:\\osmData\\bayarea_cities.osm";
		myOsmImporter = new OSMimporter(osmFileNPath,dbPath);
		myOsmImporter.ImportBorders(new String[]{Static.SPATIAL_GROUP});
		myOsmImporter.Dispose();
		System.out.println("heap size is:" + Runtime.getRuntime().totalMemory() + " Bytes");
		
		osmFileNPath = "C:\\osmData\\bayaera_county.osm";
		myOsmImporter = new OSMimporter(osmFileNPath,dbPath);
		myOsmImporter.ImportBorders(new String[]{Static.SPATIAL_GROUP});
		myOsmImporter.Dispose();
		System.out.println("heap size is:" + Runtime.getRuntime().totalMemory() + " Bytes");
		
		System.out.println("Done");
	}

}

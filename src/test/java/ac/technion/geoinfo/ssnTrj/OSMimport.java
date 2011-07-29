package ac.technion.geoinfo.ssnTrj;

import ac.technion.geoinfo.ssnTrj.OSMimpoter;

public class OSMimport {

	/**
	 * @param args
	 * @throws Exception /
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		final String dbPath = "C:\\neo4j-enterprise-1.4\\data\\graph.db";
		
		//String osmFileNPath = "C:\\osmData\\Washington-border1.osm";
		//String osmFileNPath = "C:\\osmData\\Washington_varySmall.osm";
		String osmFileNPath = "C:\\osmData\\Washington_testCase.osm";
		OSMimpoter myOsmImporter = new OSMimpoter(osmFileNPath,dbPath);
//		myOsmImporter.ImportRoads();
//		myOsmImporter.ImportBulidings();
//		myOsmImporter.Dispose();
		System.out.println("Done");
	}

}

package ac.technion.geoinfo.ssnTrj.apps;

import java.io.File;

import ac.technion.geoinfo.ssnTrj.osm.OSMimporter;

public class Import_OSM {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		/*
		Import_OSM options:
			-database database_path
			-osm osm file path
			[ -stream #reportAt
			  -roads
		 	  -buildings
		 	  -startAt #num
		 	  -borders [types Separates by comma no spaces]
		 	  -help (print this note)
		 	]	
		*/
		String databasePath = "";
		String osmFilePath ="";
		boolean roads = false;
		boolean buildings = false;
		boolean borders = false;
		boolean stream = false;
		String[] bordersTypes = null;
		int startAt = 0;
		int reportAt = 1000;
		
		for(int i = 0; i < args.length; i++)
		//finds the arguments
		{
			String thisArg = args[i];
			if(thisArg.trim().equalsIgnoreCase("-database"))
				databasePath = args[i + 1];
			if(thisArg.trim().equalsIgnoreCase("-osm"))
				osmFilePath = args[i + 1];
			if(thisArg.trim().equalsIgnoreCase("-roads"))
				roads = true;
			if(thisArg.trim().equalsIgnoreCase("-stream"))
			{
				stream = true;
				try 
				{
					reportAt = Integer.parseInt(args[i + 1]);
				} 
				catch (NumberFormatException e) {
					  System.out.println("can't parse report at number, use 1000");
				}
			}
			if(thisArg.trim().equalsIgnoreCase("-buildings"))
				buildings = true;
			if(thisArg.trim().equalsIgnoreCase("-startAt"))
			{
				try 
				{
					startAt = Integer.parseInt(args[i + 1]);
				} 
				catch (NumberFormatException e) {
					  System.out.println("can't parse start at number");
						return;
				}
			}
			if(thisArg.trim().equalsIgnoreCase("-borders"))
			{
				borders = true;
				String tempBordersTypes = args[i +1];
				tempBordersTypes = tempBordersTypes.substring(1, tempBordersTypes.trim().length() - 1);
				bordersTypes = tempBordersTypes.split(",");
			}
			
			if(thisArg.trim().equalsIgnoreCase("-help"))
			{
				PrintHelp();
				return;
			}
		}
		
		if(databasePath.isEmpty() && !databasePath.startsWith("-"))
		{
			System.out.println("no value is given for database");
			return;
		}
		
		if(osmFilePath.isEmpty() && !databasePath.startsWith("-"))
		{
			System.out.println("no value is given for osm file");
			return;
		}
		
		boolean osmExsist = (new File(osmFilePath)).exists();
		if(!osmExsist)
		{
			System.out.println("can not find the osm file");
			return;
		}
		
		if(roads & buildings & borders)
		{
			System.out.println("on operation has given, add -roads and/or -buildings and/or -borders");
			return;
		}
		
		OSMimporter myOsmImporter = new OSMimporter(osmFilePath, databasePath);
		try{
			System.out.println("heap size is:" + Runtime.getRuntime().totalMemory() + " Bytes");
			if (roads) 
			{
				System.out.println("start importing roads form " + osmFilePath);
				myOsmImporter.ImportRoads(startAt);
				System.out.println("done roads");
			}
			
			if (buildings) 
			{
				System.out.println("start importing buildings form " + osmFilePath);
				myOsmImporter.ImportBulidings(startAt);
				System.out.println("done buildings");
			}
			
			if (borders && bordersTypes != null)
			{
				System.out.print("start importing borders form " + osmFilePath + ". including spatial enteties of types:");
				for(String tempType:bordersTypes)
					System.out.print(tempType + " ");
				System.out.println(".");
				myOsmImporter.ImportBorders(bordersTypes);
				System.out.println("done borders");
			}
			
			if(stream)
			{
				System.out.println("start stream import form " + osmFilePath);
				myOsmImporter.StreamImportRoadsNBuildings(reportAt, startAt);
				System.out.println("done stream import");
			}
			System.out.println("heap size is:" + Runtime.getRuntime().totalMemory() + " Bytes");
		}
		finally
		{
			myOsmImporter.Dispose();
		}
	}
	
	static public void PrintHelp()
	{
		System.out.println("Import_OSM options:");
		System.out.println("-database database_path");
		System.out.println("-osm osm file path");
		System.out.println("[-stream #reportAt]");
		System.out.println("[-roads]");
		System.out.println("[-buildings]");
		System.out.println("[-startAt #num]");
		System.out.println("[-borders [types Separates by comma no spaces]]");
		System.out.println("-help (print this note)]");
	}

}

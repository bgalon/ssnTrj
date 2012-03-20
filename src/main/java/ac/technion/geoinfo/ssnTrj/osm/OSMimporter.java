package ac.technion.geoinfo.ssnTrj.osm;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import com.vividsolutions.jts.geom.Geometry;

import ac.technion.geoinfo.ssnTrj.SSN;
import ac.technion.geoinfo.ssnTrj.SSNonGraph;
import ac.technion.geoinfo.ssnTrj.domain.Static;


public class OSMimporter {

		private Map<Integer, OSMNode> NodeLst = null;
		
		private final String graphDBFloder, OSMFile;
		
		List<OSMWay> wayLst = null;
		
		private SSN ssn;
		
		
		public OSMimporter(String OSMFile, String grpahDBFolder) throws Exception
		{
			this.graphDBFloder = grpahDBFolder;
			this.OSMFile = OSMFile;
			ssn = new SSNonGraph(grpahDBFolder);
			createNodeList();
			System.out.println(NodeLst.size() + " Nodes has been load");
		}
		
		public void Dispose()
		{
			ssn.Dispose();
		}
		
		private void createNodeList() throws XMLStreamException
		{
			javax.xml.stream.XMLInputFactory factory = null;
			javax.xml.stream.XMLStreamReader parser = null;
			try {
				 factory = javax.xml.stream.XMLInputFactory.newInstance();
				 parser = factory.createXMLStreamReader(new FileReader(OSMFile));
				 NodeLst = new HashMap<Integer, OSMNode>();
				 int id;
				 double lat;
				 double lon;
				 while (parser.hasNext())
				 {
					 parser.next();
					 if (parser.getEventType() == XMLStreamConstants.START_ELEMENT)
					 {
						 if (parser.getLocalName().equals("node"))
						 {
							 id = 0;
							 lat = 0;
							 lon = 0;
							 for(int i = 0; i < parser.getAttributeCount(); i++)
							 {
								 if (parser.getAttributeLocalName(i).equals("id"))
								 {
									 id = Integer.parseInt(parser.getAttributeValue(i));
								 }
								 else if (parser.getAttributeLocalName(i).equals("lat"))
								 {
									 lat = Double.parseDouble(parser.getAttributeValue(i));
								 }
								 else if (parser.getAttributeLocalName(i).equals("lon"))
								 {
									 lon = Double.parseDouble(parser.getAttributeValue(i));
								 }
							 }
							 if((lat*lon*id == 0)) throw new Exception("error in id number " + id);
							 NodeLst.put(id, new OSMNode(lat, lon, id));
						 }
					 }
				 }	 
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println(e.getMessage());
			}
			finally
			{
				if (parser != null) 
					parser.close();
			}
		}
		
		private OSMWay getWay(javax.xml.stream.XMLStreamReader parser) throws XMLStreamException
		{
			 int id = 0;
			 for(int i = 0; i < parser.getAttributeCount(); i++)
			 {
				 if (parser.getAttributeLocalName(i).equals("id"))
				 {
					 id = Integer.parseInt(parser.getAttributeValue(i));
				 }
			 }
			 if (id == 0) return null;
			 OSMWay tempWay = new OSMWay(id);
			 boolean skipThisWay = false;
			 while (true)
				 //read node
			 {
				 parser.next();
				 if (parser.getEventType() == XMLStreamConstants.END_ELEMENT)
					 if (!parser.getLocalName().equals("nd"))
						 break;
				 if (parser.getEventType() == XMLStreamConstants.START_ELEMENT)
				 {
					 if (parser.getLocalName().equals("nd"))
					 {
						 int nodeId = 0;
						 for(int i = 0; i < parser.getAttributeCount(); i++)
						 {
							 if (parser.getAttributeLocalName(i).equals("ref"))
							 {
								 nodeId = Integer.parseInt(parser.getAttributeValue(i));
							 }
						 }
						 if (id == 0) continue;
						 if (NodeLst.get(nodeId) == null)
						 {
	//									 throw new Exception("node " + nodeId + " does not exsist in node List");
							 skipThisWay = true;
							 break;
						 }
						 tempWay.addNode(NodeLst.get(nodeId));
					 }
					 else
					 {
						 break;
					 }
				 }
			 }
			 
			 if (skipThisWay) return null;
			 
			 while (true)
				 //read attribtue
			 {
				 if (parser.getEventType() == XMLStreamConstants.END_ELEMENT)
					 if (!parser.getLocalName().equals("tag"))
						 break;
				 if (parser.getEventType() == XMLStreamConstants.START_ELEMENT)
				 {
					 if (parser.getLocalName().equals("tag"))
					 {
						 String key =null, value = null;
						 for(int i = 0; i < parser.getAttributeCount(); i++)
						 {
							 if (parser.getAttributeLocalName(i).equals("k"))
							 {
								 key = parser.getAttributeValue(i);
							 }
							 if (parser.getAttributeLocalName(i).equals("v"))
							 {
								 value = parser.getAttributeValue(i);
							 }
						 }
						 if( key != null && value != null)
						 {
							 tempWay.attributeLst.put(key, value);
						 }
					 }
					 else
					 {
						 break;
					 }
				 }
				 parser.next();
			 }
			 return tempWay;
		}
		
//		private Map<Integer, OSMWay> createWayList() throws XMLStreamException
		private List<OSMWay> createWayList() throws XMLStreamException
		{
			javax.xml.stream.XMLInputFactory factory = null;
			javax.xml.stream.XMLStreamReader parser = null;
//			Map<Integer, OSMWay> wayLst = null;
			List<OSMWay> wayLst = null;
			try {
				 factory = javax.xml.stream.XMLInputFactory.newInstance();
				 parser = factory.createXMLStreamReader(new FileReader(OSMFile));
//				 wayLst = new HashMap<Integer, OSMWay>();
//				 wayLst = new LinkedList<OSMimporter.OSMWay>();
				 wayLst = new ArrayList<OSMimporter.OSMWay>();
				 while (parser.hasNext())
				 {
					 parser.next();
					 if (parser.getEventType() == XMLStreamConstants.START_ELEMENT)
					 {
						 if (parser.getLocalName().equals("way"))
						 {
							 OSMWay tempWay = getWay(parser);
							 if (tempWay != null) wayLst.add(tempWay);
						 }
					 }
				 }
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println(e.getMessage());
			}
			finally
			{
				if (parser != null) 
					parser.close();
			}
			return wayLst;
		}
		
		public boolean StreamImportRoadsNBuildings(int printStatus, int satrtAt) throws XMLStreamException
		{
			javax.xml.stream.XMLInputFactory factory = null;
			javax.xml.stream.XMLStreamReader parser = null;
//			Map<Integer, OSMWay> wayLst = null;
			List<OSMWay> wayLst = null;
			int roadCounter = 0;
			int importsRoads = 0;
			int buildingCounter = 0;
			int importsBuildings = 0;
			int wayCounter = 0;
			int oldWayCounter = 0;
			try {
				 factory = javax.xml.stream.XMLInputFactory.newInstance();
				 parser = factory.createXMLStreamReader(new FileReader(OSMFile));

				 ShotdownRecovery thisShutdown = new ShotdownRecovery("shutdown while strem import");
				 Runtime.getRuntime().addShutdownHook(thisShutdown);
				 
				 while (parser.hasNext())
				 {
					 try 
					 {
						 parser.next();
						 if (parser.getEventType() == XMLStreamConstants.START_ELEMENT)
						 {
							 if (parser.getLocalName().equals("way"))
							 {
								 wayCounter ++;
								 OSMWay tempWay = getWay(parser);
								 if(wayCounter < satrtAt ) continue;
								 
								 if (tempWay != null)
								 {
									 if (tempWay.attributeLst.containsKey("highway"))
									 {
										 roadCounter ++;
										 if(ImportRoad(tempWay)) importsRoads++;
									 }
									 if (tempWay.attributeLst.containsKey("building"))
									 {
										 buildingCounter++;
										 if(ImportBuilding(tempWay)) importsBuildings++;
									 }
									 if(wayCounter - oldWayCounter >= printStatus)
									 {
										 System.out.println("Status report (" + new Date() + ")");
										 System.out.println(wayCounter + " ways have been read");
										 System.out.println(importsRoads + " roads have been saveds out of " + roadCounter + 
												 " (" + String.format("%,.2f", ((double)importsRoads/(double)roadCounter)*100) + "%)");
										 System.out.println(importsBuildings + " buildings have been saveds out of " + buildingCounter + 
												 " (" + String.format("%,.2f", ((double)importsBuildings/(double)buildingCounter)*100) + "%)");
										 oldWayCounter = wayCounter;
									 }
									 thisShutdown.SetRecoveryFrom(wayCounter, tempWay.osmId);
								 }
							 }
						 }
					 }
					 catch (OutOfMemoryError e) 
						{
							System.out.println("stoped at: " + wayCounter);
							System.out.println(MemeoryData());
							e.printStackTrace();
							throw e;
						}
						catch (StackOverflowError e) 
						{
							System.out.println("stoped at: " + wayCounter);
							System.out.println(MemeoryData());
							e.printStackTrace();
							throw e;
						}
						catch (Exception e) {
							System.out.println("error while imporing roads. " + e.getMessage() + " at way number " + wayCounter);
						}
				 }
				 System.out.println("Final report (" + new Date() + ")");
				 System.out.println(wayCounter + " ways have been read");
				 System.out.println(importsRoads + " roads have been saveds out of " + roadCounter + 
						 " (" + String.format("%,.2f", ((double)importsRoads/(double)roadCounter)*100) + "%)");
				 System.out.println(importsBuildings + " buildings have been saveds out of " + buildingCounter + 
						 " (" + String.format("%,.2f", ((double)importsBuildings/(double)buildingCounter)*100) + "%)");
				 
				 Runtime.getRuntime().removeShutdownHook(thisShutdown);
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println(e.getMessage());
			}
			finally
			{
				if (parser != null) 
					parser.close();
			}
			return true;
		}
		
		//public Map<Integer, OSMWay> ImportRoads(int StartFrom) throws Exception
		public boolean ImportRoads(int StartFrom) throws Exception
		{
			//Map<Integer, OSMWay> wayLst = createWayList();
			if (wayLst == null) 
			{
				wayLst = createWayList();
			}
			if (StartFrom >= wayLst.size()) 
			{
				System.out.println("start value (" + StartFrom + ") is higher from the way list size (" + wayLst.size() + ")");
				return false;
			}
			System.out.println(wayLst.size() + " Ways has been load");
			//GraphDatabaseService graphDB = null;
			int addedLocCounter = StartFrom;
			int locCounter = 0;
			double addedPercent = 0;
			int lstSize = wayLst.size();
			
			int i = -1;
			
			ShotdownRecovery thisShutdown = new ShotdownRecovery("shutdown while import roads");
			Runtime.getRuntime().addShutdownHook(thisShutdown);
			
			
			//graphDB = new EmbeddedGraphDatabase(graphDBFloder);
			//for (OSMWay tempWay:wayLst.values())
			
			System.out.println("start import form " + StartFrom + " at the way list");
			for (i = StartFrom; i < wayLst.size(); i++)
			{
				OSMWay tempWay = wayLst.get(i);
				try 
				{
					if (tempWay.attributeLst.containsKey("highway"))
					{
						if(ImportRoad(tempWay)) addedLocCounter++;	
							
						double newPercent = (double)addedLocCounter/(double)lstSize;
						if (newPercent - addedPercent > 0.05)
						{
							System.out.println(String.format("%,.2f", newPercent*100) + "% done at " + new Date());
							addedPercent = newPercent;
						}
					}
				}
				catch (OutOfMemoryError e) 
				{
					System.out.println("stoped at: " + i);
					System.out.println(MemeoryData());
					e.printStackTrace();
					throw e;
				}
				catch (StackOverflowError e) 
				{
					System.out.println("stoped at: " + i);
					System.out.println(MemeoryData());
					e.printStackTrace();
					throw e;
				}
				catch (Exception e) {
					System.out.println("error while imporing roads. " + e.getMessage() + " in osm way number " + tempWay.getID());
				}
				thisShutdown.SetRecoveryFrom(i, (wayLst.get(i)).osmId);
			}
			addedPercent = (double)addedLocCounter/(double)locCounter;
			System.out.println(addedLocCounter + " roads added " + String.format("%,.2f", addedPercent*100) + "% done at " + new Date());
			Runtime.getRuntime().removeShutdownHook(thisShutdown);
			return true;
		}
		
		private boolean ImportRoad(OSMWay tempWay) throws Exception
		{
			String type = "highway";
			String address = null;
			if (tempWay.attributeLst.containsKey("highway"))
			{
				if (tempWay.attributeLst.containsKey("name"))
				{
					address = tempWay.attributeLst.get("name") + " , " + String.valueOf(tempWay.getID());
				}
				else
				{
					address = String.valueOf(tempWay.getID());
				}
				type = type + ":" +  tempWay.attributeLst.get("highway");
				
				SpatialEntityParms result = BuildGeometryAndAtt(type, tempWay, "lineString");
				if (result != null && result.Success)
				{
					ssn.AddRoadSegment(result.Geometry, result.AttLst, result.ValueLst);
					return true;	
				}
			}
			return false;
		}
		
//		public Map<Integer, OSMWay> ImportBulidings() throws XMLStreamException
		public boolean ImportBulidings(int StartFrom) throws Exception
		{
			//Map<Integer, OSMWay> wayLst = createWayList();
			if (wayLst == null) 
			{
				wayLst = createWayList();
			}
			if (StartFrom >= wayLst.size()) 
			{
				System.out.println("start value (" + StartFrom + ") is higher from the way list size (" + wayLst.size() + ")");
				return false;
			}
			System.out.println(wayLst.size() + " Ways has been load");
			int addedLocCounter = StartFrom;
			int locCounter = 0;
			int lstSize = wayLst.size();
			double addedPercent = 0;
			
			int i = -1;
			
			ShotdownRecovery thisShutdown = new ShotdownRecovery("shutdown while import roads");
			Runtime.getRuntime().addShutdownHook(thisShutdown);
			
			//for (OSMWay tempWay:wayLst.values())
			System.out.println("start import form " + StartFrom + " at the way list");
			for (i = StartFrom; i < wayLst.size(); i++)
			{
				OSMWay tempWay = wayLst.get(i);
				try 
				{ 
					if (tempWay.attributeLst.containsKey("building"))
					{
						if (tempWay.attributeLst.get("building").equalsIgnoreCase("yes"))
						{
							if (ImportRoad(tempWay)) addedLocCounter++;	
							
							double newPercent = (double)addedLocCounter/(double)lstSize;
							if (newPercent - addedPercent > 0.05)
							{
								System.out.println(String.format("%,.2f",newPercent*100) + "% done at " + new Date());
								addedPercent = newPercent;
							}
						}
					}
				}
				catch (OutOfMemoryError e) 
				{
					System.out.println("stoped at: " + i);
					System.out.println(MemeoryData());
					e.printStackTrace();
					throw e;
				}
				catch (StackOverflowError e) 
				{
					System.out.println("stoped at: " + i);
					System.out.println(MemeoryData());
					e.printStackTrace();
					throw e;
				}
				catch (Exception e) {
					System.out.println("error while imporing buildings. " + e.getMessage() + " in osm way number " + tempWay.getID());
				}
				thisShutdown.SetRecoveryFrom(i, (wayLst.get(i)).osmId);
			}	
			addedPercent = (double)addedLocCounter/(double)locCounter;
			System.out.println(addedLocCounter + " roads added " + String.format("%,.2f", addedPercent*100) + "% done at " + new Date());
			Runtime.getRuntime().removeShutdownHook(thisShutdown);
			return true;
		}
		
		private boolean ImportBuilding(OSMWay tempWay) throws Exception
		{
			String address = null;
			String type = "building";
			if (tempWay.attributeLst.containsKey("building"))
			{
				if (tempWay.attributeLst.get("building").equalsIgnoreCase("yes"))
				{
					if (tempWay.attributeLst.containsKey("addr:street"))
					{
						address = tempWay.attributeLst.get("addr:street");
						if (tempWay.attributeLst.containsKey("addr:housenumber"))
						{
							address = address + " " + tempWay.attributeLst.get("addr:housenumber");
						}
						else
						{
							address = address + "," + String.valueOf(tempWay.getID());
						}
					}
					else
					{
						address = String.valueOf(tempWay.getID());
					}
					if ((tempWay.attributeLst.containsKey("amenity")))
					{
						type = type + " , " + tempWay.attributeLst.get("amenity");
					}
//					type = type + ":" +  tempWay.attributeLst.get("highway");
//					if (addLocation(Static.BULIDING, tempWay,"polygon"))
//						addedLocCounter ++;
					SpatialEntityParms result = BuildGeometryAndAtt(type, tempWay, "polygon");
					if (result != null && result.Success)
					{
						ssn.AddBuilding(result.Geometry, result.AttLst, result.ValueLst);
						return true;
					}
				}
			}
			return false;
		}
		
		/*
		public Map<Integer, OSMWay> ImportParks() throws XMLStreamException
		{
			Map<Integer, OSMWay> wayLst = createWayList();
			System.out.println(wayLst.size() + " Ways has been load");
			GraphDatabaseService graphDB = null;
			int addedLocCounter = 0;
			int locCounter = 0;
			try {
				//graphDB = new EmbeddedGraphDatabase(graphDBFloder);
				
				for (OSMWay tempWay:wayLst.values())
				{
					String address = null;
					String type = "park";
					if (tempWay.attributeLst.containsKey("leisure"))
					{
						if (tempWay.attributeLst.get("leisure").equalsIgnoreCase("park"))
						{
							locCounter++;
							if (tempWay.attributeLst.containsKey("name"))
							{
								address = tempWay.attributeLst.get("name");
							}
							else
							{
								address = String.valueOf(tempWay.getID());
							}
//							type = type + ":" +  tempWay.attributeLst.get("highway");
							if (addLocation(type, tempWay,"polygon"))
								addedLocCounter ++;
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				if (graphDB != null)
					graphDB.shutdown();
			}
			double addedPercent = (double)addedLocCounter/(double)locCounter;
			System.out.println(addedLocCounter + " location added " + addedPercent);
			return wayLst;
		}
		*/
		
		
		public void ImportBorders(String[] spatialTypes) throws Exception
		{
			//Map<Integer, OSMWay> wayLst = createWayList();
			if (wayLst == null) 
			{
				wayLst = createWayList();
			}
			System.out.println(wayLst.size() + " Ways has been load");
			/*GraphDatabaseService graphDb, String address, String Geometry, String locationType, String locationDes, String[] attributes, String[] values  */
			GraphDatabaseService graphDB = null;
		
			//graphDB = new EmbeddedGraphDatabase(graphDBFloder);
			for (OSMWay tempWay:wayLst)
			{
				try 
				{
					SpatialEntityParms result = BuildGeometryAndAtt("spatialGroup", tempWay, "polygon");
					if (result != null && result.Success)
					{
						ssn.AddSpatialGroup(result.Geometry, spatialTypes, result.AttLst, result.ValueLst);
					}
				} 
				catch (OutOfMemoryError e) 
				{
					System.out.println("stoped at osm object Num: " + tempWay.getID());
					e.printStackTrace();
					throw e;
				}
				catch (StackOverflowError e) 
				{
					System.out.println("stoped at osm object Num: " + tempWay.getID());
					e.printStackTrace();
					throw e;
				}
				catch (Exception e) {					
					System.out.println("error while imporing Border. " + e.getMessage() + " in osm way number " + tempWay.getID());
				}
			}
		}
		
		private String MemeoryData()
		{
			return String.format("JVM max memory size is: %,.2f %n " +
								"JVM heap size is: %,.2f %n " +
								"JVM free memory size is: %,.2f %n " ,
								Runtime.getRuntime().maxMemory()/1024, Runtime.getRuntime().totalMemory()/1024, Runtime.getRuntime().freeMemory() /1024); 
		}
		
		private SpatialEntityParms BuildGeometryAndAtt(String type, OSMWay theWay, String geomType) throws Exception
		{
			SpatialEntityParms result = null;
			String geometry, geomEndStr;
			if(geomType.equalsIgnoreCase("polygon"))
			{
				if (theWay.isLoop())
				{
					geometry = "POLYGON ((";
					geomEndStr = "))";
				}
				else
				{
					return null;
				}
			}
			else if (geomType.equalsIgnoreCase("linestring"))
			{
				geometry = "LINESTRING (";
				geomEndStr = ")";
			}
			else
			{
				return null;
			}
			
			for (int i = 0; i < theWay.size(); i++)
			{
				geometry = geometry + theWay.getNode(i).getLat() + " " + theWay.getNode(i).getLon() + ","; 
			}
			geometry = geometry.substring(0, geometry.length() - 1) + geomEndStr;
			
			String[] attributes = new String[theWay.attributeLst.size() + 2];
			String[] values = new String[theWay.attributeLst.size() + 2];
			attributes[0] = "osm_id";
			values[0] = Integer.toString(theWay.getID());
			attributes[1] = "osm_type";
			values[1] = type;
			int i = 2;
			for(String tempKey:theWay.attributeLst.keySet())
			{
				attributes[i] = "osm_" + tempKey;
				values[i] = theWay.attributeLst.get(tempKey);
				i++;
			}
			result = new SpatialEntityParms();
			result.AttLst = attributes;
			result.ValueLst = values;
			result.Geometry = geometry;
			result.Success = true;
			return result;
		}
		
		/*
		private boolean addLocation(String type, OSMWay theWay, String geomType) throws Exception
		{
			String geometry, geomEndStr;
			if(geomType.equalsIgnoreCase("polygon"))
			{
				if (theWay.isLoop())
				{
					geometry = "POLYGON ((";
					geomEndStr = "))";
				}
				else
				{
					return false;
				}
			}
			else if (geomType.equalsIgnoreCase("linestring"))
			{
				geometry = "LINESTRING (";
				geomEndStr = ")";
			}
			else
			{
				return false;
			}
			
			for (int i = 0; i < theWay.size(); i++)
			{
				geometry = geometry + theWay.getNode(i).getLat() + " " + theWay.getNode(i).getLon() + ","; 
			}
			geometry = geometry.substring(0, geometry.length() - 1) + geomEndStr;
			
			String[] attributes = new String[theWay.attributeLst.size() + 1];
			String[] values = new String[theWay.attributeLst.size() + 1];
			attributes[0] = "osm_id";
			values[0] = Integer.toString(theWay.getID());
			int i = 1;
			for(String tempKey:theWay.attributeLst.keySet())
			{
				attributes[i] = "osm_" + tempKey;
				values[i] = theWay.attributeLst.get(tempKey);
				i++;
			}
			try 
			{
				ssn.AddLocation(geometry, type, attributes, values);
				System.gc();
				//createLocation(graphDB, address, geometry, type, null, attributes, values);
				//System.out.println(address + " added");
				return true;
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
				throw e;
			}
			//return false;
		}
		
		*/
		
		private class OSMWay
		{
			private int osmId;
			private List<OSMNode> nodeLst;
			public Map<String, String> attributeLst;
			
			public OSMWay(int osmId)
			{
				this.osmId = osmId;
//				nodeLst = new LinkedList<OSMNode>();
				nodeLst = new ArrayList<OSMNode>();
				attributeLst = new HashMap<String, String>();
			}
			
			public OSMNode getNode(int num)
			{
				if (num < nodeLst.size())
				{
					return nodeLst.get(num);
				}
				return null;
			}
			
			public void addNode(OSMNode theNode)
			{
				nodeLst.add(theNode);
			}
			
			public int size()
			{
				return nodeLst.size();
			}
			
			public boolean isLoop()
			{
				if (this.size()<2)
					return false;
				if (this.getNode(0).equals(this.getNode(this.size() - 1)))
					return true;
				return false;
			}
			
			public int getID() {return osmId;}
		}
		
		private class OSMNode
		{
			private double lat,lon,hight;
			private int osmId;
			private Map<String,String> attributes;
			
			public OSMNode(double lat, double lon, double hight, int id)
			{
				this.hight = hight;
				this.lat = lat;
				this.lon = lon;
				this.osmId = id;
				this.attributes = new HashMap<String, String>();
			}
			
			public OSMNode(double lat, double lon, int id)
			{
				this.hight = 0;
				this.lat = lat;
				this.lon = lon;
				this.osmId = id;
				this.attributes = new HashMap<String, String>();
			}
			
			public int getID() {return osmId;}
			public double getLat() {return lat;}
			public double getLon() {return lon;}
			public double getHight() {return hight;}
			
			public void setAttribute(String attKey, String attValue)
			{
				attributes.put(attKey, attValue);	
			}
			
			public String getAttribute(String attKey)
			{
				return attributes.get(attKey);
			}
		}
		
		class SpatialEntityParms
		{
			public String[] AttLst;
			public String[] ValueLst;
			public String Geometry;
			public boolean Success = false;
		}
		
		class ShotdownRecovery extends Thread
		{
			private int recoveryFrom, osmWayId;
			private final String recoveryMsg;
			
			public ShotdownRecovery(String msg)
			{
				recoveryMsg = msg;
				recoveryFrom = -1;
				osmWayId = -1;
			}
			
			public void SetRecoveryFrom(int n, int theOsmId)
			{
				recoveryFrom = n;
				osmWayId = theOsmId;
			}
			
			@Override
			public void run() {
				System.out.println("unexpected shutdown, done working on " + recoveryFrom + " in the way list with osm id: " + osmWayId);
				System.out.println(recoveryMsg);
				if (ssn != null) ssn.Dispose();
				System.out.println("ssn has been shut down successfully");
				super.run();
			}
		}
		
}

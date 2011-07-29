package ac.technion.geoinfo.ssnTrj;

import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;


public class OSMimpoter {

		private Map<Integer, OSMNode> NodeLst = null;
		
		private final String graphDBFloder, OSMFile;
		
		private SSN ssn;
		
		
		public OSMimpoter(String OSMFile, String grpahDBFolder) throws XMLStreamException
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
				 while (parser.hasNext())
				 {
					 parser.next();
					 if (parser.getEventType() == XMLStreamConstants.START_ELEMENT)
					 {
						 if (parser.getLocalName().equals("node"))
						 {
							 int id = 0;
							 double lat = 0;
							 double lon = 0;
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
		
		private Map<Integer, OSMWay> createWayList() throws XMLStreamException
		{
			javax.xml.stream.XMLInputFactory factory = null;
			javax.xml.stream.XMLStreamReader parser = null;
			Map<Integer, OSMWay> wayLst = null;
			try {
				 factory = javax.xml.stream.XMLInputFactory.newInstance();
				 parser = factory.createXMLStreamReader(new FileReader(OSMFile));
				 wayLst = new HashMap<Integer, OSMWay>();
				 while (parser.hasNext())
				 {
					 parser.next();
					 if (parser.getEventType() == XMLStreamConstants.START_ELEMENT)
					 {
						 if (parser.getLocalName().equals("way"))
						 {
							 int id = 0;
							 for(int i = 0; i < parser.getAttributeCount(); i++)
							 {
								 if (parser.getAttributeLocalName(i).equals("id"))
								 {
									 id = Integer.parseInt(parser.getAttributeValue(i));
								 }
							 }
							 if (id == 0) continue;
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
//											 throw new Exception("node " + nodeId + " does not exsist in node List");
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
							 
							 if (skipThisWay) continue;
							 
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
							 wayLst.put(tempWay.getID(), tempWay);
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
		
		public Map<Integer, OSMWay> ImportRoads() throws Exception
		{
			Map<Integer, OSMWay> wayLst = createWayList();
			System.out.println(wayLst.size() + " Ways has been load");
			GraphDatabaseService graphDB = null;
			int addedLocCounter = 0;
			int locCounter = 0;
			double addedPercent = 0;
			int lstSize = wayLst.size();
			try {
				//graphDB = new EmbeddedGraphDatabase(graphDBFloder);
				for (OSMWay tempWay:wayLst.values())
				{
					String address = null;
					String type = "highway";
					if (tempWay.attributeLst.containsKey("highway"))
					{
						locCounter++;
						if (tempWay.attributeLst.containsKey("name"))
						{
							address = tempWay.attributeLst.get("name") + " , " + String.valueOf(tempWay.getID());
						}
						else
						{
							address = String.valueOf(tempWay.getID());
						}
						type = type + ":" +  tempWay.attributeLst.get("highway");
						
						if (addLocation(address, type, tempWay,"lineString"))
							addedLocCounter++;
						double newPercent = (double)addedLocCounter/(double)lstSize;
						if (newPercent - addedPercent > 0.05)
						{
							System.out.println(newPercent*100 + "% done");
							addedPercent = newPercent;
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw e;
			}finally{
				if (graphDB != null)
					graphDB.shutdown();
			}
			addedPercent = (double)addedLocCounter/(double)locCounter;
			System.out.println(addedLocCounter + " location added " + addedPercent);
			return wayLst;
		}
		
		public Map<Integer, OSMWay> ImportBulidings() throws XMLStreamException
		{
			Map<Integer, OSMWay> wayLst = createWayList();
			System.out.println(wayLst.size() + " Ways has been load");
			int addedLocCounter = 0;
			int locCounter = 0;
			double addedPercent = 0;
			try { 
				for (OSMWay tempWay:wayLst.values())
				{
					String address = null;
					String type = "building";
					if (tempWay.attributeLst.containsKey("building"))
					{
						if (tempWay.attributeLst.get("building").equalsIgnoreCase("yes"))
						{
							locCounter++;
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
//							type = type + ":" +  tempWay.attributeLst.get("highway");
							if (addLocation(address, type, tempWay,"polygon"))
								addedLocCounter ++;
							double newPercent = (double)addedLocCounter/(double)locCounter;
							if (newPercent - addedPercent > 0.05)
							{
								System.out.println(newPercent*100 + "% done");
								addedPercent = newPercent;
							}
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			addedPercent = (double)addedLocCounter/(double)locCounter;
			System.out.println(addedLocCounter + " location added " + addedPercent);
			return wayLst;
		}
		
		public Map<Integer, OSMWay> ImportParks() throws XMLStreamException
		{
			Map<Integer, OSMWay> wayLst = createWayList();
			System.out.println(wayLst.size() + " Ways has been load");
			GraphDatabaseService graphDB = null;
			int addedLocCounter = 0;
			int locCounter = 0;
			try {
				graphDB = new EmbeddedGraphDatabase(graphDBFloder);
				
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
							if (addLocation(address, type, tempWay,"polygon"))
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
		
		public void ImportBorders() throws Exception
		{
			Map<Integer, OSMWay> wayLst = createWayList();
			System.out.println(wayLst.size() + " Ways has been load");
			/*GraphDatabaseService graphDb, String address, String Geometry, String locationType, String locationDes, String[] attributes, String[] values  */
			GraphDatabaseService graphDB = null;
			try {
				graphDB = new EmbeddedGraphDatabase(graphDBFloder);
				for (OSMWay tempWay:wayLst.values())
				{
					String address = null;
					if (tempWay.attributeLst.containsKey("name"))
					{
						address = tempWay.attributeLst.get("name");
					}
					else if (tempWay.attributeLst.containsKey("_NAME_"))
					{
						address = tempWay.attributeLst.get("_NAME_");
						if (address.endsWith("(2002)"))
							address = address.substring(0, address.length() - 6 ).trim();
					}
					if (address == null) continue;
					
					String locationType;
					if(tempWay.attributeLst.containsKey("border_type"))
					{
						locationType = tempWay.attributeLst.get("border_type");
					}
					else if(tempWay.attributeLst.containsKey("_NAME_"))
					{
						if(tempWay.attributeLst.get("_NAME_").trim().toLowerCase().startsWith("ward"))
						{
							locationType = "ward";
						}
						else if(tempWay.attributeLst.get("_NAME_").trim().toLowerCase().startsWith("anc"))
						{
							locationType = "anc";
						}
						else
						{
							continue;
						}
					}
					else
					{
						continue;
					}
					
					addLocation(address, locationType, tempWay,"polyogn");
						
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw e;
			}finally{
				if (graphDB != null)
					graphDB.shutdown();
			}
		}
		
		private boolean addLocation(String address, String type, OSMWay theWay, String geomType) throws Exception
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
			
			String[] attributes = new String[theWay.attributeLst.size()];
			String[] values = new String[theWay.attributeLst.size()];
			int i = 0;
			for(String tempKey:theWay.attributeLst.keySet())
			{
				attributes[i] = "osm_" + tempKey;
				values[i] = theWay.attributeLst.get(tempKey);
				i++;
			}
			try 
			{
				ssn.AddLocation(geometry, attributes, values);
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
		
		
		private class OSMWay
		{
			private int osmId;
			private List<OSMNode> nodeLst;
			public Map<String, String> attributeLst;
			
			public OSMWay(int osmId)
			{
				this.osmId = osmId;
				nodeLst = new LinkedList<OSMNode>();
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
}

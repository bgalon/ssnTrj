package ac.technion.geoinfo.ssnTrj;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.Search;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.WKTGeometryEncoder;
import org.neo4j.gis.spatial.query.SearchEqual;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.linemerge.LineMerger;

import ac.technion.geoinfo.ssnTrj.domain.NodeWrapper;
import ac.technion.geoinfo.ssnTrj.domain.NodeWrapperImpl;
import ac.technion.geoinfo.ssnTrj.domain.Route;
import ac.technion.geoinfo.ssnTrj.domain.RouteImpl;
import ac.technion.geoinfo.ssnTrj.domain.SocialRelation;
import ac.technion.geoinfo.ssnTrj.domain.SpatialEntity;
import ac.technion.geoinfo.ssnTrj.domain.SpatialEntityImpl;
import ac.technion.geoinfo.ssnTrj.domain.SpatialRelation;
import ac.technion.geoinfo.ssnTrj.domain.TimePatternRelation;
import ac.technion.geoinfo.ssnTrj.domain.TimePattern;
import ac.technion.geoinfo.ssnTrj.domain.TimePatternImpl;
import ac.technion.geoinfo.ssnTrj.domain.User;
import ac.technion.geoinfo.ssnTrj.domain.Static;
import ac.technion.geoinfo.ssnTrj.domain.UserImpl;
import ac.technion.geoinfo.ssnTrj.geometry.ColsestRoadSearch;
import ac.technion.geoinfo.ssnTrj.geometry.ContainsBySNNtypes;
import ac.technion.geoinfo.ssnTrj.geometry.RoadIntersectSearch;
import ac.technion.geoinfo.ssnTrj.geometry.RoadTouchesSearch;
import ac.technion.geoinfo.ssnTrj.spatial.SsnSpatialLayer;

public class SSNonGraph implements SSN, Static {

	private final GraphDatabaseService graphDB;
	private final SpatialDatabaseService sgDB;
	private EditableLayer spatialLyr;
	private EditableLayer routeLyr;
	//private Index<Node> spatialIndex;
	//private Index<Node> socialIndex;
	
	//const for map resolution
	private final double METER = 8.98315E-06; //for OSM
	private final double SEARCH_COLSEST_ROAD = 100 * METER;
	
	public SSNonGraph(String path) throws Exception
	{
		graphDB = new EmbeddedGraphDatabase(path);
		sgDB = new SpatialDatabaseService(graphDB);
		init();
	}
	
	public SSNonGraph(GraphDatabaseService theGraphDB) throws Exception
	{
		graphDB = theGraphDB;
		sgDB = new SpatialDatabaseService(graphDB);
		init();
	}
	
	private void init() throws Exception
	{
		Transaction tx = sgDB.getDatabase().beginTx(); 
		try
		{
			//create the indexes
			graphDB.index().forNodes(SPATIAL_FULLTEXT_INDEX, 
					MapUtil.stringMap( IndexManager.PROVIDER, "lucene", "type", "fulltext", "to_lower_case", "true"));
			graphDB.index().forNodes(SOCIAL_FULLTEXT_INDEX, 
					MapUtil.stringMap( IndexManager.PROVIDER, "lucene", "type", "fulltext", "to_lower_case", "true"));
			
			if (!sgDB.containsLayer(SPATIAL_LAYER))
			{
				//spatialLyr = (EditableLayer)sgDB.createLayer(SPATIAL_LAYER, WKTGeometryEncoder.class, EditableLayerImpl.class);
				spatialLyr = (EditableLayer)sgDB.createLayer(SPATIAL_LAYER, WKTGeometryEncoder.class, SsnSpatialLayer.class);
				//spatialLyr = (EditableLayer)sgDB.createLayer(SPATIAL_LAYER, WKTGeometryEncoder.class, DynamicLayer.class);
			}
			else
			{
				spatialLyr = (EditableLayer)sgDB.getLayer(SPATIAL_LAYER);
			}
			
			if (!sgDB.containsLayer(ROUTE_LAYER))
			{
				routeLyr = (EditableLayer)sgDB.createLayer(ROUTE_LAYER, WKTGeometryEncoder.class, SsnSpatialLayer.class);
			}
			else
			{
				routeLyr = (EditableLayer)sgDB.getLayer(ROUTE_LAYER);
			}
			tx.success();
		}
		catch (Exception e) {
			// TODO: handle exception
			tx.failure();
			throw e;
			//e.printStackTrace();
		}
		finally
		{
			tx.finish();
		}
	}
	
	//for testing*************
	public EditableLayer getSatialEntitesLayer()
	{
		return spatialLyr;
	}
	
	public GraphDatabaseService getGDB()
	{
		return graphDB;
	}
	
	public EditableLayer getLayer(String theLayer)
	{
		return (EditableLayer) sgDB.getLayer(theLayer);
	}
	//end of test methods 
	
	public void finalize() throws Throwable
	{
		Dispose();
		super.finalize();
	}
	
	public Index<Node> getSpatialIndex()
	{
		return getNodeIndex(SPATIAL_FULLTEXT_INDEX);
	}
	
	public Index<Node> getSocialIndex()
	{
		return getNodeIndex(SOCIAL_FULLTEXT_INDEX);
	}
	
	public Index<Node> getNodeIndex(String indexName)
	{
		return graphDB.index().forNodes(indexName);
	}
	
	public void executeSpatialSearch(Search theSearch, String lyr) throws Exception
	{
		if(lyr.equalsIgnoreCase("spatial"))
		{
			spatialLyr.getIndex().executeSearch(theSearch);
		} 
		else if(lyr.equalsIgnoreCase("route"))
		{
			routeLyr.getIndex().executeSearch(theSearch);
		}
		else
		{
			throw new Exception("worng input in executeSpatialSearch no such layer " + lyr);
		}
	}
	
	public GeometryFactory getGeometryFactory()
	{
		return spatialLyr.getGeometryFactory();
	}
	
	public NodeWrapper getNodeById(long id)
	{
		return new NodeWrapperImpl(graphDB.getNodeById(id));
	}
	
	public SpatialEntity AddSpatialGroup(String geom,String[] spatialTypes, String[] attributes, Object[] values) throws Exception 
	{
		SpatialEntity theSE = null;
		Transaction tx = sgDB.getDatabase().beginTx(); 
		try
		{
			WKTReader reader = new WKTReader();
			Geometry seGeom = reader.read(geom);
			if (seGeom.getGeometryType().equalsIgnoreCase("polygon"))
			{
				Search cointainsTypes = new ContainsBySNNtypes(seGeom.buffer(8.98315E-06*400), spatialTypes);
				spatialLyr.getIndex().executeSearch(cointainsTypes);
				if(!cointainsTypes.getResults().isEmpty())
				{
					SpatialEntity newSE = AddSpatialEntity(seGeom, spatialLyr, Static.SPATIAL_GROUP, attributes, values);
					for(SpatialDatabaseRecord tempSpatialRecord:cointainsTypes.getResults())
					{
						if(!tempSpatialRecord.getGeomNode().hasRelationship(Direction.INCOMING, SpatialRelation.within))
							newSE.createRelationshipTo(tempSpatialRecord.getGeomNode(), SpatialRelation.within);
					}
				}
				else
				{
					throw new Exception("the geometry do not contain any spatial entities from the required type");
				}
			}
			else 
			{
				throw new Exception("can not dedect geometry for spatial enttiy");
			}
			
			tx.success();
		}
		catch (Exception e) {
			// TODO: handle exception
			//System.out.println(e.getMessage());
			tx.failure();
			throw e;
		}
		finally
		{
			tx.finish();
		}
		
		 return theSE;
	}
	
	public List<SpatialEntity> AddBuilding(String geom, String[] attributes, Object[] values) throws Exception
	{
		List<SpatialEntity> theSE = null;
		Transaction tx = sgDB.getDatabase().beginTx(); 
		try
		{
			WKTReader reader = new WKTReader();
			Geometry seGeom = reader.read(geom);
			if (seGeom.getGeometryType().equalsIgnoreCase("polygon"))
			{
				//theSE = new LinkedList<SpatialEntity>();
				theSE = new ArrayList<SpatialEntity>();
				Search LeadTo = new ColsestRoadSearch(seGeom, SEARCH_COLSEST_ROAD);
				spatialLyr.getIndex().executeSearch(LeadTo);
				SpatialEntity newSE = AddSpatialEntity(seGeom, spatialLyr, Static.BULIDING, attributes, values);
				if (!LeadTo.getResults().isEmpty())
				{
					LeadTo.getResults().get(0).getGeomNode().createRelationshipTo(newSE, SpatialRelation.lead_to);
				}
				theSE.add(newSE);
			}
			else 
			{
				throw new Exception("can not dedect geometry for buliding");
			}
			
			tx.success();
		}
		catch (Exception e) {
			// TODO: handle exception
			//System.out.println(e.getMessage());
			tx.failure();
			throw e;
		}
		finally
		{
			tx.finish();
		}
		
		 return theSE;
	}
	
	public List<SpatialEntity> AddRoadSegment(String geom, String[] attributes, Object[] values) throws Exception
	{
		List<SpatialEntity> theSE = null;
		Transaction tx = sgDB.getDatabase().beginTx(); 
		try
		{
			WKTReader reader = new WKTReader();
			Geometry seGeom = reader.read(geom);
			if (seGeom.getGeometryType().equalsIgnoreCase("LineString"))
			{
				theSE = InsertLineString(seGeom, spatialLyr, attributes, values);
			}
			else
			{
				throw new Exception("can not dedect geometry for orad segment");
			}
			
			tx.success();
		}
		catch (Exception e) {
			// TODO: handle exception
			//System.out.println(e.getMessage());
			tx.failure();
			throw e;
		}
		finally
		{
			tx.finish();
		}
		
		 return theSE;
	}
	
/*	
//	public List<SpatialEntity> AddLocation(String geom, String[] attributes, Object[] values) throws Exception {
//		List<SpatialEntity> theSE = null;
//		Transaction tx = sgDB.getDatabase().beginTx(); 
//		try
//		{
//			WKTReader reader = new WKTReader();
//			Geometry seGeom = reader.read(geom);
//			if (seGeom.getGeometryType().equalsIgnoreCase("polygon"))
//			{
//				theSE = InsertPolygon(seGeom, spatialLyr ,attributes, values);
//			}
//			else if (seGeom.getGeometryType().equalsIgnoreCase("LineString"))
//			{
//				theSE = InsertLineString(seGeom, spatialLyr, attributes, values);
//			}
//			if (theSE == null)
//			{
//				throw new Exception("can not dedect geometry");
//			}
//			
//			tx.success();
//		}
//		catch (Exception e) {
//			// TODO: handle exception
//			System.out.println(e.getMessage());
////			throw e;
//		}
//		finally
//		{
//			tx.finish();
//		}
//		
//		 return theSE;
//	}
*/	


/*
	private List<SpatialEntity> InsertPolygon(Geometry theGeom, EditableLayer spatialLayer ,String[] attributes, Object[] values) throws Exception
	{
//		Search cointainsPoly = new PolygonContainsSearch(theGeom);
//		Search withinPoly = new PolygonWithinSearch(theGeom);
		Search LeadTo = new ColsestRoadSearch(theGeom, SEARCH_COLSEST_ROAD);
//		spatialLyr.getIndex().executeSearch(cointainsPoly);
//		spatialLyr.getIndex().executeSearch(withinPoly);
		spatialLyr.getIndex().executeSearch(LeadTo);
		SpatialEntity newSE = AddSpatialEntity(theGeom, spatialLyr, Static.BULIDING, attributes, values);
//		for(SpatialDatabaseRecord tempSpatialRecord:cointainsPoly.getResults())
//		{
//			newSE.createRelationshipTo(tempSpatialRecord.getGeomNode(), SpatialRelation.within);
//		}
//		for(SpatialDatabaseRecord tempSpatialRecord:withinPoly.getResults())
//		{
//			tempSpatialRecord.getGeomNode().createRelationshipTo(newSE, SpatialRelation.within);
//		}
		if (!LeadTo.getResults().isEmpty())
		{
			LeadTo.getResults().get(0).getGeomNode().createRelationshipTo(newSE, SpatialRelation.lead_to);
		}
		List<SpatialEntity> returnLst = new LinkedList<SpatialEntity>();
		returnLst.add(newSE);
		return returnLst;
	}
	
	*/
	
	private List<SpatialEntity> InsertLineString(Geometry theGeom, EditableLayer spatialLayer, String[] thisAttributes, Object[] thisValues) throws Exception
	{
		//Geometry tempGeom = extend((LineString)theGeom, 10*METER);
		//Geometry tempGeom = theGeom;
		Search lineStringSearch = new RoadIntersectSearch(theGeom);
		spatialLayer.getIndex().executeSearch(lineStringSearch);
//		List<Geometry> needToBeAddedFromThis = new LinkedList<Geometry>();
//		List<Geometry> needToBeAddedFromDB = new LinkedList<Geometry>();
//		
//		List<String[]> DBattributes = new LinkedList<String[]>();
//		List<Object[]> DBvalues = new LinkedList<Object[]>();
		List<Geometry> needToBeAddedFromThis = new ArrayList<Geometry>();
		List<Geometry> needToBeAddedFromDB = new ArrayList<Geometry>();
		
		List<String[]> DBattributes = new ArrayList<String[]>();
		List<Object[]> DBvalues = new ArrayList<Object[]>();
		needToBeAddedFromThis.add(theGeom);
		List<SpatialEntity> returnedList = null;
		
		for(SpatialDatabaseRecord tempSpatialRecord:lineStringSearch.getResults())
		{
	        //in case that geometries in the database cuts due to the new geometry 
			
			Geometry otherDiffGeom = tempSpatialRecord.getGeometry().difference(theGeom);
			
			if (otherDiffGeom.getNumGeometries() > 1)
			{
				//get attributes list and values here
				for (int i =0; i < otherDiffGeom.getNumGeometries(); i++)
				{
					needToBeAddedFromDB.add(otherDiffGeom.getGeometryN(i));
					List<String> tempDBAtt = new ArrayList<String>();
					List<Object> tempDBVal = new ArrayList<Object>();
					for(String tempAtt:tempSpatialRecord.getGeomNode().getPropertyKeys())
					{
						if (tempAtt != "gtype" && tempAtt != "wkt" && tempAtt != "bbox")
						{
							tempDBAtt.add(tempAtt);
							tempDBVal.add(tempSpatialRecord.getGeomNode().getProperty(tempAtt));
						}
					}
					String[] AttArray = new String[tempDBAtt.size()];
					AttArray = tempDBAtt.toArray(AttArray);
					DBattributes.add(AttArray);
					DBvalues.add(tempDBVal.toArray());
				}
				RemoveSpatialEntity(tempSpatialRecord, spatialLayer);
			}
		}
		
//		returnedList = new LinkedList<SpatialEntity>(); 
		returnedList = new ArrayList<SpatialEntity>(); 
		for(int i = 0; i < needToBeAddedFromDB.size(); i++)
		{
			returnedList.add(AddOneRoadSegment(needToBeAddedFromDB.get(i), spatialLayer, 
					DBattributes.get(i), DBvalues.get(i)));
		}
		
		//spatialLayer.getIndex().executeSearch(lineStringSearch);
		for(SpatialDatabaseRecord tempSpatialRecord:lineStringSearch.getResults())
		{
			//in case that the new geometry cuts due to geometries in the database
			//Geometry other = extend((LineString)tempSpatialRecord.getGeometry(), 10*METER);
			Geometry other = tempSpatialRecord.getGeometry();
//			List<Geometry> addTo_eedToBeAddedFromThis = new LinkedList<Geometry>();
//			List<Geometry> removeFrom_eedToBeAddedFromThis = new LinkedList<Geometry>();
			List<Geometry> addTo_eedToBeAddedFromThis = new ArrayList<Geometry>();
			List<Geometry> removeFrom_eedToBeAddedFromThis = new ArrayList<Geometry>();
			for (Geometry thisGeom:needToBeAddedFromThis)
			{
				Geometry geomDiffOther = thisGeom.difference(other);
				if(geomDiffOther.getNumGeometries() > 1)
				{
					removeFrom_eedToBeAddedFromThis.add(thisGeom);
					for (int i = 0; i < geomDiffOther.getNumGeometries(); i++)
					{
						addTo_eedToBeAddedFromThis.add(geomDiffOther.getGeometryN(i));
					}
				}
			}
			if(!addTo_eedToBeAddedFromThis.isEmpty())
			{
				needToBeAddedFromThis.removeAll(removeFrom_eedToBeAddedFromThis);
				needToBeAddedFromThis.addAll(addTo_eedToBeAddedFromThis);
			}
		}
		
		
		for(Geometry addMe:needToBeAddedFromThis)
		{
			returnedList.add(AddOneRoadSegment(addMe, spatialLayer, thisAttributes, thisValues));
		}
		
		return returnedList;
    }
	
	private SpatialEntity AddOneRoadSegment(Geometry theGeom, EditableLayer spatialLayer, String[] attributes, Object[] values) throws Exception
	{
		SpatialEntity newSE = AddSpatialEntity(theGeom, spatialLayer, ROAD_SEGMENT, attributes, values);
		
		Search touchSearch = new RoadTouchesSearch(theGeom);
		spatialLayer.getIndex().executeSearch(touchSearch);
		for(SpatialDatabaseRecord tempSpatialRecord:touchSearch.getResults())
		{
			//TODO: add here connectivity check for the relationship
			SpatialEntity otherNode = new SpatialEntityImpl(tempSpatialRecord.getGeomNode());
			
			if(CheckRSConnectivity(newSE, otherNode))
				newSE.createRelationshipTo(otherNode.getNode(), SpatialRelation.touch);
			if(CheckRSConnectivity(otherNode, newSE))
				otherNode.createRelationshipTo(newSE.getNode(), SpatialRelation.touch);
			
		}
		
		return newSE;
	}
	
	private boolean CheckRSConnectivity(SpatialEntity seFrom, SpatialEntity seTo) throws Exception
	{
		//in this function we assume that the road segments are touched at the ends of each segments
		if (!seTo.getGeometry().getGeometryType().equals("LineString") && !seFrom.getGeometry().getGeometryType().equals("LineString")) 
			return false;
		
		LineString lsTo = (LineString)seTo.getGeometry();
		LineString lsFrom = (LineString)seFrom.getGeometry();
		if(IsOneway(seFrom))
		{
			if(IsOneway(seTo))
			{
				return lsFrom.getEndPoint().equals(lsTo.getStartPoint());
			}
			else
			{
				return lsTo.touches(lsFrom.getEndPoint());
			}
		}
		else
		{
			if(IsOneway(seTo))
			{
				return lsFrom.touches(lsTo.getStartPoint());
			}
			else
			{
				return lsFrom.touches(lsTo);
			}
		}
	}
	
	private boolean IsOneway(SpatialEntity se)
	{
		if(se.hasProperty(ONEWAY_PROPERTY) && ((String)se.getProperty(ONEWAY_PROPERTY)).equals("yes"))
			return true;
		return false;
	}
	
	private SpatialEntity AddSpatialEntity(Geometry theGeom, EditableLayer spatialLayer, String type, String[] attributes, Object[] values) throws Exception
	{
		//Check if the geometry already exist in the database
		Search equalSearch = new SearchEqual(theGeom);
		spatialLayer.getIndex().executeSearch(equalSearch);
		if (!equalSearch.getResults().isEmpty())
		{
			throw new Exception("Geometry alrady exsist");
		}
		SpatialEntity newSE = new SpatialEntityImpl(spatialLayer.add(theGeom).getGeomNode());	
		String fullIndexField = "";
		//add the attributes 
		if (attributes.length != values.length)
		{
			throw new Exception("the attributes and the values array are not in the same length");
		}
		//update attributes
		for(int i = 0; i < attributes.length; i++)
		{
			newSE.setProperty(attributes[i], values[i]);
			fullIndexField = fullIndexField + values[i].toString() + ";";
		}
		//update the index
		if(!fullIndexField.isEmpty()){
			fullIndexField = fullIndexField.substring(0, fullIndexField.length() -1);
			Index<Node> spatialIndex = graphDB.index().forNodes(SPATIAL_FULLTEXT_INDEX); 
			spatialIndex.add(newSE.getNode() , SPATIAL_FULLTEXT_KEY, fullIndexField);
			newSE.setProperty(FULLTEXT_PROPERTY, fullIndexField);
		}
		
		newSE.setProperty(SSN_TYPE, type);
		Index<Node> typeIndex = graphDB.index().forNodes(TYPE_INDEX);
		typeIndex.add(newSE, TYPE_INDEX, type);
		//***********
		//System.out.println(spatialLayer.getIndex().count());
		//((RTreeIndexFix)spatialLayer.getIndex()).debugIndexTree();
		//************
		return newSE;
	}
	
	private void RemoveSpatialEntity(SpatialDatabaseRecord removeRecord, EditableLayer spatialLayer) throws Exception
	{
		Index<Node> LocationFulltxtInd = graphDB.index().forNodes(SPATIAL_FULLTEXT_INDEX);
		LocationFulltxtInd.remove(removeRecord.getGeomNode());
		try
		{
			spatialLayer.delete(removeRecord.getGeomNode().getId());
			//System.out.println("remove: " + removeRecord.getGeomNode().getId() + "-" + removeRecord.getId());
			//((RTreeIndexFix)spatialLayer.getIndex()).debugIndexTree();
		}
		catch (Exception e) {
			// TODO: handle exception
			//System.out.println(spatialLayer.getIndex().count());
			//System.out.println("remove: " + removeRecord.getGeomNode().getId() + "-" + removeRecord.getId());
			//((RTreeIndexFix)spatialLayer.getIndex()).debugIndexTree();
			throw e;
		}
			
	}
	
/*
//*********************************extend function************************
//	private LineString extend(LineString extendMe, double extendIn)
//	{
//		Coordinate[] theCoord = (extendMe.getCoordinates()).clone();
//		
//		double dx = theCoord[0].x - theCoord[1].x;
//		double dy = theCoord[0].y - theCoord[1].y;
//		double dis = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
//		theCoord[0] = new Coordinate(theCoord[0].x + dx*extendIn/dis, theCoord[0].y + dy*extendIn/dis);
//		
//		
//		int coordNum = theCoord.length;
//		
//		dx = theCoord[coordNum - 1].x - theCoord[coordNum - 2].x;
//		dy = theCoord[coordNum - 1].y - theCoord[coordNum - 2].y;
//		dis = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
//		theCoord[coordNum - 1] = new Coordinate(theCoord[coordNum - 1].x + dx*extendIn/dis, 
//				theCoord[coordNum - 1].y + dy*extendIn/dis);
//		
//		return spatialLyr.getGeometryFactory().createLineString(theCoord);
//	}
//********************************end of extend function************************
*/

	public User AddUser(String uName, String[] relatedUsers, String[] relationType, String[] attributes, String[] values) throws Exception {
		User newUser = null;
		Transaction tx = sgDB.getDatabase().beginTx(); 
		try
		{
			Index<Node> socialKeyIndex = graphDB.index().forNodes(SOCIAL_KEY_INDEX);
			IndexHits<Node> findIfExsist = socialKeyIndex.get(SOCIAL_KEY_INDEX_KEY, uName);
			if (!(findIfExsist.getSingle() == null))
			{
				throw new Exception("user " + uName + " alraey exsist");
			}
			newUser = new UserImpl(graphDB.createNode());
			newUser.setProperty(SOCIAL_KEY_INDEX_KEY, uName);
			newUser.setProperty(SSN_TYPE, USER);
			Index<Node> typeIndex = graphDB.index().forNodes(TYPE_INDEX);
			typeIndex.add(newUser, TYPE_INDEX, USER);
			socialKeyIndex.add(newUser, SOCIAL_KEY_INDEX_KEY, uName);
			
			String fullIndexField = "";
			//add the attributes 
			if (attributes.length != values.length)
			{
				throw new Exception("the attributes and the values array are not in the same length");
			}
			//update attributes
			fullIndexField = uName + ";";
			for(int i = 0; i < attributes.length; i++)
			{
				newUser.setProperty(attributes[i], values[i]);
				fullIndexField = fullIndexField + values[i].toString() + ";";
			}
			//update the index
			if(!fullIndexField.isEmpty()){
				fullIndexField = fullIndexField.substring(0, fullIndexField.length() -1);
				Index<Node> socialIndex = graphDB.index().forNodes(SOCIAL_FULLTEXT_INDEX); 
				socialIndex.add(newUser.getNode() , SOCIAL_FULLTEXT_KEY, fullIndexField);
				newUser.setProperty(FULLTEXT_PROPERTY, fullIndexField);
			}
			
			if (relatedUsers != null){
				for (int i = 0; i < relatedUsers.length; i++) 
				{
					String tempUser = relatedUsers[i];
					if (tempUser.isEmpty()) continue;
					IndexHits<Node> tempNodeHits = socialKeyIndex.get(SOCIAL_KEY_INDEX_KEY, tempUser);
					Node tempNode = tempNodeHits.getSingle();
					if (tempNode != null){
						addSocialRelationship(newUser, new UserImpl(tempNode), SocialRelation.Parse(relationType[i]));
					}
					else{
						tx.failure();
						throw new Exception("no such friend " + tempUser);
					}
				}
			}
			tx.success();
		}
		catch (Exception e) 
		{
			// TODO: handle exception
			//System.out.println(e.getMessage());
			tx.failure();
			throw e;
		}
		finally
		{
			tx.finish();
		}
		return newUser;
	}
		
	private static Relationship addSocialRelationship(User user1, User user2, SocialRelation rlationType){
		return user1.createRelationshipTo(user2, rlationType);
	}
	
	public TimePattern addPattren(User theUser, SpatialEntity theSE, String TimePattrenAsStr, double confident) throws Exception
	{
		TimePattern theTP;
		if(confident < 0 || confident > 1)
			throw new Exception("con't create time patttren, the confident value is out of range");
		Transaction tx = sgDB.getDatabase().beginTx(); 
		try
		{
			Relationship newTp = null;
			if(theSE.getType().equals(ROUTE))
				newTp = theUser.createRelationshipTo(theSE, TimePatternRelation.tpToRoute);
			if(theSE.getType().equals(BULIDING) || theSE.getType().equals(SPATIAL_GROUP))
				newTp = theUser.createRelationshipTo(theSE, TimePatternRelation.tpToSpatialEntity);
			newTp.setProperty(TIME_PATTERN_PORP, TimePattrenAsStr);
			newTp.setProperty(CONFIDENT_PROP, confident);
			theTP = new TimePatternImpl(newTp);
			tx.success();
		}
		catch (Exception e) {
			// TODO: handle exception
			//System.out.println(e.getMessage());
			tx.failure();
			throw e;
		}
		finally
		{
			tx.finish();
		}
		return theTP;
	}

	public Route addRoute(SpatialEntity start, SpatialEntity end, SpatialEntity[] segments) throws Exception {
		//the segments array must be in the real order
		if(start == null && end == null)
		{
			throw new Exception("error while create route. start and end are both null");
		}
		if(segments.length < 1)
		{
			throw new Exception("error while create route. no segments");
		}
		Node newRoute = null;
		Transaction tx = sgDB.getDatabase().beginTx(); 
		try
		{
//			newRoute = sgDB.getDatabase().createNode();
			Geometry routeGeom = bulidRouteGeom(start, end, segments);
			newRoute = routeLyr.add(routeGeom).getGeomNode();
			newRoute.setProperty(SSN_TYPE, ROUTE);
			Index<Node> typeIndex = graphDB.index().forNodes(TYPE_INDEX);
			typeIndex.add(newRoute, TYPE_INDEX, ROUTE);
			//check if start lead to the first segment
			if (start != null && chcekConncet(start, segments[0], SpatialRelation.lead_to))
			{
				newRoute.createRelationshipTo(start, SpatialRelation.startAt);
			}
			else
			{
				throw new Exception("error while create route. " + start + 
						" do not lead to " + segments[0]);
			}
			for(int i = 0; i < segments.length - 1; i++)
			{
				if(chcekConncet(segments[i], segments[i+1], SpatialRelation.touch))
				{
					Relationship tempRel = newRoute.createRelationshipTo(segments[i], SpatialRelation.include);
					tempRel.setProperty(SEGMENT_NUMBER, i);
				}
				else 
				{
					throw new Exception("error while create route. " + segments[i] + 
							" do not conncet to " + segments[i+1]);
				}
			}
			Relationship tempRel = newRoute.createRelationshipTo(segments[segments.length -1], SpatialRelation.include);
			tempRel.setProperty(SEGMENT_NUMBER, segments.length - 1);
			//check if the last segment lead to end
			if (end != null && chcekConncet(segments[segments.length -1], end, SpatialRelation.lead_to))
			{
				newRoute.createRelationshipTo(end, SpatialRelation.endAt);
			}
			else
			{
				throw new Exception("error while create route. " + segments[segments.length -1] + 
						" do not lead to " + end);
			}
			
			tx.success();
		}
		catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
			newRoute = null;
			tx.failure();
			throw e;
		}
		finally
		{
			tx.finish();
		}
		if (newRoute == null) return null;
		return new RouteImpl(newRoute);
	}
	//private 
	
	private boolean chcekConncet(SpatialEntity se1, SpatialEntity se2, RelationshipType relType)
	{
		Iterable<Relationship> Rels = se1.getRelationships(relType);
		for (Relationship tempRel:Rels)
		{
			if(tempRel.getOtherNode(se1).equals(se2))
				return true;
		}
		return false;
	}
	
	private Geometry bulidRouteGeom(SpatialEntity start, SpatialEntity end, SpatialEntity[] segments) throws Exception
	{
//		Point startPoint = start.getGeometry().getCentroid();
//		Point endPoint = end.getGeometry().getCentroid();
//		PointPairDistance ppd = new PointPairDistance(); 
//		DistanceToPoint.computeDistance(segments[0].getGeometry(), startPoint.getCoordinate(), ppd);
//		
//		
		LineMerger lineMerger = new LineMerger();
		for(SpatialEntity tempSE:segments)
		{
			lineMerger.add(tempSE.getGeometry());
		}
		
		if (lineMerger.getMergedLineStrings().size() != 1)
			throw new Exception("error while bulid geometry to route, the segment are not connceted");
		
		return (Geometry) lineMerger.getMergedLineStrings().iterator().next();
	}
	
	public void Dispose()
	{
		graphDB.shutdown();
	}
}

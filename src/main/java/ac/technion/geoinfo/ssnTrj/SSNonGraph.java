package ac.technion.geoinfo.ssnTrj;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.EditableLayerImpl;
import org.neo4j.gis.spatial.RTreeIndex;
import org.neo4j.gis.spatial.Search;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.WKTGeometryEncoder;
import org.neo4j.gis.spatial.query.SearchEqual;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;

import ac.technion.geoinfo.ssnTrj.domain.NodeWarpperImpl;
import ac.technion.geoinfo.ssnTrj.domain.SpatialEntity;
import ac.technion.geoinfo.ssnTrj.domain.SpatialEntityImpl;
import ac.technion.geoinfo.ssnTrj.domain.SpatialRelation;
import ac.technion.geoinfo.ssnTrj.domain.User;
import ac.technion.geoinfo.ssnTrj.domain.Static;
import ac.technion.geoinfo.ssnTrj.geometry.ColsestRoadSearch;
import ac.technion.geoinfo.ssnTrj.geometry.PolygonContainsSearch;
import ac.technion.geoinfo.ssnTrj.geometry.PolygonWithinSearch;
import ac.technion.geoinfo.ssnTrj.geometry.RoadIntersectSearch;
import ac.technion.geoinfo.ssnTrj.geometry.RoadTouchesSearch;
import ac.technion.geoinfo.ssnTrj.spatial.RTreeIndexFix;
import ac.technion.geoinfo.ssnTrj.spatial.SsnSpatialLayer;

public class SSNonGraph implements SSN, Static {

	private final GraphDatabaseService graphDB;
	private final SpatialDatabaseService sgDB;
	private EditableLayer spatialLyr;
	
	//const for map resolotion
	private final double METER = 8.98315E-06;
	private final double SEARCH_COLSEST_ROAD = 15 * METER;
	
	public SSNonGraph(String path)
	{
		graphDB = new EmbeddedGraphDatabase(path);
		sgDB = new SpatialDatabaseService(graphDB);
		graphDB.index().forNodes(SPATIAL_FULLTEXT_INDEX, 
				MapUtil.stringMap( IndexManager.PROVIDER, "lucene", "type", "fulltext", "to_lower_case", "true"));
		Transaction tx = sgDB.getDatabase().beginTx(); 
		try
		{
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
			tx.success();
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		finally
		{
			tx.finish();
		}
	}
	
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
	
	private Index<Node> getNodeIndex(String indexName)
	{
		return graphDB.index().forNodes(indexName);
	}
	
	public void executeSpatialSearch(Search theSearch)
	{
		spatialLyr.getIndex().executeSearch(theSearch);
	}
	
	public GeometryFactory getGeometryFactory()
	{
		return spatialLyr.getGeometryFactory();
	}
	
	public List<SpatialEntity> AddLocation(String geom, String[] attributes, Object[] values) throws Exception {
		List<SpatialEntity> theSE = null;
		Transaction tx = sgDB.getDatabase().beginTx(); 
		try
		{
			WKTReader reader = new WKTReader();
			Geometry seGeom = reader.read(geom);
			if (seGeom.getGeometryType().equalsIgnoreCase("polygon"))
			{
				theSE = InsertPolygon(seGeom, spatialLyr, attributes, values);
			}
			else if (seGeom.getGeometryType().equalsIgnoreCase("LineString"))
			{
				theSE = InsertLineString(seGeom, spatialLyr, attributes, values);
			}
			if (theSE == null)
			{
				throw new Exception("can not dedect geometry");
			}
			
			tx.success();
		}
		catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
			throw e;
		}
		finally
		{
			tx.finish();
		}
		
		 return theSE;
	}
	
	private List<SpatialEntity> InsertPolygon(Geometry theGeom, EditableLayer spatialLayer, String[] attributes, Object[] values) throws Exception
	{
		Search cointainsPoly = new PolygonContainsSearch(theGeom);
		Search withinPoly = new PolygonWithinSearch(theGeom);
		Search LeadTo = new ColsestRoadSearch(theGeom, SEARCH_COLSEST_ROAD);
		spatialLyr.getIndex().executeSearch(cointainsPoly);
		spatialLyr.getIndex().executeSearch(withinPoly);
		spatialLyr.getIndex().executeSearch(LeadTo);
		SpatialEntity newSE = AddSpatialEntity(theGeom, spatialLyr, BULIDING, attributes, values);
		for(SpatialDatabaseRecord tempSpatialRecord:cointainsPoly.getResults())
		{
			newSE.createRelationshipTo(tempSpatialRecord.getGeomNode(), SpatialRelation.within);
		}
		for(SpatialDatabaseRecord tempSpatialRecord:withinPoly.getResults())
		{
			tempSpatialRecord.getGeomNode().createRelationshipTo(newSE, SpatialRelation.within);
		}
		if (!LeadTo.getResults().isEmpty())
		{
			LeadTo.getResults().get(0).getGeomNode().createRelationshipTo(newSE, SpatialRelation.lead_to);
		}
		List<SpatialEntity> returnLst = new LinkedList<SpatialEntity>();
		returnLst.add(newSE);
		return returnLst;
	}
	
	private List<SpatialEntity> InsertLineString(Geometry theGeom, EditableLayer spatialLayer, String[] thisAttributes, Object[] thisValues) throws Exception
	{
		//Geometry tempGeom = extend((LineString)theGeom, 10*METER);
		//Geometry tempGeom = theGeom;
		Search lineStringSearch = new RoadIntersectSearch(theGeom);
		spatialLayer.getIndex().executeSearch(lineStringSearch);
		List<Geometry> needToBeAddedFromThis = new LinkedList<Geometry>();
		List<Geometry> needToBeAddedFromDB = new LinkedList<Geometry>();
		
		List<String[]> DBattributes = new LinkedList<String[]>();
		List<Object[]> DBvalues = new LinkedList<Object[]>();
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
		
		returnedList = new LinkedList<SpatialEntity>(); 
		for(int i = 0; i < needToBeAddedFromDB.size(); i++)
		{
			returnedList.add(AddRoadSegment(needToBeAddedFromDB.get(i), spatialLayer, 
					DBattributes.get(i), DBattributes.get(i)));
		}
		
		//spatialLayer.getIndex().executeSearch(lineStringSearch);
		for(SpatialDatabaseRecord tempSpatialRecord:lineStringSearch.getResults())
		{
			//in case that the new geometry cuts due to geometries in the database
			//Geometry other = extend((LineString)tempSpatialRecord.getGeometry(), 10*METER);
			Geometry other = tempSpatialRecord.getGeometry();
			List<Geometry> addTo_eedToBeAddedFromThis = new LinkedList<Geometry>();;
			List<Geometry> removeFrom_eedToBeAddedFromThis = new LinkedList<Geometry>();
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
			returnedList.add(AddRoadSegment(addMe, spatialLayer, thisAttributes, thisValues));
		}
		
		return returnedList;
    }
	
	private SpatialEntity AddRoadSegment(Geometry theGeom, EditableLayer spatialLayer, String[] attributes, Object[] values) throws Exception
	{
		SpatialEntity newSE = AddSpatialEntity(theGeom, spatialLayer, ROAD_SEGMENT, attributes, values);
		
		Search touchSearch = new RoadTouchesSearch(theGeom);
		spatialLayer.getIndex().executeSearch(touchSearch);
		for(SpatialDatabaseRecord tempSpatialRecord:touchSearch.getResults())
		{
			newSE.createRelationshipTo(tempSpatialRecord.getGeomNode(), SpatialRelation.touch);
		}
		
		return newSE;
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
		SpatialEntity newSE = new SpatialEntityImpl(new NodeWarpperImpl(spatialLayer.add(theGeom).getGeomNode()));	
		String fullIndexField = "";
		//add the attributes 
		if (attributes.length != values.length)
		{
			throw new Exception("the attributes and the values array are not in the same length");
		}
		for(int i = 0; i < attributes.length; i++)
		{
			newSE.setProperty(attributes[i], values[i]);
			fullIndexField = fullIndexField + values[i].toString() + ";";
		}
		//update the index
		if(!fullIndexField.isEmpty()){
			fullIndexField = fullIndexField.substring(0, fullIndexField.length() -1);
			Index<Node> LocationFulltxtInd = graphDB.index().forNodes(SPATIAL_FULLTEXT_INDEX); 
			LocationFulltxtInd.add(newSE.getNode() , SPATIAL_FULLTEXT_KEY, fullIndexField);
			newSE.setProperty(FULLTEXT_PROPERTY, fullIndexField);
		}
		
		newSE.setProperty(SSN_TYPE, type);
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
			System.out.println(spatialLayer.getIndex().count());
			//System.out.println("remove: " + removeRecord.getGeomNode().getId() + "-" + removeRecord.getId());
			//((RTreeIndexFix)spatialLayer.getIndex()).debugIndexTree();
			throw e;
		}
			
	}
	
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
	
	public User AddUser() {
		// TODO Auto-generated method stub
		//add a fulltext field 
		return null;
	}

	public void Dispose()
	{
		graphDB.shutdown();
	}
}

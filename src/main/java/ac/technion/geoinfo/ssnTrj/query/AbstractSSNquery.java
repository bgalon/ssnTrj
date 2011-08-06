package ac.technion.geoinfo.ssnTrj.query;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.query.SearchInRelation;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.Index;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

import ac.technion.geoinfo.ssnTrj.domain.NodeWrapperImpl;
import ac.technion.geoinfo.ssnTrj.domain.SpatialEntityImpl;
import ac.technion.geoinfo.ssnTrj.domain.Static;
import ac.technion.geoinfo.ssnTrj.SSN;
import ac.technion.geoinfo.ssnTrj.domain.NodeWrapper;

public abstract class AbstractSSNquery implements SSNquery, Static {

	SSN ssn;
	
	public AbstractSSNquery(SSN theSsn)
	{
		this.ssn = theSsn;
	}
	
	public Collection<NodeWrapper> Select(String source, String theQuery) throws Exception
	{
		Index<Node> theIndex;
		String fullTxtKey;
		if (source.equals("social")){
			theIndex = ssn.getSocialIndex();
			fullTxtKey = SOCIAL_FULLTEXT_KEY;
		}else if (source.equals("spatial")){
			theIndex = ssn.getSpatialIndex();
			fullTxtKey = SPATIAL_FULLTEXT_KEY;
			if (theQuery.toLowerCase().startsWith("in:"))
			{
				int layerInd = theQuery.toLowerCase().indexOf("@layer:");
				String lyr = theQuery.substring(layerInd + 7);
				String envelope = theQuery.substring(3, layerInd);
				return SelectByLoction(envelope, lyr);
			}
		}else{
			throw new Exception("network source mast be social or spatial");
		}
		List<NodeWrapper> returnLst =  new LinkedList<NodeWrapper>();
		for (Node tempNode:theIndex.query(fullTxtKey , theQuery))
		{
			returnLst.add(new NodeWrapperImpl(tempNode));
		}
		return returnLst;
	}
	
	private List<NodeWrapper> SelectByLoction(String theEnvelope, String lyr) throws Exception
	{
		//the theEnvelope = "MaxX,MaxY,MinX,MinY"
		
		String[] splitStr = theEnvelope.split(",");
		
		Coordinate[] coordinates = new Coordinate[5];
		coordinates[0] = coordinates[4] = new Coordinate(Double.parseDouble(splitStr[2]),Double.parseDouble(splitStr[1]));
		coordinates[1] = new Coordinate(Double.parseDouble(splitStr[0]),Double.parseDouble(splitStr[1]));
		coordinates[2] = new Coordinate(Double.parseDouble(splitStr[0]),Double.parseDouble(splitStr[3]));
		coordinates[3] = new Coordinate(Double.parseDouble(splitStr[2]),Double.parseDouble(splitStr[3]));

		Polygon other = ssn.getGeometryFactory().createPolygon(ssn.getGeometryFactory().createLinearRing(coordinates),null);
		SearchInRelation search = new SearchInRelation(other, "T*****T**");
		
		ssn.executeSpatialSearch(search, lyr);
		List<SpatialDatabaseRecord> result = search.getResults();
		List<NodeWrapper> retrunLst = new LinkedList<NodeWrapper>();
		for (SpatialDatabaseRecord tempRec : result)
		{
			retrunLst.add(new SpatialEntityImpl(tempRec.getGeomNode()));
		}
		return retrunLst;
	}

	public abstract Collection<NodeWrapper> Select(Collection<NodeWrapper> source, String theQuery) throws Exception;

	public abstract Collection<NodeWrapper> Extend(Collection<NodeWrapper> source, RelationshipType[] relationType, String[] conditions) throws Exception;
	public abstract Collection<NodeWrapper> Move(Collection<NodeWrapper> source, RelationshipType[] relationType, String[] conditions) throws Exception ;
	public abstract Collection<NodeWrapper> MultiMove(Collection<NodeWrapper> source, RelationshipType[] relationType, String[] conditions, double percentage);
	public abstract Collection<NodeWrapper> Union(Collection<NodeWrapper> source1, Collection<NodeWrapper> source2) throws Exception;
	public abstract Collection<NodeWrapper> Difference(Collection<NodeWrapper> source1,	Collection<NodeWrapper> source2) throws Exception;
	public abstract Collection<NodeWrapper> Intersect(Collection<NodeWrapper> source1, Collection<NodeWrapper> source2) throws Exception;
	
}

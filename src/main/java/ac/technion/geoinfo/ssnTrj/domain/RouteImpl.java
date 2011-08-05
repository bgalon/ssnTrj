package ac.technion.geoinfo.ssnTrj.domain;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class RouteImpl extends SpatialEntityImpl implements Route {

	public RouteImpl(Node theNode) throws Exception {
		super(theNode);
	}

	public SpatialEntity getStart() throws Exception {
		if(!underlayingNode.hasRelationship(SpatialRelation.startAt))
			return null;
		return new SpatialEntityImpl(underlayingNode.getSingleRelationship(
				SpatialRelation.startAt,Direction.OUTGOING).getEndNode());
	}

	public SpatialEntity getEnd() throws Exception {
		if(!underlayingNode.hasRelationship(SpatialRelation.endAt))
			return null;
		return new SpatialEntityImpl(underlayingNode.getSingleRelationship(
				SpatialRelation.endAt,Direction.OUTGOING).getEndNode());
	}

	public Collection<SpatialEntity> getSegments() throws Exception {
		Map<Integer,SpatialEntity> returnMap = new TreeMap<Integer, SpatialEntity>();
		Iterable<Relationship> includeIter = underlayingNode.getRelationships(SpatialRelation.include);
		for(Relationship tempRel:includeIter)
		{
			returnMap.put((Integer)tempRel.getProperty(SEGMENT_NUMBER), 
					new SpatialEntityImpl(tempRel.getOtherNode(underlayingNode)));
		}
		return returnMap.values();
	}
	
	public String RouteAsString() throws Exception
	{
		String returnStr = "";
		SpatialEntity tempSE1 = getStart();
		if (tempSE1 != null )
			returnStr = tempSE1.toString();
		for(SpatialEntity tempSE2:getSegments())
		{
			returnStr = returnStr + "-->" + tempSE2.toString();
		}
		tempSE1 = getEnd();
		if (tempSE1 != null )
			returnStr = returnStr + "-->" + tempSE1.toString();
		
		return returnStr;
	}
}

package ac.technion.geoinfo.ssnTrj.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
				SpatialRelation.startAt,Direction.OUTGOING).getOtherNode(underlayingNode));
	}

	public SpatialEntity getEnd() throws Exception {
		if(!underlayingNode.hasRelationship(SpatialRelation.endAt))
			return null;
		return new SpatialEntityImpl(underlayingNode.getSingleRelationship(
				SpatialRelation.endAt,Direction.OUTGOING).getOtherNode(underlayingNode));
	}

	public Collection<SpatialEntity> getSegments() throws Exception {
		Set<SpatialEntity> returnSet = new HashSet<SpatialEntity>();
		Iterable<Relationship> includeIter = underlayingNode.getRelationships(SpatialRelation.include);
		for(Relationship tempRel:includeIter)
		{
			returnSet.add(new SpatialEntityImpl(tempRel.getOtherNode(underlayingNode)));
		}
		return returnSet;
	}
}

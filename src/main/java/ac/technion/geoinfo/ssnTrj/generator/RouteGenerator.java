package ac.technion.geoinfo.ssnTrj.generator;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.kernel.Traversal;

import ac.technion.geoinfo.ssnTrj.SSN;
import ac.technion.geoinfo.ssnTrj.domain.SpatialEntity;
import ac.technion.geoinfo.ssnTrj.domain.SpatialEntityImpl;
import ac.technion.geoinfo.ssnTrj.domain.SpatialRelation;

public class RouteGenerator {
	
//	private final SSN ssn; 
//	
//	public RouteGenerator(SSN theSSNdb)
//	{
//		ssn = theSSNdb;
//	}
	
	public static SpatialEntity[] routeFind(SpatialEntity start, SpatialEntity end) throws Exception
	{
		SpatialEntity startSegment = new SpatialEntityImpl(start.getRelationships(SpatialRelation.lead_to).iterator().next().getOtherNode(start));
		SpatialEntity endSegment = new SpatialEntityImpl(end.getRelationships(SpatialRelation.lead_to).iterator().next().getOtherNode(end));
		PathFinder<Path> findShortst = GraphAlgoFactory.shortestPath(
				Traversal.expanderForTypes(SpatialRelation.touch, Direction.BOTH), 200);
		Path thePath = findShortst.findSinglePath(startSegment, endSegment);
		if (thePath == null) return null;
		SpatialEntity[] returnArray = new SpatialEntityImpl[thePath.length() + 1];
		int i = 0;
		for(Node tempNode:thePath.nodes())
		{
			returnArray[i] = new SpatialEntityImpl(tempNode);
			i++;
		}
		return returnArray;
	}
}

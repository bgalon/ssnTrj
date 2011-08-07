package ac.technion.geoinfo.ssnTrj.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import ac.technion.geoinfo.ssnTrj.SSN;
import ac.technion.geoinfo.ssnTrj.domain.NodeWrapperImpl;
import ac.technion.geoinfo.ssnTrj.domain.NodeWrapper;
import ac.technion.geoinfo.ssnTrj.domain.Route;
import ac.technion.geoinfo.ssnTrj.domain.RouteImpl;
import ac.technion.geoinfo.ssnTrj.domain.SpatialEntity;
import ac.technion.geoinfo.ssnTrj.domain.SpatialRelation;

public class SSNbfsQuery extends AbstractSSNquery {

	public SSNbfsQuery(SSN theSsn) {
		super(theSsn);
	}

	@Override
	public Collection<NodeWrapper> Select(Collection<NodeWrapper> source, String theQuery) throws Exception {
		Set<NodeWrapper> retrunSet = new HashSet<NodeWrapper>();
		String[] splitWorld = theQuery.trim().split(" ");
		boolean checkNode = true;
		for (NodeWrapper tempNode:source){
			for(String tempVal:splitWorld)
			{
				checkNode = checkNode && CheckNode(tempNode, FULLTEXT_PROPERTY, tempVal.trim().toLowerCase());
			}
			if (checkNode) retrunSet.add(tempNode);
		}
		return retrunSet;
	}

	private boolean CheckNode(Node theNode, String theProperty, String val) throws Exception{
		if (((String)theNode.getProperty(theProperty)).matches("(?i).*" + val + ".*")) {
			return true;
		}
		else{
			return false;
		}
	}
	
	@Override
	public Collection<NodeWrapper> Extend(Collection<NodeWrapper> source, RelationshipType[] relationType, String[] conditions) throws Exception {
		// TODO Auto-generated method stub
		return Union(source, Move(source, relationType, conditions));
	}

	@Override
	public Collection<NodeWrapper> Move(Collection<NodeWrapper> source, RelationshipType[] relationType, String[] conditions) throws Exception {
		//condition in a format of "propertyName1 operator1 condition1;propertyName2 operator2 condition2 ..."
		//for example "name == 'hello' ; sameNumber > 50"
		//optional operator are : < , > , <= , >= , ==
		if (relationType.length < 1)
			throw new Exception("relationType length is 0");
		if (conditions != null && relationType.length != conditions.length)
			throw new Exception("conditions length in not equal to relationType length");
		Set<NodeWrapper> returnSet = new HashSet<NodeWrapper>();
		for(NodeWrapper tempNodeW: source)
		{
			//boolean check = true;
			for(int i = 0; i < relationType.length; i++)
			{
				Iterable<Relationship> relIter = tempNodeW.getRelationships(relationType[i]);
				for(Relationship tempRel:relIter)
				{
					String Cond;
					if (conditions == null)
					{
						Cond = null;
					}else
					{
						Cond = conditions[i];
					}
					if(CheckCond.CheckRel4Con(tempRel, Cond))
					{
						returnSet.add(new NodeWrapperImpl(tempRel.getOtherNode(tempNodeW)));
					}
					//check = check && CheckRel4Con(tempRel, conditions[i]);
				}
				//if (check)
				//	returnSet.add(relIter);
			}
		}
		return returnSet;
	}
	


	@Override
	public Collection<NodeWrapper> MultiMove(Collection<NodeWrapper> source, RelationshipType[] relationType, String[] conditions, double percentage) throws Exception 
	{
		Map<Long,NodeWrapper> firstMove = MoveMap(source, relationType, conditions);
		
		double leadToTrashold = (double)source.size()*percentage;
		
		Set<NodeWrapper> retrunSet = new HashSet<NodeWrapper>();
		
		for (NodeWrapper tempNode:firstMove.values()){
			if (tempNode.getLeads() >= leadToTrashold)
				retrunSet.add(tempNode);
		}
		return retrunSet;
	}
	
	public Collection<NodeWrapper> MultiMoveByNum(Collection<NodeWrapper> source, RelationshipType[] relationType, String[] conditions, int numOfLeads) throws Exception 
	{
		Map<Long,NodeWrapper> firstMove = MoveMap(source, relationType, conditions);
		
		Set<NodeWrapper> retrunSet = new HashSet<NodeWrapper>();
		
		for (NodeWrapper tempNode:firstMove.values()){
			if (tempNode.getLeads() >= numOfLeads)
				retrunSet.add(tempNode);
		}
		return retrunSet;
	}

	
	private Map<Long,NodeWrapper> MoveMap (Collection<NodeWrapper> source, RelationshipType[] relationType, String[] conditions) throws Exception{
		//condition in a format of "propertyName1 operator1 condition1;propertyName2 operator2 condition2 ..."
		//for example "name == 'hello' ; sameNumber > 50"
		//optional operator are : < , > , <= , >= , ==
		if (relationType.length < 1)
			throw new Exception("relationType length is 0");
		if (conditions != null && relationType.length != conditions.length)
			throw new Exception("conditions length in not equal to relationType length");
		Map<Long,NodeWrapper> returnMap = new HashMap<Long,NodeWrapper>();
		for(NodeWrapper tempNodeW: source)
		{
			//boolean check = true;
			for(int i = 0; i < relationType.length; i++)
			{
				Iterable<Relationship> relIter = tempNodeW.getRelationships(relationType[i]);
				for(Relationship tempRel:relIter)
				{
					String Cond;
					if (conditions == null)
					{
						Cond = null;
					}else
					{
						Cond = conditions[i];
					}
					if(CheckCond.CheckRel4Con(tempRel, Cond))
					{
						NodeWrapper addNode = new NodeWrapperImpl(tempRel.getOtherNode(tempNodeW));
						if(returnMap.containsKey(addNode.getId()))
						{
							returnMap.get(addNode.getId()).addLaed();
						}
						else
						{
							returnMap.put(addNode.getId(), addNode);
						}
					}
					//check = check && CheckRel4Con(tempRel, conditions[i]);
				}
				//if (check)
				//	returnSet.add(relIter);
			}
		}
		return returnMap;
	}
	
	@Override
	public Collection<NodeWrapper> Union(Collection<NodeWrapper> source1, Collection<NodeWrapper> source2) throws Exception {
		if (!source2.iterator().next().getType().equals(source1.iterator().next().getType())) throw new Exception("the input list are not of the same type, Union Error");
		Set<NodeWrapper> returnSet = new HashSet<NodeWrapper>(source1);
		returnSet.addAll(source2);
		return returnSet;
	}

	@Override
	public Collection<NodeWrapper> Difference(Collection<NodeWrapper> source1, Collection<NodeWrapper> source2) throws Exception {
		if (!source2.iterator().next().getType().equals(source1.iterator().next().getType())) throw new Exception("the input list are not of the same type, Union Error");
		Set<NodeWrapper> returnSet = new HashSet<NodeWrapper>(source1);
		returnSet.removeAll(source2);
		return returnSet;
	}

	@Override
	public Collection<NodeWrapper> Intersect(Collection<NodeWrapper> source1, Collection<NodeWrapper> source2) throws Exception {
		if (source1.isEmpty() && source2.isEmpty())
			return new HashSet<NodeWrapper>();
		if (source1.isEmpty() && !source2.isEmpty())
			return source2;
		if (!source1.isEmpty() && source2.isEmpty())
			return source1;
		if (!source2.iterator().next().getType().equals(source1.iterator().next().getType())) throw new Exception("the input list are not of the same type, Union Error");
		Set<NodeWrapper> returnSet = new HashSet<NodeWrapper>(source1);
		returnSet.retainAll(source2);
		return returnSet;
	}
	
	public Collection<NodeWrapper> RoutesPassThrow(Collection<NodeWrapper> source, SpatialEntity A, SpatialEntity B) throws Exception
	{
		Set<NodeWrapper> returnSet = new HashSet<NodeWrapper>();
		for(NodeWrapper tempNodew:source)
		{
			if(findRoutesPassThrow(new RouteImpl(tempNodew), A, B))
				returnSet.add(tempNodew);
		}
		return returnSet;
	}
	
	private boolean findRoutesPassThrow(Route R, SpatialEntity A, SpatialEntity B) throws Exception
	{
		Set<NodeWrapper> NodeAscollection = new HashSet<NodeWrapper>();
		NodeAscollection.add(R);
		Collection<NodeWrapper> startNode = Move(NodeAscollection, new RelationshipType[]{SpatialRelation.startAt}, null);
		Collection<NodeWrapper> endNode = Move(NodeAscollection, new RelationshipType[]{SpatialRelation.endAt}, null);
		Collection<NodeWrapper> roadSegment = Move(NodeAscollection, new RelationshipType[]{SpatialRelation.include}, null);
		Set<NodeWrapper> NearRoadSegment = (Set<NodeWrapper>)Move(roadSegment, new RelationshipType[]{SpatialRelation.lead_to}, null);
		NearRoadSegment = (Set<NodeWrapper>)Union(Union(startNode, endNode), NearRoadSegment);
		if (NearRoadSegment.contains((NodeWrapper)A) && NearRoadSegment.contains((NodeWrapper)B))
			return true;
		return false;
	}

}

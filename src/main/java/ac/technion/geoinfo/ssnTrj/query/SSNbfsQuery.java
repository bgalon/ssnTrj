package ac.technion.geoinfo.ssnTrj.query;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.management.relation.RelationType;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import ac.technion.geoinfo.ssnTrj.SSN;
import ac.technion.geoinfo.ssnTrj.domain.NodeWarpperImpl;
import ac.technion.geoinfo.ssnTrj.domain.NodeWrapper;

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
		if (relationType.length != conditions.length)
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
					if(CheckCond.CheckRel4Con(tempRel, conditions[i]))
					{
						returnSet.add(new NodeWarpperImpl(tempRel.getOtherNode(tempNodeW)));
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
	public Collection<NodeWrapper> MultiMove(Collection<NodeWrapper> source, RelationshipType[] relationType, String[] conditions, double percentage) {
		// TODO Auto-generated method stub
		return null;
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
		if (!source2.iterator().next().getType().equals(source1.iterator().next().getType())) throw new Exception("the input list are not of the same type, Union Error");
		Set<NodeWrapper> returnSet = new HashSet<NodeWrapper>(source1);
		returnSet.retainAll(source2);
		return returnSet;
	}

}

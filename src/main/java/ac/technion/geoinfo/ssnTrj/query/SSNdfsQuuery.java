package ac.technion.geoinfo.ssnTrj.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import org.geotools.filter.IsEqualsToImpl;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import ac.technion.geoinfo.ssnTrj.SSN;
import ac.technion.geoinfo.ssnTrj.domain.NodeWarpperImpl;
import ac.technion.geoinfo.ssnTrj.domain.NodeWrapper;

public class SSNdfsQuuery {
	private final SSN ssn;
	private Set<NodeWrapper> result;
	
	public SSNdfsQuuery(SSN theSSN)
	{
		this.ssn = theSSN;
	}
	
	public Collection<NodeWrapper> RunQuery(Collection<NodeWrapper> source, Queue<RelationshipType[]> relationType, 
			Queue<String[]> conditions, Queue<Boolean> toExtend) throws Exception
	{
		result = new HashSet<NodeWrapper>();
		for(NodeWrapper tempNodeW:source)
		{
			RecursiveQuery(tempNodeW, relationType, conditions, toExtend);
		}
		return result;
	}
	
	public void RecursiveQuery(NodeWrapper source, Queue<RelationshipType[]> relationType, 
			Queue<String[]> conditions, Queue<Boolean> toExtend) throws Exception
	{
		if(relationType.isEmpty())
		{
			return;
		}
		RelationshipType[] relTypes4This = relationType.poll();
		String[] con4This = conditions.poll();
		boolean toExtend4This = toExtend.poll();
		if (relTypes4This.length != con4This.length)
			throw new Exception("relTypes4This not in the same size of con4This");
		for(int i = 0; i < relTypes4This.length; i++)
		{
			if(source.hasRelationship(relTypes4This[i]))
			{
				for (Relationship tempRel:source.getRelationships(relTypes4This[i]))
				{
					if(CheckCond.CheckRel4Con(tempRel, con4This[i]))
					{
						NodeWrapper nextNode = new NodeWarpperImpl(tempRel.getOtherNode(source));
						result.add(nextNode);
						if(!toExtend4This) result.remove(source); //think about the remove operation
						RecursiveQuery(nextNode, relationType,conditions,toExtend);
					}
				}
			}
		}
	}
}

package ac.technion.geoinfo.ssnTrj.query;

import java.util.Collection;

import org.neo4j.graphdb.Relationship;

import ac.technion.geoinfo.ssnTrj.domain.NodeWrapper;

public interface SSNquery {

	Collection<NodeWrapper> Select(String source, String theQuery);
	Collection<NodeWrapper> Select(Collection<NodeWrapper> source, String theQuery);
	
	Collection<NodeWrapper> Extend(Collection<NodeWrapper> source, Relationship[] relationType, String[] conditions);
	Collection<NodeWrapper> Move(Collection<NodeWrapper> source, Relationship[] relationType, String[] conditions);
	
	Collection<NodeWrapper> MultiMove(Collection<NodeWrapper> source, Relationship[] relationType, String[] conditions, double percentage);
	
	Collection<NodeWrapper> Union(Collection<NodeWrapper> source1, Collection<NodeWrapper> source2);
	Collection<NodeWrapper> Difference(Collection<NodeWrapper> source1, Collection<NodeWrapper> source2);
	Collection<NodeWrapper> Intersect(Collection<NodeWrapper> source1, Collection<NodeWrapper> source2);
	
}

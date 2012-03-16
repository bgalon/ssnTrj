package ac.technion.geoinfo.ssnTrj.query;

import java.util.Collection;

import org.neo4j.graphdb.RelationshipType;

import ac.technion.geoinfo.ssnTrj.domain.NodeWrapper;

public interface SSNquery {

	Collection<NodeWrapper> Select(String source, String theQuery) throws Exception;
	Collection<NodeWrapper> Select(Collection<NodeWrapper> source, String theQuery) throws Exception;
	
//	Collection<NodeWrapper> Extend(Collection<NodeWrapper> source, RelationshipType[] relationType, String[] conditions) throws Exception;
	Collection<NodeWrapper> Move(Collection<NodeWrapper> source, RelationshipType[] relationType, String[] conditions) throws Exception;
	
	Collection<NodeWrapper> MultiMove(Collection<NodeWrapper> source, RelationshipType[] relationType, String[] conditions, double percentage) throws Exception;
	
	Collection<NodeWrapper> Union(Collection<NodeWrapper> source1, Collection<NodeWrapper> source2) throws Exception;
	Collection<NodeWrapper> Difference(Collection<NodeWrapper> source1, Collection<NodeWrapper> source2) throws Exception;
	Collection<NodeWrapper> Intersect(Collection<NodeWrapper> source1, Collection<NodeWrapper> source2) throws Exception;
	
}

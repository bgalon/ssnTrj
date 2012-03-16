package ac.technion.geoinfo.ssnTrj.indexes.temporal;

import java.util.Collection;

import ac.technion.geoinfo.ssnTrj.domain.NodeWrapper;

public interface TemopralIndex {
	
	void Add(NodeWrapper toInsert, long startTime, long duration) throws Exception;
	Collection<NodeWrapper> Search(long startTime, long duration);
	
}

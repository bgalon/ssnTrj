package ac.technion.geoinfo.ssnTrj.indexes.temporal;

import java.util.Collection;

import ac.technion.geoinfo.ssnTrj.domain.NodeWrapper;
import ac.technion.geoinfo.ssnTrj.domain.TimeInterval;

public interface TemopralIndex {
	
	void Add(NodeWrapper toInsert, long startTime, long endTime) throws Exception;
	Collection<NodeWrapper> Search(long startTime, long endTime) throws Exception;
	
	void Add(NodeWrapper toInsert, TimeInterval addInter) throws Exception;
	Collection<NodeWrapper> Search(TimeInterval searchInter) throws Exception;
	
	long getStartTime();
	long getEndTime();
	
	int NumberOfLeaves();
}

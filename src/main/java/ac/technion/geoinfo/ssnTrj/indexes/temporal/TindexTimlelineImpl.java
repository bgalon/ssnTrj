package ac.technion.geoinfo.ssnTrj.indexes.temporal;

import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import ac.technion.geoinfo.ssnTrj.domain.NodeWrapper;
import ac.technion.geoinfo.ssnTrj.domain.TimeInterval;
import ac.technion.geoinfo.ssnTrj.domain.TimeIntervalImpl;

public class TindexTimlelineImpl extends TindexExplicitBase implements TemporalIndex {

	public TindexTimlelineImpl(Node nodeWithIndex) throws Exception
	{
		super(nodeWithIndex);
	}
	
	
	private void AddNode()
	{
		
	}
	
	
	public void Add(NodeWrapper toInsert, TimeInterval addInter) throws Exception {
		Add(toInsert, addInter.GetStartTime(), addInter.GetEndTime());
	}
	
	public void Add(NodeWrapper toInsert, long startTime, long endTime) throws Exception {
		Transaction tx = treeRoot.getGraphDatabase().beginTx(); 
		try
		{
			//**build the index for the first time
			if (treeRoot.getInclude() == null) {
				//initIndex(toInsert, startTime, endTime);
			}else{
				
				
				
			}
			tx.success();
		}
		catch (Exception e) {
			tx.failure();
			throw e;
		}
		finally
		{
			tx.finish();
		}
	}

	
	private void FindAddChain(TemporalIndexNode father, NodeWrapper toInsert, long t)
	{
		
		
	}
	
	
	public Collection<NodeWrapper> Search(long startTime, long endTime) throws Exception {
		return Search(new TimeIntervalImpl(startTime, endTime));
	}

	public Collection<NodeWrapper> Search(TimeInterval searchInter) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Collection<NodeWrapper> Search(Map<Long, NodeWrapper> addToThis, long startTime, long endTime) throws Exception {
		return Search(addToThis, new TimeIntervalImpl(startTime, endTime));
	}
	
	public Collection<NodeWrapper> Search(Map<Long, NodeWrapper> addToThis, TimeInterval searchInter) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}

package ac.technion.geoinfo.ssnTrj.indexes.temporal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import ac.technion.geoinfo.ssnTrj.domain.NodeWrapper;
import ac.technion.geoinfo.ssnTrj.domain.NodeWrapperImpl;
import ac.technion.geoinfo.ssnTrj.domain.TimeInterval;
import ac.technion.geoinfo.ssnTrj.domain.TimeIntervalImpl;
import ac.technion.geoinfo.ssnTrj.domain.TimeIntervalIntersection;
import ac.technion.geoinfo.ssnTrj.domain.TimePatternRelation;

public class TindexRelList implements TemporalIndex, TemporalStatic {

	static final String LIST_PROPERTY = "time_list"; 
	protected TemporalIndexNode treeRoot;
	
	public TindexRelList(Node nodeWithIndex){
		treeRoot = new TemporalIndexNode(nodeWithIndex);
	}
	
	public long getStartTime() {
		if (treeRoot.hasProperty(START_TIME))
			return (Long)treeRoot.getProperty(START_TIME);
		return -1;
	}

	public long getEndTime() {
		if (treeRoot.hasProperty(END_TIME))
			return (Long)treeRoot.getProperty(END_TIME);
		return -1;
	}

	public int NumberOfLeaves() {
		if (treeRoot.hasProperty(NUMBER_OF_LEAVES))
			return (Integer)treeRoot.getProperty(NUMBER_OF_LEAVES);
		return 0;
	}
	
	
	public void Add(NodeWrapper toInsert, long startTime, long endTime) throws Exception {
		Add(toInsert, new TimeIntervalImpl(startTime, endTime));
	}
	
	public void Add(NodeWrapper toInsert, TimeInterval addInter)	throws Exception {
		Transaction tx = treeRoot.getGraphDatabase().beginTx(); 
		try
		{
			Relationship underlayingRel = null;
			Iterable<Relationship> rels = treeRoot.getRelationships(TimePatternRelation.tpIndex, Direction.OUTGOING);
			for(Relationship tempRel:rels){
				if(tempRel.getOtherNode(treeRoot).equals(toInsert)){
					underlayingRel = tempRel;
					break;
				}
			}
			if(underlayingRel == null) underlayingRel = treeRoot.createRelationshipTo(toInsert, TimePatternRelation.tpIndex);
			List<TimeInterval> theList = readList(underlayingRel);
			theList.add(addInter);
			writeList(theList, underlayingRel);
			
			upDateProperties(toInsert, addInter);
			
			tx.success();
		}catch (Exception e) {
			tx.failure();
			throw e;
		}
		finally
		{
			tx.finish();
		}
		
//		TemporalIndex reverseIndex = new TindexRelList(toInsert);
//		reverseIndex.Add(treeRoot, addInter);
	}
	
	protected void upDateProperties(NodeWrapper toInsert, TimeInterval addInter){

		if (treeRoot.hasProperty(START_TIME)){
			long indexStartTime = (Long)treeRoot.getProperty(START_TIME);
			if (addInter.GetStartTime() < indexStartTime) treeRoot.setProperty(START_TIME, addInter.GetStartTime());
		}else{
			treeRoot.setProperty(START_TIME, addInter.GetStartTime());
		}
		if (treeRoot.hasProperty(END_TIME)){
			long indexEndTime = (Long)treeRoot.getProperty(END_TIME);
			if(addInter.GetEndTime() > indexEndTime) treeRoot.setProperty(END_TIME, addInter.GetEndTime());
		}else{
			treeRoot.setProperty(END_TIME, addInter.GetEndTime());
		}
		
		if(treeRoot.hasProperty(NUMBER_OF_LEAVES)){
			int numSoFar = (Integer)treeRoot.getProperty(NUMBER_OF_LEAVES);
			treeRoot.setProperty(NUMBER_OF_LEAVES, numSoFar + 1);
		}else{
			treeRoot.setProperty(NUMBER_OF_LEAVES, 1);
		}
	}
	
	public Collection<NodeWrapper> Search(Map<Long, NodeWrapper> addToThis, long startTime, long endTime) throws Exception {
		return Search(addToThis, new TimeIntervalImpl(startTime, endTime));
	}

	public Collection<NodeWrapper> Search(Map<Long, NodeWrapper> addToThis, TimeInterval searchInter) throws Exception {
		//long startOnTree = tempCal.getTimeInMillis();
		//Map<Long,NodeWrapper> retrunColl = new HashMap<Long,NodeWrapper>();
		Iterable<Relationship> rels = treeRoot.getRelationships(TimePatternRelation.tpIndex, Direction.OUTGOING);
		for(Relationship tempRel:rels){
			int countIner = hasTimeIntersection(searchInter, tempRel);
			if(countIner > 0){
				NodeWrapper otherNode = new NodeWrapperImpl(tempRel.getOtherNode(treeRoot));
				if(addToThis.containsKey(otherNode.getId())){
					addToThis.get(otherNode.getId()).addLaeds(countIner);
				}else{
					addToThis.put(otherNode.getId(), otherNode);
					otherNode.addLaeds(countIner - 1);
				}
			}
		}
		return addToThis.values();
	}

	

	public Collection<NodeWrapper> Search(long startTime, long endTime) throws Exception {
		return Search(new TimeIntervalImpl(startTime, endTime));
	}
	
	public Collection<NodeWrapper> Search(TimeInterval searchInter) throws Exception {
		Map<Long,NodeWrapper> retrunColl = new HashMap<Long,NodeWrapper>();
		return Search(retrunColl, searchInter);
	}
	
//	public boolean hasTimeIntersection(long startFromOnTree,TimeInterval searchInter, Relationship underlayingRel) throws ClassNotFoundException, IOException {
//		List<TimeInterval> theTree = readList(underlayingRel);
//		for(TimeInterval tempInterval:theTree)
//		{
//			TimeIntervalIntersection relate = searchInter.intersect(tempInterval);
//			if (relate == TimeIntervalIntersection.intersect) return true;
//		}
//		return false;
//	}
	
	public int hasTimeIntersection(TimeInterval searchInter, Relationship underlayingRel) throws ClassNotFoundException, IOException {
		List<TimeInterval> theTree = readList(underlayingRel);
		int countIntersection = 0;
		for(TimeInterval tempInterval:theTree)
		{
			TimeIntervalIntersection relate = searchInter.intersect(tempInterval);
			if (relate == TimeIntervalIntersection.intersect) countIntersection ++;
			//if (relate == TimeIntervalIntersection.after) 
		}
		return countIntersection;
	}
	
	@SuppressWarnings("unchecked")
	protected List<TimeInterval> readList(Relationship underlayingRel) throws IOException, ClassNotFoundException{
		if (!underlayingRel.hasProperty(LIST_PROPERTY))
			return new ArrayList<TimeInterval>();
		byte[] binTree = (byte[]) underlayingRel.getProperty(LIST_PROPERTY);
		ByteArrayInputStream bis = new ByteArrayInputStream(binTree);
		ObjectInput in = new ObjectInputStream(bis);
		List<TimeInterval> theList = (ArrayList<TimeInterval>) in.readObject(); 
		bis.close();
		in.close();
		return theList;
	}
	
	protected void writeList(List<TimeInterval> theList, Relationship underlayingRel) throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream(bos);
		out.writeObject(theList);
		
		underlayingRel.setProperty(LIST_PROPERTY, bos.toByteArray());
		out.close();
		bos.close();
	}
}

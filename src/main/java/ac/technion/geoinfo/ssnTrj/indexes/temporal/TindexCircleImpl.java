package ac.technion.geoinfo.ssnTrj.indexes.temporal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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

public class TindexCircleImpl extends TindexExplicitBase implements TemporalIndex {
	//Assuming that time interval is in one day
	
	
	public TindexCircleImpl(Node nodeWithIndex) throws Exception
	{
		super(nodeWithIndex);
	}
	

	
	public void Add(NodeWrapper toInsert, TimeInterval addInter) throws Exception {
		Add(toInsert, addInter.GetStartTime(), addInter.GetEndTime());
	}
	
	public void Add(NodeWrapper toInsert, long startTime, long endTime) throws Exception {
		//TODO: add a check if the time interval is in the same day
		
		Transaction tx = treeRoot.getGraphDatabase().beginTx(); 
		try
		{
			//**build the index for the first time
			if (treeRoot.getInclude() == null) {
				initIndex(toInsert, startTime, endTime);
			}else{
				Date startDate = new Date(startTime);
				Calendar startCal = Calendar.getInstance();
				startCal.setTime(startDate);
				//**find the time node
				TemporalIndexNode yearNode = FindAdd(treeRoot, TindexCircleImpl.class.getDeclaredMethod("createYearNode", Calendar.class), 
						startCal, startTime, endTime);
				TemporalIndexNode monthNode = FindAdd(yearNode, TindexCircleImpl.class.getDeclaredMethod("createMonthNode", Calendar.class), 
						startCal, startTime, endTime);
				TemporalIndexNode weekNode = FindAdd(monthNode, TindexCircleImpl.class.getDeclaredMethod("createWeekNode", Calendar.class), 
						startCal, startTime, endTime);
				TemporalIndexNode dayNode = FindAdd(weekNode, TindexCircleImpl.class.getDeclaredMethod("createDayNode", Calendar.class), 
						startCal, startTime, endTime);
				TemporalIndexNode timeNode = FindAdd(dayNode, TindexCircleImpl.class.getDeclaredMethod("createTimeNode", Calendar.class), 
						startCal, startTime, endTime);
				timeNode.setProperty(END_TIME, endTime);
				//for debuging.
	//			if (timeNode.hasRelationship(circleTempoalRelTypes.time_reference, Direction.OUTGOING))
	//				System.out.println("!******Error Here*******!");
	//				
				timeNode.createRelationshipTo(toInsert, circleTempoalRelTypes.time_reference);
				
				long indexStartTime = (Long)treeRoot.getProperty(START_TIME);
				if (startTime < indexStartTime) treeRoot.setProperty(START_TIME, startTime);
				long indexEndTime = (Long)treeRoot.getProperty(END_TIME);
				if(endTime > indexEndTime) treeRoot.setProperty(END_TIME, endTime);
				
				
				int numSoFar = (Integer)treeRoot.getProperty(NUMBER_OF_LEAVES);
				treeRoot.setProperty(NUMBER_OF_LEAVES, numSoFar + 1);
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
		
//		TemporalIndex reverseIndex = new TindexCircleImpl(toInsert);
//		reverseIndex.Add(new NodeWrapperImpl(treeRoot.getSingleRelationship(TimePatternRelation.tpIndex, Direction.INCOMING).getEndNode()),
//					startTime, endTime);
	}
	
	private TemporalIndexNode FindAdd(TemporalIndexNode rootNode, Method theNodeCerator, Calendar cal,
			long startTime, long endTime) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		TemporalIndexNode returnNode = null;
		Relationship leadRel = rootNode.getSingleRelationship(circleTempoalRelTypes.inclue_time, Direction.OUTGOING);
		if(leadRel == null){//start a new node in a Chain
			returnNode = (TemporalIndexNode)theNodeCerator.invoke(this, cal);
			rootNode.createRelationshipTo(returnNode, circleTempoalRelTypes.inclue_time);
			return returnNode;
		}
		//**find node in the index 
		while(leadRel != null){
			returnNode = new TemporalIndexNode(leadRel.getEndNode());
			int relate = IntervalChack(startTime, endTime, returnNode.getStartTime(), returnNode.getEndTime());
			if (relate == 1){ 
//				if(returnNode.hasRelationship(circleTempoalRelTypes.time_reference, Direction.OUTGOING)){
//					System.out.println("WTF Times " + startTime + "-" + endTime);
//					System.out.println("WTF Times " + new Date(startTime) + "-" + new Date(endTime));
//					System.out.println("WTF Node " + returnNode.getStartTime() + "-" + returnNode.getEndTime());
//					System.out.println("WTF Node " + new Date(returnNode.getStartTime()) + "-" + new Date(returnNode.getEndTime()));
//				}
				break; //find it (the node, of course)
			}
			if (relate == 0){		//pass it, add an new node
				TemporalIndexNode tempReturnNode = (TemporalIndexNode) theNodeCerator.invoke(this, cal);
				leadRel.getStartNode().createRelationshipTo(tempReturnNode, leadRel.getType());
				leadRel.delete();
				tempReturnNode.createRelationshipTo(returnNode, circleTempoalRelTypes.next_time);
				returnNode = tempReturnNode;
				break;
			}
			leadRel = returnNode.getSingleRelationship(circleTempoalRelTypes.next_time, Direction.OUTGOING);
		}
		if(leadRel == null){			//add new node node at the end
			TemporalIndexNode tempReturnNode = (TemporalIndexNode)theNodeCerator.invoke(this, cal);
			returnNode.createRelationshipTo(tempReturnNode, circleTempoalRelTypes.next_time);
			returnNode = tempReturnNode;
		}
		return returnNode;
	}
	
	private void initIndex(NodeWrapper toInsert, long startTime, long endTime)
	{
		//assuming that the startTime and the end Time are in the same day
		Calendar tempCal = Calendar.getInstance();
		tempCal.setTimeInMillis(startTime);
		TemporalIndexNode yearNode = createYearNode(tempCal.get(Calendar.YEAR));
		TemporalIndexNode monthNode = createMonthNode(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH));
		TemporalIndexNode weekNode = createWeekNode(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), 
				tempCal.get(Calendar.WEEK_OF_MONTH));
		TemporalIndexNode dayNode = createDayNode(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), 
				tempCal.get(Calendar.WEEK_OF_MONTH),tempCal.get(Calendar.DAY_OF_WEEK));
		TemporalIndexNode timeNode = createTimeNode(startTime, endTime);
		treeRoot.createRelationshipTo(yearNode, circleTempoalRelTypes.inclue_time);
		yearNode.createRelationshipTo(monthNode, circleTempoalRelTypes.inclue_time);
		monthNode.createRelationshipTo(weekNode, circleTempoalRelTypes.inclue_time);
		weekNode.createRelationshipTo(dayNode, circleTempoalRelTypes.inclue_time);
		dayNode.createRelationshipTo(timeNode, circleTempoalRelTypes.inclue_time);
		timeNode.createRelationshipTo(toInsert, circleTempoalRelTypes.time_reference);
		
		treeRoot.setProperty(START_TIME, startTime);
		treeRoot.setProperty(END_TIME, endTime);
		
		treeRoot.setProperty(NUMBER_OF_LEAVES, 1);
	}
	

	public Collection<NodeWrapper> Search(Map<Long, NodeWrapper> addToThis, long startTime, long endTime) throws Exception {
		return Search(addToThis, new TimeIntervalImpl(startTime, endTime));
	}

	public Collection<NodeWrapper> Search(Map<Long, NodeWrapper> addToThis, TimeInterval searchInter) throws Exception {
		TemporalIndexNode startHere = new TemporalIndexNode(treeRoot.getSingleRelationship(circleTempoalRelTypes.inclue_time, Direction.OUTGOING).getEndNode());
		Visit(addToThis, startHere, searchInter);
		return addToThis.values();
	}
	
	public Collection<NodeWrapper> Search(TimeInterval inter) throws Exception {
//		return Search(inter.GetStartTime(), inter.GetEndTime());
		Map<Long,NodeWrapper> retrunColl = new HashMap<Long,NodeWrapper>();
		TemporalIndexNode startHere = new TemporalIndexNode(treeRoot.getSingleRelationship(circleTempoalRelTypes.inclue_time, Direction.OUTGOING).getEndNode());
		Visit(retrunColl, startHere, inter);
		return retrunColl.values();
	}
	
	public Collection<NodeWrapper> Search(long startTime, long endTime) throws Exception {
		return Search(new TimeIntervalImpl(startTime, endTime));
	}
	
	private void Visit(Map<Long,NodeWrapper> retrunColl, TemporalIndexNode indexNode, TimeInterval timeSearch) throws Exception{
//		int related = IntervalChack(startTime, endTime, indexNode.getStartTime(), indexNode.getEndTime());
		TimeIntervalIntersection related = timeSearch.intersect(new TimeIntervalImpl(indexNode.getStartTime(), indexNode.getEndTime()));
		if(related == TimeIntervalIntersection.before) return; //in case we pass it
		if(related == TimeIntervalIntersection.intersect){
			//in case of intersection
			if(indexNode.hasRelationship(circleTempoalRelTypes.inclue_time, Direction.OUTGOING)){
				//not a leaf
				TemporalIndexNode nextNode = new TemporalIndexNode(indexNode.getSingleRelationship(circleTempoalRelTypes.inclue_time, Direction.OUTGOING).getEndNode());
				Visit(retrunColl, nextNode, timeSearch);
			}else if(indexNode.hasRelationship(circleTempoalRelTypes.time_reference, Direction.OUTGOING)){
				//is a leaf
				//	if(indexNode.)
				Iterable<Relationship> nextNodes = indexNode.getRelationships(circleTempoalRelTypes.time_reference, Direction.OUTGOING);
				for(Relationship temprel:nextNodes){
					NodeWrapper nextNode = new NodeWrapperImpl(temprel.getEndNode());
					if(retrunColl.containsKey(nextNode.getId())){
						retrunColl.get(nextNode.getId()).addLaed();
					}else{
						retrunColl.put(nextNode.getId(), nextNode);
					}
				}
			}
		}
		//keep goging 
		Relationship nextRel = indexNode.getSingleRelationship(circleTempoalRelTypes.next_time, Direction.OUTGOING);
		if (nextRel != null){
			TemporalIndexNode nextNode = new TemporalIndexNode(nextRel.getEndNode());
			Visit(retrunColl, nextNode, timeSearch);	
		}
	}

	private int IntervalChack(long i1start, long i1end, long i2start, long i2end){
		/**
		 * Return 0 if the interval 1 is before interval 2
		 * Return 1 if the Intervals intersects
		 * Return 2 if the Interval 1 is after interval 2 
		 **/
		if (i1start > i2start){
			if (i1start > i2end) return 2;
			return 1;
		}else{ //in case (i1start <= i2start)
			if (i2start > i1end) return 0;
			return 1;
		}
	}
	
	
	
}

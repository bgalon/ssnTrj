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
import ac.technion.geoinfo.ssnTrj.domain.TimePatternRelation;

public class TindexCircleImpl implements TemporalIndex, TemporalStatic {
	//Assuming that time interval is in one day
	TemporalIndexNode treeRoot;
	
	public TindexCircleImpl(Node nodeWithIndex) throws Exception
	{
		//TODO: add checks here to if theRoot is a root Node.
		Relationship rel = nodeWithIndex.getSingleRelationship(TimePatternRelation.tpIndex, Direction.OUTGOING);
		if(rel != null){
			treeRoot = new TemporalIndexNode(rel.getEndNode());
		}else{
			Transaction tx = nodeWithIndex.getGraphDatabase().beginTx(); 
			try
			{
				treeRoot = new TemporalIndexNode(nodeWithIndex.getGraphDatabase().createNode());
				nodeWithIndex.createRelationshipTo(treeRoot, TimePatternRelation.tpIndex);
				tx.success();
			}catch (Exception e) {
				// TODO: handle exception
				tx.failure();
				throw e;
			}
			finally
			{
			tx.finish();
			}
		}
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
		//add a check if the time interval is in the same day
		return Search(new TimeIntervalImpl(startTime, endTime));
//		Map<Long,NodeWrapper> retrunColl = new HashMap<Long,NodeWrapper>();
//		TemporalIndexNode startHere = new TemporalIndexNode(treeRoot.getSingleRelationship(circleTempoalRelTypes.inclue_time, Direction.OUTGOING).getEndNode());
//		Visit(retrunColl, startHere, startTime, endTime);
//		return retrunColl.values();
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
	
	/**
	 * 
	 * this code build the index nodes for each level
	 */
	//****Time
	private TemporalIndexNode createTimeNode(Calendar aroundThis){
		return createTimeNode(aroundThis.getTimeInMillis(), 0);
	}
	   private TemporalIndexNode createTimeNode(long startTime, long endTime){
			Node tempNode = treeRoot.getGraphDatabase().createNode();
			tempNode.setProperty(TIME_HIERARCHY, TIME_IN_DAY);
			//tempNode.setProperty(HIERARCHY_VALUE, theDay);
			tempNode.setProperty(START_TIME, startTime);
			tempNode.setProperty(END_TIME, endTime);
			tempNode.setProperty(SSN_TYPE, TEMPORAL_INDEX);
			return new TemporalIndexNode(tempNode);
		}
		
	//****Day
		private TemporalIndexNode createDayNode(Calendar aroundThis){
			return createDayNode(aroundThis.get(Calendar.YEAR), aroundThis.get(Calendar.MONTH), 
					aroundThis.get(Calendar.WEEK_OF_MONTH),aroundThis.get(Calendar.DAY_OF_WEEK));

		}
		
		private TemporalIndexNode createDayNode(int theYear, int theMonth, int theWeek, int theDay){
			Node tempNode = treeRoot.getGraphDatabase().createNode();
			tempNode.setProperty(TIME_HIERARCHY, DAY);
			tempNode.setProperty(HIERARCHY_VALUE, theDay);
			tempNode.setProperty(START_TIME, getDayMin(theYear, theMonth, theWeek, theDay));
			tempNode.setProperty(END_TIME, getDayMax(theYear, theMonth, theWeek, theDay));
			tempNode.setProperty(SSN_TYPE, TEMPORAL_INDEX);
			return new TemporalIndexNode(tempNode);
		}
			
		private long getDayMax(int theYear, int theMonth, int theWeek, int theDay){
			Calendar temp = Calendar.getInstance();
			temp.set(Calendar.YEAR, theYear);
			temp.set(Calendar.MONTH, theMonth);
			temp.set(Calendar.WEEK_OF_MONTH, theWeek);
			temp.set(Calendar.DAY_OF_WEEK, theDay);		
			temp.set(Calendar.HOUR_OF_DAY, temp.getActualMaximum(Calendar.HOUR_OF_DAY));
			temp.set(Calendar.MINUTE, temp.getActualMaximum(Calendar.MINUTE));
			temp.set(Calendar.SECOND, temp.getActualMaximum(Calendar.SECOND));
			temp.set(Calendar.MILLISECOND, temp.getActualMaximum(Calendar.MILLISECOND));
			return temp.getTimeInMillis();
		}
		
		private long getDayMin(int theYear, int theMonth, int theWeek, int theDay){
			Calendar temp = Calendar.getInstance();
			temp.set(Calendar.YEAR, theYear);
			temp.set(Calendar.MONTH, theMonth);
			temp.set(Calendar.WEEK_OF_MONTH, theWeek);
			temp.set(Calendar.DAY_OF_WEEK, theDay);
			temp.set(Calendar.HOUR_OF_DAY, temp.getActualMinimum(Calendar.HOUR_OF_DAY));
			temp.set(Calendar.MINUTE, temp.getActualMinimum(Calendar.MINUTE));
			temp.set(Calendar.SECOND, temp.getActualMinimum(Calendar.SECOND));
			temp.set(Calendar.MILLISECOND, temp.getActualMinimum(Calendar.MILLISECOND));
			return temp.getTimeInMillis();
		}
		
	
	//****Week
		private TemporalIndexNode createWeekNode(Calendar aroundThis){
			return createWeekNode(aroundThis.get(Calendar.YEAR), aroundThis.get(Calendar.MONTH), 
					aroundThis.get(Calendar.WEEK_OF_MONTH));
		}
		
		private TemporalIndexNode createWeekNode(int theYear, int theMonth, int theWeek){
			Node tempNode = treeRoot.getGraphDatabase().createNode();
			tempNode.setProperty(TIME_HIERARCHY, WEEK);
			tempNode.setProperty(HIERARCHY_VALUE, theWeek);
			tempNode.setProperty(START_TIME, getWeekMin(theYear, theMonth, theWeek));
			tempNode.setProperty(END_TIME, getWeekMax(theYear, theMonth, theWeek));
			tempNode.setProperty(SSN_TYPE, TEMPORAL_INDEX);
			return new TemporalIndexNode(tempNode);
		}
			
		private long getWeekMax(int theYear, int theMonth, int theWeek){
			Calendar temp = Calendar.getInstance();
			temp.set(Calendar.YEAR, theYear);
			temp.set(Calendar.MONTH, theMonth);
			if(theWeek == temp.getActualMaximum(Calendar.WEEK_OF_MONTH)){
				//for the last week
				temp.set(Calendar.DAY_OF_MONTH, temp.getActualMaximum(Calendar.DAY_OF_MONTH));
			} else{
				temp.set(Calendar.WEEK_OF_MONTH, theWeek);
				temp.set(Calendar.DAY_OF_WEEK, temp.getActualMaximum(Calendar.DAY_OF_WEEK));	
			}
			temp.set(Calendar.HOUR_OF_DAY, temp.getActualMaximum(Calendar.HOUR_OF_DAY));
			temp.set(Calendar.MINUTE, temp.getActualMaximum(Calendar.MINUTE));
			temp.set(Calendar.SECOND, temp.getActualMaximum(Calendar.SECOND));
			temp.set(Calendar.MILLISECOND, temp.getActualMaximum(Calendar.MILLISECOND));
			return temp.getTimeInMillis();
		}
		
		private long getWeekMin(int theYear, int theMonth, int theWeek){
			Calendar temp = Calendar.getInstance();
			temp.set(Calendar.YEAR, theYear);
			temp.set(Calendar.MONTH, theMonth);
			if(theWeek <= 1){
				//for the first week in the month
				temp.set(Calendar.DAY_OF_MONTH, temp.getActualMinimum(Calendar.DAY_OF_MONTH));
			}else{
				temp.set(Calendar.WEEK_OF_MONTH, theWeek);
				temp.set(Calendar.DAY_OF_WEEK, temp.getMinimalDaysInFirstWeek());
			}
			temp.set(Calendar.HOUR_OF_DAY, temp.getActualMinimum(Calendar.HOUR_OF_DAY));
			temp.set(Calendar.MINUTE, temp.getActualMinimum(Calendar.MINUTE));
			temp.set(Calendar.SECOND, temp.getActualMinimum(Calendar.SECOND));
			temp.set(Calendar.MILLISECOND, temp.getActualMinimum(Calendar.MILLISECOND));
			return temp.getTimeInMillis();
		}
	
	//****Month
	private TemporalIndexNode createMonthNode(Calendar aroundThis){
		return createMonthNode(aroundThis.get(Calendar.YEAR), aroundThis.get(Calendar.MONTH));
	}
		
	private TemporalIndexNode createMonthNode(int theYear, int theMonth){
		Node tempNode = treeRoot.getGraphDatabase().createNode();
		tempNode.setProperty(TIME_HIERARCHY, MANTH);
		tempNode.setProperty(HIERARCHY_VALUE, theMonth);
		tempNode.setProperty(START_TIME, getMonthMin(theYear, theMonth));
		tempNode.setProperty(END_TIME, getMonthMax(theYear, theMonth));
		tempNode.setProperty(SSN_TYPE, TEMPORAL_INDEX);
		return new TemporalIndexNode(tempNode);
	}
		
	private long getMonthMax(int theYear, int theMonth){
		Calendar temp = Calendar.getInstance();
		temp.set(Calendar.YEAR, theYear);
		temp.set(Calendar.MONTH, theMonth);
		temp.set(Calendar.DAY_OF_MONTH, temp.getActualMaximum(Calendar.DAY_OF_MONTH));
		temp.set(Calendar.HOUR_OF_DAY, temp.getActualMaximum(Calendar.HOUR_OF_DAY));
		temp.set(Calendar.MINUTE, temp.getActualMaximum(Calendar.MINUTE));
		temp.set(Calendar.SECOND, temp.getActualMaximum(Calendar.SECOND));
		temp.set(Calendar.MILLISECOND, temp.getActualMaximum(Calendar.MILLISECOND));
		return temp.getTimeInMillis();
	}
	
	private long getMonthMin(int theYear, int theMonth){
		Calendar temp = Calendar.getInstance();
		temp.set(Calendar.YEAR, theYear);
		temp.set(Calendar.MONTH, theMonth);
		temp.set(Calendar.DAY_OF_MONTH, temp.getActualMinimum(Calendar.DAY_OF_MONTH));
		temp.set(Calendar.HOUR_OF_DAY, temp.getActualMinimum(Calendar.HOUR_OF_DAY));
		temp.set(Calendar.MINUTE, temp.getActualMinimum(Calendar.MINUTE));
		temp.set(Calendar.SECOND, temp.getActualMinimum(Calendar.SECOND));
		temp.set(Calendar.MILLISECOND, temp.getActualMinimum(Calendar.MILLISECOND));
		return temp.getTimeInMillis();
	}
	
	//****Year
	private TemporalIndexNode createYearNode(Calendar aroundThis){
		return createYearNode(aroundThis.get(Calendar.YEAR));
	}
	
	private TemporalIndexNode createYearNode(int theYear){
		Node tempNode = treeRoot.getGraphDatabase().createNode();
		tempNode.setProperty(TIME_HIERARCHY, YEAR);
		tempNode.setProperty(HIERARCHY_VALUE, theYear);
		tempNode.setProperty(START_TIME, getYearMin(theYear));
		tempNode.setProperty(END_TIME, getYearMax(theYear));
		tempNode.setProperty(SSN_TYPE, TEMPORAL_INDEX);
		return new TemporalIndexNode(tempNode);
	}
	
	private long getYearMax(int theYear){
		Calendar temp = Calendar.getInstance();
		temp.set(Calendar.YEAR, theYear);
		temp.set(Calendar.MONTH, temp.getActualMaximum(Calendar.MONTH));
		temp.set(Calendar.DAY_OF_MONTH, temp.getActualMaximum(Calendar.DAY_OF_MONTH));
		temp.set(Calendar.HOUR_OF_DAY, temp.getActualMaximum(Calendar.HOUR_OF_DAY));
		temp.set(Calendar.MINUTE, temp.getActualMaximum(Calendar.MINUTE));
		temp.set(Calendar.SECOND, temp.getActualMaximum(Calendar.SECOND));
		temp.set(Calendar.MILLISECOND, temp.getActualMaximum(Calendar.MILLISECOND));
		return temp.getTimeInMillis();
	}
	
	private long getYearMin(int theYear){
		Calendar temp = Calendar.getInstance();
		temp.set(Calendar.YEAR, theYear);
		temp.set(Calendar.MONTH, temp.getActualMinimum(Calendar.MONTH));
		temp.set(Calendar.DAY_OF_MONTH, temp.getActualMinimum(Calendar.DAY_OF_MONTH));
		temp.set(Calendar.HOUR_OF_DAY, temp.getActualMinimum(Calendar.HOUR_OF_DAY));
		temp.set(Calendar.MINUTE, temp.getActualMinimum(Calendar.MINUTE));
		temp.set(Calendar.SECOND, temp.getActualMinimum(Calendar.SECOND));
		temp.set(Calendar.MILLISECOND, temp.getActualMinimum(Calendar.MILLISECOND));
		return temp.getTimeInMillis();
	}
}

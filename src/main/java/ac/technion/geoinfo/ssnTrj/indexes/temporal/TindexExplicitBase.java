package ac.technion.geoinfo.ssnTrj.indexes.temporal;

import java.util.Calendar;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import ac.technion.geoinfo.ssnTrj.domain.TimePatternRelation;

public abstract class TindexExplicitBase implements TemporalStatic {
	TemporalIndexNode treeRoot;
	
	public TindexExplicitBase(Node nodeWithIndex) throws Exception
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
	
	
	/**
	 * 
	 * this code build the index nodes for each level
	 */
	
			//****Time
			protected TemporalIndexNode createTimeNode(Calendar aroundThis){
			return createTimeNode(aroundThis.getTimeInMillis(), 0);
			}
	
			protected TemporalIndexNode createTimeNode(long startTime, long endTime){
				Node tempNode = treeRoot.getGraphDatabase().createNode();
				tempNode.setProperty(TIME_HIERARCHY, TIME_IN_DAY);
				//tempNode.setProperty(HIERARCHY_VALUE, theDay);
				tempNode.setProperty(START_TIME, startTime);
				tempNode.setProperty(END_TIME, endTime);
				tempNode.setProperty(SSN_TYPE, TEMPORAL_INDEX);
				return new TemporalIndexNode(tempNode);
			}
			
			//****Day
		    protected TemporalIndexNode createDayNode(Calendar aroundThis){
				return createDayNode(aroundThis.get(Calendar.YEAR), aroundThis.get(Calendar.MONTH), 
						aroundThis.get(Calendar.WEEK_OF_MONTH),aroundThis.get(Calendar.DAY_OF_WEEK));

			}
			
			protected TemporalIndexNode createDayNode(int theYear, int theMonth, int theWeek, int theDay){
				Node tempNode = treeRoot.getGraphDatabase().createNode();
				tempNode.setProperty(TIME_HIERARCHY, DAY);
				tempNode.setProperty(HIERARCHY_VALUE, theDay);
				tempNode.setProperty(START_TIME, getDayMin(theYear, theMonth, theWeek, theDay));
				tempNode.setProperty(END_TIME, getDayMax(theYear, theMonth, theWeek, theDay));
				tempNode.setProperty(SSN_TYPE, TEMPORAL_INDEX);
				return new TemporalIndexNode(tempNode);
			}
				
			protected long getDayMax(int theYear, int theMonth, int theWeek, int theDay){
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
			
			protected long getDayMin(int theYear, int theMonth, int theWeek, int theDay){
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
			protected TemporalIndexNode createWeekNode(Calendar aroundThis){
				return createWeekNode(aroundThis.get(Calendar.YEAR), aroundThis.get(Calendar.MONTH), 
						aroundThis.get(Calendar.WEEK_OF_MONTH));
			}
			
			protected TemporalIndexNode createWeekNode(int theYear, int theMonth, int theWeek){
				Node tempNode = treeRoot.getGraphDatabase().createNode();
				tempNode.setProperty(TIME_HIERARCHY, WEEK);
				tempNode.setProperty(HIERARCHY_VALUE, theWeek);
				tempNode.setProperty(START_TIME, getWeekMin(theYear, theMonth, theWeek));
				tempNode.setProperty(END_TIME, getWeekMax(theYear, theMonth, theWeek));
				tempNode.setProperty(SSN_TYPE, TEMPORAL_INDEX);
				return new TemporalIndexNode(tempNode);
			}
				
			protected long getWeekMax(int theYear, int theMonth, int theWeek){
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
			
			protected long getWeekMin(int theYear, int theMonth, int theWeek){
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
		protected TemporalIndexNode createMonthNode(Calendar aroundThis){
			return createMonthNode(aroundThis.get(Calendar.YEAR), aroundThis.get(Calendar.MONTH));
		}
			
		protected TemporalIndexNode createMonthNode(int theYear, int theMonth){
			Node tempNode = treeRoot.getGraphDatabase().createNode();
			tempNode.setProperty(TIME_HIERARCHY, MANTH);
			tempNode.setProperty(HIERARCHY_VALUE, theMonth);
			tempNode.setProperty(START_TIME, getMonthMin(theYear, theMonth));
			tempNode.setProperty(END_TIME, getMonthMax(theYear, theMonth));
			tempNode.setProperty(SSN_TYPE, TEMPORAL_INDEX);
			return new TemporalIndexNode(tempNode);
		}
			
		protected long getMonthMax(int theYear, int theMonth){
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
		
		protected long getMonthMin(int theYear, int theMonth){
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
		protected TemporalIndexNode createYearNode(Calendar aroundThis){
			return createYearNode(aroundThis.get(Calendar.YEAR));
		}
		
		protected TemporalIndexNode createYearNode(int theYear){
			Node tempNode = treeRoot.getGraphDatabase().createNode();
			tempNode.setProperty(TIME_HIERARCHY, YEAR);
			tempNode.setProperty(HIERARCHY_VALUE, theYear);
			tempNode.setProperty(START_TIME, getYearMin(theYear));
			tempNode.setProperty(END_TIME, getYearMax(theYear));
			tempNode.setProperty(SSN_TYPE, TEMPORAL_INDEX);
			return new TemporalIndexNode(tempNode);
		}
		
		protected long getYearMax(int theYear){
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
		
		protected long getYearMin(int theYear){
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

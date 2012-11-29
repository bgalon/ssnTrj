package ac.technion.geoinfo.ssnTrj.indexes.temporal;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import ac.technion.geoinfo.ssnTrj.domain.NodeWrapperImpl;

public class TemporalIndexNode extends NodeWrapperImpl implements TemporalStatic {

	public TemporalIndexNode(Node theUnderlaying) {
		super(theUnderlaying);
	}

	public long getStartTime(){
		return (Long)underlayingNode.getProperty(START_TIME);
	}
	
	public long getEndTime(){
		return (Long)underlayingNode.getProperty(END_TIME);
	}
	
	public TimeHierarchy getTimeHierarchy(){
		return TimeHierarchy.valueOf((String)underlayingNode.getProperty(TIME_HIERARCHY));
	}
	
	public int getHierarchyValue() {
		return (Integer)underlayingNode.getProperty(HIERARCHY_VALUE);
	}
	
	public TemporalIndexNode getNext(){
		Relationship next = underlayingNode.getSingleRelationship(circleTempoalRelTypes.next_time, Direction.OUTGOING);
		if (next == null) return null;
		return new TemporalIndexNode(next.getEndNode());
	}
	
	public TemporalIndexNode getInclude(){
		Relationship next = underlayingNode.getSingleRelationship(circleTempoalRelTypes.inclue_time, Direction.OUTGOING);
		if (next == null) return null;
		return new TemporalIndexNode(next.getEndNode());
	}
}

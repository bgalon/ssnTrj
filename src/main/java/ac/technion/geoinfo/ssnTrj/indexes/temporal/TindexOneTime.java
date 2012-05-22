//package ac.technion.geoinfo.ssnTrj.indexes.temporal;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.ObjectInput;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutput;
//import java.io.ObjectOutputStream;
//import java.util.Calendar;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.SortedMap;
//import java.util.TreeMap;
//
//import org.neo4j.graphdb.Direction;
//import org.neo4j.graphdb.Node;
//import org.neo4j.graphdb.Relationship;
//
//import ac.technion.geoinfo.ssnTrj.domain.NodeWrapper;
//import ac.technion.geoinfo.ssnTrj.domain.NodeWrapperImpl;
//import ac.technion.geoinfo.ssnTrj.domain.TimeInterval;
//import ac.technion.geoinfo.ssnTrj.domain.TimeIntervalImpl;
//import ac.technion.geoinfo.ssnTrj.domain.TimeIntervalIntersection;
//import ac.technion.geoinfo.ssnTrj.domain.TimePatternRelation;
//
//public class TindexOneTime implements TemopralIndex {
//
//	static final String START_PROPERTY = "start_time"; 
//	static final String END_PROPERTY = "end_time";
//	TemporalIndexNode treeRoot;
//	
//	public TindexOneTime(Node nodeWithIndex){
//		treeRoot = new TemporalIndexNode(nodeWithIndex);
//	}
//	
//	public void Add(NodeWrapper toInsert, long startTime, long endTime) throws Exception {
//		Add(toInsert, new TimeIntervalImpl(startTime, endTime));
//	}
//	
//	public void Add(NodeWrapper toInsert, TimeInterval addInter)	throws Exception {
//		Relationship underlayingRel = treeRoot.createRelationshipTo(toInsert, TimePatternRelation.tpIndex);
//		underlayingRel.setProperty(START_PROPERTY, addInter.GetStartTime());
//		underlayingRel.setProperty(END_PROPERTY, addInter.GetEndTime());
//	}
//	
//	public Collection<NodeWrapper> Search(long startTime, long endTime) throws Exception {
//		return Search(new TimeIntervalImpl(startTime, endTime));
//	}
//	
//	public Collection<NodeWrapper> Search(TimeInterval searchInter) throws Exception {
//		Map<Long,NodeWrapper> retrunColl = new HashMap<Long,NodeWrapper>();
//		Iterable<Relationship> rels = treeRoot.getRelationships(TimePatternRelation.tpIndex, Direction.OUTGOING);
//		for(Relationship tempRel:rels){
//			TimeInterval testMe = new TimeIntervalImpl((Long)tempRel.getProperty(START_PROPERTY), 
//					(Long)tempRel.getProperty(END_PROPERTY));
//			if(searchInter.intersect(testMe) == TimeIntervalIntersection.intersect){
//				NodeWrapper otherNode = new NodeWrapperImpl(tempRel.getOtherNode(treeRoot));
//				if(retrunColl.containsKey(otherNode.getId())){
//					retrunColl.get(otherNode.getId()).addLaed();
//				}else{
//					retrunColl.put(otherNode.getId(), otherNode);
//				}
//			}
//		}
//		return retrunColl.values();
//	}
//	
//}

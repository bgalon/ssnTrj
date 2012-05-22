package ac.technion.geoinfo.ssnTrj.indexes.temporal;

import static org.neo4j.index.lucene.ValueContext.numeric;
import static org.apache.lucene.search.NumericRangeQuery.newLongRange;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.index.lucene.QueryContext;

import ac.technion.geoinfo.ssnTrj.domain.NodeWrapper;
import ac.technion.geoinfo.ssnTrj.domain.NodeWrapperImpl;
import ac.technion.geoinfo.ssnTrj.domain.TimeInterval;
import ac.technion.geoinfo.ssnTrj.domain.TimeIntervalImpl;
import ac.technion.geoinfo.ssnTrj.domain.TimePatternRelation;

public class TindexLucene extends TindexRelList {
	
	static final String INDEX_NAME = "TimeRouteIndex";
	static final String TIME_KEY = "time_key";
	
	public TindexLucene(Node nodeWithIndex){
		super(nodeWithIndex);
	}

	@Override
	public void Add(NodeWrapper toInsert, long startTime, long endTime) throws Exception {
		Add(toInsert, new TimeIntervalImpl(startTime, endTime));
	}
	
	@Override 
	public void Add(NodeWrapper toInsert, TimeInterval addInter) throws Exception {
		Transaction tx = treeRoot.getGraphDatabase().beginTx(); 
		try{
			RelationshipIndex theIndex = treeRoot.getGraphDatabase().index().forRelationships(INDEX_NAME);
			Relationship rel;
			rel = theIndex.get(TIME_KEY, null, treeRoot, toInsert).getSingle();
			if (rel == null) rel = treeRoot.createRelationshipTo(toInsert, TimePatternRelation.tpIndex);
			
			List<TimeInterval> theList = readList(rel);
			theList.add(addInter);
			writeList(theList, rel);
			
			theIndex.add(rel, TIME_KEY, numeric(addInter.GetStartTime()));
			
			upDateProperties(toInsert, addInter);
			
			tx.success();
		}catch (Exception e) {
			tx.failure();
			throw e;
		}finally{
			tx.finish();
		}
			
//		TemporalIndex reverseIndex = new TindexLucene(toInsert);
//		reverseIndex.Add(treeRoot, addInter);
	}
	
	
	@Override
	public Collection<NodeWrapper> Search(long startTime, long endTime) throws Exception {
		return Search(new TimeIntervalImpl(startTime, endTime));
	}
	
	@Override
	public Collection<NodeWrapper> Search(TimeInterval searchInter) throws Exception {
		Map<Long,NodeWrapper> retrunColl = new HashMap<Long,NodeWrapper>();
		return Search(retrunColl, searchInter);
	}

	@Override
	public Collection<NodeWrapper> Search(Map<Long, NodeWrapper> addToThis, long startTime, long endTime) throws Exception {
		return Search(addToThis, new TimeIntervalImpl(startTime, endTime));
	}

	@Override
	public Collection<NodeWrapper> Search(Map<Long, NodeWrapper> addToThis, TimeInterval searchInter) throws Exception {
		Calendar tempCal = Calendar.getInstance();
		tempCal.setTimeInMillis(searchInter.GetStartTime());
		tempCal.set(Calendar.HOUR_OF_DAY, tempCal.getActualMinimum(Calendar.HOUR_OF_DAY));
		tempCal.set(Calendar.MINUTE, tempCal.getActualMinimum(Calendar.MINUTE));
		tempCal.set(Calendar.SECOND, tempCal.getActualMinimum(Calendar.SECOND));
		tempCal.set(Calendar.MILLISECOND, tempCal.getActualMinimum(Calendar.MILLISECOND));
		
		long startOnTree = tempCal.getTimeInMillis();
		RelationshipIndex theIndex = treeRoot.getGraphDatabase().index().forRelationships(INDEX_NAME);
		IndexHits<Relationship> rels = theIndex.query(TIME_KEY, 
				sort( rangeQuery( startOnTree, searchInter.GetEndTime() ), true ), treeRoot, null);
		
		
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
	
	private QueryContext rangeQuery( Long startTimestampOrNull, Long endTimestampOrNull )
    {
        long start = startTimestampOrNull != null ? startTimestampOrNull : -Long.MAX_VALUE;
        long end = endTimestampOrNull != null ? endTimestampOrNull : Long.MAX_VALUE;
        return new QueryContext( newLongRange( TIME_KEY, start, end, true, true ) );
    }
    
    private QueryContext sort( QueryContext query, boolean reversed )
    {
        return query.sort( new Sort( new SortField( TIME_KEY, SortField.LONG, reversed ) ) );
    }
}

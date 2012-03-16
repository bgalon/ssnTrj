package ac.technion.geoinfo.ssnTrj.temporalIndexes;


import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import ac.technion.geoinfo.ssnTrj.domain.NodeWrapper;
import ac.technion.geoinfo.ssnTrj.domain.NodeWrapperImpl;
import ac.technion.geoinfo.ssnTrj.domain.SocialRelation;
import ac.technion.geoinfo.ssnTrj.domain.TimePatternRelation;
import ac.technion.geoinfo.ssnTrj.indexes.temporal.TemopralIndex;
import ac.technion.geoinfo.ssnTrj.indexes.temporal.TindexCircleImpl;

public class CircleIndexTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		String dbPath = "d:\\indexTests\\";
		
		GraphDatabaseService gdb = new EmbeddedGraphDatabase(dbPath);
		
//		 //* build the database
//		Transaction tx = gdb.beginTx(); 
//		try{
//			NodeWrapper p = new NodeWrapperImpl(gdb.createNode());
//			gdb.getReferenceNode().createRelationshipTo(p, TimePatternRelation.tpIndex);
//			TemopralIndex tempInd = new TindexCircleImpl(p);
//			
//			Calendar cal = Calendar.getInstance();
//			for (int i = 1; i < 13140 ; i++){  // for time in the day
//				Date startTime = new Date(cal.getTimeInMillis());
//				Date endTime = new Date(startTime.getTime() + 1000 * 60 * 30);
//				NodeWrapper l = new NodeWrapperImpl(gdb.createNode());
//				l.setProperty("Start", startTime.toString());
//				l.setProperty("End", endTime.toString());
//				tempInd.Add(l, startTime.getTime(), endTime.getTime());
//				cal.add(Calendar.HOUR, 1);
//			}
//
//			tx.success();
//		}catch (Exception e) {
//			// TODO: handle exception
//			tx.failure();
//			throw e;
//				//e.printStackTrace();
//		}finally{
//			tx.finish();
//			
//		}
//		
		
		//Serch test
		
		TemopralIndex tmpInd = new TindexCircleImpl(gdb.getReferenceNode().getSingleRelationship(TimePatternRelation.tpIndex, Direction.OUTGOING).getEndNode());
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		Date startTime = cal.getTime();
		Date endTime = new Date(cal.getTimeInMillis() + 1000 * 60 * 60 * 3);
		System.out.println("Look for times from " + startTime + " to " + endTime);
		List<NodeWrapper> result = new LinkedList<NodeWrapper>(tmpInd.Search(startTime.getTime(), endTime.getTime()));
		System.out.println(result.size() + " nodes has been found");
		for(NodeWrapper temp:result)
		{
			System.out.println(temp + " " + temp.getProperty("Start") + " - " + temp.getProperty("End"));
		}
		
		
		gdb.shutdown();
	}

}

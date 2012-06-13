package ac.technion.geoinfo.ssnTrj.temporalIndexes;


import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import ac.technion.geoinfo.ssnTrj.domain.NodeWrapper;

import ac.technion.geoinfo.ssnTrj.domain.Static;
import ac.technion.geoinfo.ssnTrj.generator.BuildDBs;
import ac.technion.geoinfo.ssnTrj.indexes.temporal.TindexLucene;
import ac.technion.geoinfo.ssnTrj.indexes.temporal.TindexRelList;
import ac.technion.geoinfo.ssnTrj.indexes.temporal.TemporalIndex;
import ac.technion.geoinfo.ssnTrj.indexes.temporal.TindexCircleImpl;

public class CircleIndexTest implements Static{
	
	final static int NUM_OF_USERES = 1000;
	final static int NUM_OF_LOCATIONS = 100;
	
	//properties
	final static String USER_ID = "user_id";
	final static String LOCATION_ID = "location_id";
	
	//indexes
	final static String THE_INDEX ="main_index";
	final static String USERS_KEY ="users";
	final static String LOCATION_KEY ="locations";
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		BulidGDBs();
		
	}
	
	private static void BulidGDBs() throws Exception{
		String dbPathCircle = "d:\\indexTests\\CircleN\\";
		String dbPathRel = "d:\\indexTests\\RelN\\";
		String dbPathIndex = "d:\\indexTests\\IndexN\\";
		
		@SuppressWarnings("unchecked")
		Class<TemporalIndex>[] IndexCalsses = new Class[]{TindexCircleImpl.class, TindexRelList.class, TindexLucene.class};
		
		Calendar startCal = Calendar.getInstance();
		startCal.set(Calendar.HOUR_OF_DAY, startCal.getActualMinimum(Calendar.HOUR_OF_DAY));
		startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
		startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
		startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));
		
		Random rangen = new Random();
		
		BuildDBs dbBuilder = new BuildDBs(new String[]{dbPathCircle, dbPathRel, dbPathIndex}, IndexCalsses, 1000, 10000);
		dbBuilder.BuildUsersNLocations();
		int iterCount = 10;
		long startTime = startCal.getTimeInMillis();
		for(int i = 0; i < 500; i=i+100){
			long endTime  = startTime + 1000 * 60 * 60 * iterCount;
			int locStart = rangen.nextInt(10000 - iterCount);
			dbBuilder.BuildTimesForUsers(i, i + 99, locStart , locStart + iterCount , startTime, endTime);
			
			
			System.out.println("done users " + i + "-" + (i + 99) + ", Time: " + startTime + "-" + endTime + "(" + 
					((endTime-startTime)/(1000 * 60 * 60))  + "), locations: " + locStart + "-" + (locStart + iterCount));
			
			iterCount = iterCount * 10;
			startTime = endTime + 1;
		}
		
		iterCount = 10;
		for(int i = 500; i < 1000; i=i+100){
			long endTime  = startTime + 1000 * 60 * 60 * iterCount;
			int locStart = rangen.nextInt(10000 - iterCount/10);
			dbBuilder.BuildTimesForUsers(i, i + 99, locStart , locStart + iterCount/10 , startTime, endTime);
			
			System.out.println("done users " + i + "-" + (i + 99) + ", Time: " + startTime + "-" + endTime + "(" + 
					((endTime-startTime)/(1000 * 60 * 60))  + "), locations: " + locStart + "-" + (locStart + iterCount));
			
			iterCount = iterCount * 10;
			startTime = endTime + 1;
		}
				
		dbBuilder.PrintDBreport();
		dbBuilder.Dispose();
	}
	
	static private void Ex1(){
		String dbPathCircle = "d:\\indexTests\\CircleN\\";
		String dbPathRel = "d:\\indexTests\\RelN\\";
		String dbPathIndex = "d:\\indexTests\\IndexN\\";
		
		@SuppressWarnings("unchecked")
		Class<TemporalIndex>[] IndexCalsses = new Class[]{TindexCircleImpl.class, TindexRelList.class, TindexLucene.class};
		
		long startTime = 1332367200000L;
		
				
				
	}
	
	static private long[] SearchNodes(String dbsPath, Class<TemporalIndex> timeIndexClasses, int[] startNodesId, 
			String indexKey, long startTime, long endTime, int numOfRuns, boolean printResult) throws Exception{
		
		GraphDatabaseService gdb = new EmbeddedGraphDatabase(dbsPath);
		long[] runTimes = new long[numOfRuns];
		//get start nodes
		for (int n = 0; n < numOfRuns; n ++){
			long runtime = System.currentTimeMillis();
			List<TemporalIndex> startNodes = new ArrayList<TemporalIndex>();
			Index<Node> theIndex = gdb.index().forNodes(THE_INDEX);
			Constructor<TemporalIndex> theConstructor = timeIndexClasses.getConstructor(new Class[]{Node.class});
			for(int i = 0; i < startNodesId.length; i++){
				IndexHits<Node> result = theIndex.get(indexKey, startNodesId[i]);
				while(result.hasNext()){
					startNodes.add(theConstructor.newInstance(new Object[]{result.next()}));
				}
			}
			Map<Long, NodeWrapper> finalResult = new HashMap<Long, NodeWrapper>();
			for(int i = 0; i < startNodesId.length; i++){
				startNodes.get(i).Search(finalResult, startTime, endTime);
			}
			runTimes[n] = System.currentTimeMillis() - runtime;
			
			if(printResult){
				for(NodeWrapper temp:finalResult.values())
				{
					int id = -1;
					if(temp.hasProperty(USER_ID)) id = (Integer)temp.getProperty(USER_ID);
					if(temp.hasProperty(LOCATION_ID)) id = (Integer)temp.getProperty(LOCATION_ID);
					if(id < 0) return null;
					System.out.println("result for run num " + n);
					System.out.println(id + " wiht " + temp.getLeads() + " leads (" + temp + ")");
				}
			}
		}		
		gdb.shutdown();

		return runTimes;
	}
	
	
//	private static void OldbulidGDBs(String[] dbsPaths, Class<TemporalIndex>[] timeIndexClasses) throws Exception{
//		Random ranGen = new Random();
//		NodeWrapper[][] users;
//		NodeWrapper[][] locations;
//		
//		@SuppressWarnings("unchecked")
//		Index<Node>[] indexes = new Index[dbsPaths.length];
//		@SuppressWarnings("unchecked")
//		Constructor<TemporalIndex>[] constructors= new Constructor[dbsPaths.length];
//		for(int i = 0; i < dbsPaths.length ;i++){
//			constructors[i] = timeIndexClasses[i].getConstructor(new Class[] {Node.class});
//		}
//		
//		
//		
//		//open dbs and transactions
//		GraphDatabaseService[] gdbs = new GraphDatabaseService[dbsPaths.length];
//		Transaction[] txs = new Transaction[gdbs.length];
//		for(int i = 0; i < dbsPaths.length ;i++){
//			gdbs[i] = new EmbeddedGraphDatabase(dbsPaths[i]);
//			txs[i] = gdbs[i].beginTx();
//		}
//		try{
//			//build indexs
//			Node[] userRef  = new Node[gdbs.length];
//			for(int i = 0; i < gdbs.length ;i++){
//				indexes[i] = gdbs[i].index().forNodes(THE_INDEX);
//				Node tempUserRefNode = gdbs[i].createNode();
//				userRef[i] = (gdbs[i].getReferenceNode().createRelationshipTo(tempUserRefNode, TestTIndexRel.user_ref)).getEndNode();
//				
//			}
//			
//			//build users
//			users = new NodeWrapperImpl[gdbs.length][NUM_OF_USERES];
//			
//			for (int i = 0; i < NUM_OF_USERES; i++){
//				for (int j = 0; j < gdbs.length ; j++){
//					NodeWrapper tempNode = new NodeWrapperImpl(gdbs[j].createNode());
//					users[j][i] = tempNode;
//					tempNode.setProperty(SSN_TYPE, USER);
//					tempNode.setProperty(USER_ID, i);
//					indexes[j].add(tempNode, USERS_KEY, i);
//					
//					userRef[j].createRelationshipTo(tempNode, TestTIndexRel.user_ref);
//				}
//			}
//			
//			//build location
//			locations = new NodeWrapperImpl[gdbs.length][NUM_OF_LOCATIONS];
//			for (int i = 0; i < NUM_OF_LOCATIONS; i++){
//				for (int j = 0; j < gdbs.length ; j++){
//					NodeWrapper tempNode = new NodeWrapperImpl(gdbs[j].createNode());
//					locations[j][i] = tempNode;
//					tempNode.setProperty(SSN_TYPE, BULIDING);
//					tempNode.setProperty(LOCATION_ID, i);
//					indexes[j].add(tempNode, LOCATION_KEY, i);
//					
//				}
//			}
//			
//			//build times for users
//			
//			Calendar startCal = Calendar.getInstance();
//			startCal.set(Calendar.HOUR_OF_DAY, startCal.getActualMinimum(Calendar.HOUR_OF_DAY));
//			startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
//			startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
//			startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));
//			Calendar endCal = Calendar.getInstance();
//			endCal.setTimeInMillis(startCal.getTimeInMillis());
//			endCal.add(Calendar.MONTH, 1); //****data for one month
//			
//			Calendar tempCal = Calendar.getInstance();
//			
//			for (int i = 0; i < NUM_OF_USERES; i++){
//				//Home and work pattern  
//				int dayCounter = 0;
//				tempCal.setTimeInMillis(startCal.getTimeInMillis());
//				tempCal.set(Calendar.DAY_OF_YEAR, startCal.get(Calendar.DAY_OF_YEAR) + dayCounter);
//				
//				int randomHomeLoc = ranGen.nextInt(NUM_OF_LOCATIONS);
//				int randomWorkLoc = ranGen.nextInt(NUM_OF_LOCATIONS);
//				while(tempCal.getTimeInMillis() < endCal.getTimeInMillis()){
//					long startTime = tempCal.getTimeInMillis();
//					tempCal.add(Calendar.HOUR, 7);
//					long endTime = tempCal.getTimeInMillis();
//					tempCal.add(Calendar.HOUR, 1);
//					long startWorkTime = tempCal.getTimeInMillis();
//					tempCal.add(Calendar.HOUR, 8);
//					long endWorkTime = tempCal.getTimeInMillis();
//					
//					tempCal.add(Calendar.HOUR, 1);
//					long startRandomTime = tempCal.getTimeInMillis();
//					tempCal.add(Calendar.HOUR, 5);
//					long endRandomTime = tempCal.getTimeInMillis();
//					
//					int randomLoc = ranGen.nextInt(NUM_OF_LOCATIONS);
//					for (int j = 0; j < gdbs.length ; j++){
//						NodeWrapper tempUser = users[j][i];
//						NodeWrapper tempHomeLoc = locations[j][randomHomeLoc];
//						TemporalIndex userIndex = constructors[j].newInstance(new Object[]{tempUser});
//						TemporalIndex locationIndex = constructors[j].newInstance(new Object[]{tempHomeLoc});
//						
//						userIndex.Add(tempHomeLoc, startTime, endTime);
//						locationIndex.Add(tempUser, startTime, endTime);
//						
//						if(tempCal.get(Calendar.DAY_OF_WEEK) > Calendar.SUNDAY && tempCal.get(Calendar.DAY_OF_WEEK) < Calendar.SATURDAY){
//							NodeWrapper tempWorkLoc = locations[j][randomWorkLoc];
//							locationIndex = constructors[j].newInstance(new Object[]{tempWorkLoc});
//							
//							userIndex.Add(tempWorkLoc, startWorkTime, endWorkTime);
//							locationIndex.Add(tempUser, startWorkTime, endWorkTime);
//						}
//						
//						NodeWrapper tempRandomLoc = locations[j][randomLoc];
//						locationIndex = constructors[j].newInstance(new Object[]{tempRandomLoc});
//						
//						userIndex.Add(tempRandomLoc, startRandomTime, endRandomTime);
//						locationIndex.Add(tempUser, startRandomTime, endRandomTime);
//					}				
//					System.out.println(i + "--" + new Date(startTime) + ":" + new Date(endTime) + "->" + randomHomeLoc);
//					System.out.println(i + "--" + new Date(startWorkTime) + ":" + new Date(endWorkTime) + "->" + randomWorkLoc);
//					System.out.println(i + "--" + new Date(startRandomTime) + ":" + new Date(endRandomTime) + "->" + randomLoc);
//					
//					dayCounter++;
//					tempCal.setTimeInMillis(startCal.getTimeInMillis());
//					tempCal.add(Calendar.DAY_OF_YEAR, dayCounter);
//				}	
//			}
//			
//			for(int i = 0; i < txs.length ;i++){
//				txs[i].success();
//			}
//			
//		}catch(Exception e){
//			for(int i = 0; i < txs.length ;i++){
//				txs[i].failure();
//			}
//			throw e;
//		}finally{
//			for(int i = 0; i < txs.length ;i++){
//				txs[i].finish();
//			}
//		}
//		
//		
//		
//		//print final report
//		TemporalIndex tempIndex;
//		for (int i = 0; i < NUM_OF_USERES; i++){
//			for (int j = 0; j < gdbs.length ; j++){
//				tempIndex = constructors[j].newInstance(new Object[]{users[j][i]});
//				
//				System.out.println("user: " + i + " db: " + j + " number of nodes: " + tempIndex.NumberOfLeaves()
//						+ " strat at: " + new Date(tempIndex.getStartTime()) + "(" + tempIndex.getStartTime() + ")"
//						+ " end time: " + new Date(tempIndex.getEndTime()) + "(" + tempIndex.getEndTime() + ")");
//			}
//		}
//		
//		for (int i = 0; i < NUM_OF_LOCATIONS; i++){
//			for (int j = 0; j < gdbs.length ; j++){
//				tempIndex = constructors[j].newInstance(new Object[]{locations[j][i]});
//				
//				System.out.println("location: " + i + " db: " + j + " number of nodes: " + tempIndex.NumberOfLeaves()
//						+ " strat at: " + new Date(tempIndex.getStartTime()) + "(" + tempIndex.getStartTime() + ")"
//						+ " end time: " + new Date(tempIndex.getEndTime()) + "(" + tempIndex.getEndTime() + ")");
//			}
//		}
//		
//		
//	
//		
//		//close gdbs
//		for(int i = 0; i < gdbs.length ;i++){
//			gdbs[i].shutdown();
//		}
//	}
	
	
	
//	static public void JastTest(){
//		String dbPathCircle = "d:\\indexTests\\Circle\\";
//		String dbPathRel = "d:\\indexTests\\Rel\\";
//		
//		GraphDatabaseService gdbCircle = new EmbeddedGraphDatabase(dbPathCircle);
//		GraphDatabaseService gdbRel = new EmbeddedGraphDatabase(dbPathRel);
		
		 //* build the database
//		Transaction tx = gdbCircle.beginTx(); 
//		Transaction tx1 = gdbRel.beginTx();
//		try{
//			NodeWrapper p = new NodeWrapperImpl(gdbCircle.createNode());
//			NodeWrapper p1 = new NodeWrapperImpl(gdbRel.createNode());
//			
//			gdbCircle.getReferenceNode().createRelationshipTo(p, SocialRelation.Family);
//			gdbRel.getReferenceNode().createRelationshipTo(p1, SocialRelation.Family);
//			
//			TemopralIndex tempInd = new TindexCircleImpl(p);
//			TemopralIndex tempInd1 = new TindexRelList(p1);
//			
//			Calendar cal = Calendar.getInstance();
//			int toSame = 0;
//			int lastSame = 0;
//			NodeWrapper l = new NodeWrapperImpl(gdbCircle.createNode());
//			NodeWrapper l1 = new NodeWrapperImpl(gdbRel.createNode());
//			for (int i = 1; i < 13140 ; i++){  // for time in the day
//				Date startTime = new Date(cal.getTimeInMillis());
//				Date endTime = new Date(startTime.getTime() + 1000 * 60 * 30);
//				if(toSame > lastSame + 100){
//					l = new NodeWrapperImpl(gdbCircle.createNode());
//					l1 = new NodeWrapperImpl(gdbRel.createNode());
//					lastSame = toSame;
//				}
//				toSame++;
//				
//				l.setProperty("Start", startTime.toString());
//				l.setProperty("End", endTime.toString());
//				tempInd.Add(l, startTime.getTime(), endTime.getTime());
//				
//				l1.setProperty("Start", startTime.toString());
//				l1.setProperty("End", endTime.toString());
//				tempInd1.Add(l1, startTime.getTime(), endTime.getTime());
//				
//				
//				cal.add(Calendar.HOUR, 1);
//			}
//
//			tx.success();
//			tx1.success();
//		}catch (Exception e) {
//			// TODO: handle exception
//			tx.failure();
//			tx1.failure();
//			throw e;
//				//e.printStackTrace();
//		}finally{
//			tx.finish();
//			tx1.finish();
//		}
		
		
		//Serch test
		
//		TemopralIndex tmpInd = new TindexCircleImpl(gdbCircle.getReferenceNode().getSingleRelationship(SocialRelation.Family, Direction.OUTGOING).getEndNode());
//		TemopralIndex tmpInd1 = new TindexRelList(gdbRel.getReferenceNode().getSingleRelationship(SocialRelation.Family, Direction.OUTGOING).getEndNode());
//		Calendar cal = Calendar.getInstance();
//		Date startTime = cal.getTime();
//		Date endTime = new Date(cal.getTimeInMillis() + 1000 * 60 * 60 * 500);
//		System.out.println("Look for times from " + startTime + " to " + endTime);
//		long runingTime = System.currentTimeMillis();
//		List<NodeWrapper> result = new ArrayList<NodeWrapper>(tmpInd.Search(startTime.getTime(), endTime.getTime()));
//		runingTime = System.currentTimeMillis() - runingTime;
//		System.out.println(result.size() + " nodes has been found run time = " + runingTime + " miliSec" );
//		for(NodeWrapper temp:result)
//		{
//			System.out.println(temp + " " + temp.getProperty("Start") + " - " + temp.getProperty("End") + " * " + temp.getLeads());
//		}
//		
//		
//		runingTime = System.currentTimeMillis();
////		result = new LinkedList<NodeWrapper>(tmpInd1.Search(startTime.getTime(), endTime.getTime()));
//		result = new ArrayList<NodeWrapper>(tmpInd1.Search(startTime.getTime(), endTime.getTime()));
//		runingTime = System.currentTimeMillis() - runingTime;
//		System.out.println(result.size() + " nodes has been found run time = " + runingTime + " miliSec" );
//
//		for(NodeWrapper temp:result)
//		{
//			System.out.println(temp + " " + temp.getProperty("Start") + " - " + temp.getProperty("End") + " * " + temp.getLeads());
//		}
//		
//		gdbCircle.shutdown();
//		gdbRel.shutdown();		
//	}
}

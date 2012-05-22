package ac.technion.geoinfo.ssnTrj.apps;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
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
import ac.technion.geoinfo.ssnTrj.indexes.temporal.TemporalIndex;
import ac.technion.geoinfo.ssnTrj.indexes.temporal.TindexCircleImpl;
import ac.technion.geoinfo.ssnTrj.indexes.temporal.TindexLucene;
import ac.technion.geoinfo.ssnTrj.indexes.temporal.TindexRelList;
import ac.technion.geoinfo.ssnTrj.indexes.temporal.TindexRelTree;

public class TempoIndEx1 {
	
	public static void main(String[] args) throws Exception{
//		Ex1();
//		System.out.println("Done Ex1");
//		Ex2();
//		System.out.println("Done Ex2");
//		Ex3();
//		System.out.println("Done Ex3");
		
//		Ex4();
//		System.out.println("Done Ex4");
		
//		Ex5();
//		System.out.println("Done Ex5");
//		
		Ex6();
		System.out.println("Done Ex6");
		
	}
	
	
	
	static private void Ex1() throws Exception{
		String dbPathCircle = "D:\\indexTests\\indexTests1\\CircleN\\";
		String dbPathRel = "D:\\indexTests\\indexTests1\\RelN\\";
		String dbPathIndex = "D:\\indexTests\\indexTests1\\IndexN\\";
		
		@SuppressWarnings("unchecked")
		Class<TemporalIndex>[] IndexCalsses = new Class[]{TindexCircleImpl.class, TindexRelList.class, TindexLucene.class};
		
		Random ranGen = new Random();
		int u1 = ranGen.nextInt(10);
		int u2 = ranGen.nextInt(10);
		int u3 = ranGen.nextInt(10);
		
		long startTime = 0;  //ranGen.nextInt(9) * (long)(1000 * 60 * 60) - 1;
		long endTime = 35;//startTime + (long)(1000 * 60 * 60) + 10;
		
		List<long[]> resultListC = new ArrayList<long[]>();
		List<long[]> resultListR = new ArrayList<long[]>();
		List<long[]> resultListI = new ArrayList<long[]>();
		
		//run for the first 10
		System.out.println("result for users: " + u1  + "," + u2 + "," + u3 +" from: "+ startTime + "-" + endTime);
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		//for 10-20
		u1 = 10 + ranGen.nextInt(10);
		u2 = 10 + ranGen.nextInt(10);
		u3 = 10 + ranGen.nextInt(10);

		startTime = 3600000L + ranGen.nextInt(9) * (long)(1000 * 60 * 60) - 1;
		endTime = startTime + (long)(1000 * 60 * 60) + 10;
		
		System.out.println("result for users: " + u1  + "," + u2 + "," + u3 +" from: "+ startTime + "-" + endTime);
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		//for 20-30
		u1 = 20 + ranGen.nextInt(10);
		u2 = 20 + ranGen.nextInt(10);
		u3 = 20 + ranGen.nextInt(10);

		startTime = 39600002L + ranGen.nextInt(99) * (long)(1000 * 60 * 60) - 1;
		endTime = startTime + (long)(1000 * 60 * 60) + 10;
		
		System.out.println("result for users: " + u1  + "," + u2 + "," + u3 +" from: "+ startTime + "-" + endTime);
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		//for 30-40
		u1 = 30 + ranGen.nextInt(10);
		u2 = 30 + ranGen.nextInt(10);
		u3 = 30 + ranGen.nextInt(10);

		startTime = 399600003L + ranGen.nextInt(999) * (long)(1000 * 60 * 60) - 1;
		endTime = startTime + (long)(1000 * 60 * 60) + 10;
		
		System.out.println("result for users: " + u1  + "," + u2 + "," + u3 +" from: "+ startTime + "-" + endTime);
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		//for 40-50
		u1 = 40 + ranGen.nextInt(10);
		u2 = 40 + ranGen.nextInt(10);
		u3 = 40 + ranGen.nextInt(10);

		startTime = 3999600004L + ranGen.nextInt(9999) * (long)(1000 * 60 * 60) - 1;
		endTime = startTime + (long)(1000 * 60 * 60) + 10;
		
		System.out.println("result for users: " + u1  + "," + u2 + "," + u3 +" from: "+ startTime + "-" + endTime);
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		System.out.println("result for Circulr index");
		PrintList(resultListC);
		System.out.println("result for Rel index");
		PrintList(resultListR);
		System.out.println("result for Index index");
		PrintList(resultListI);
		
	}
	
	static private void Ex2() throws Exception{
		String dbPathCircle = "D:\\indexTests\\indexTests1\\CircleN\\";
		String dbPathRel = "D:\\indexTests\\indexTests1\\RelN\\";
		String dbPathIndex = "D:\\indexTests\\indexTests1\\IndexN\\";
		
		@SuppressWarnings("unchecked")
		Class<TemporalIndex>[] IndexCalsses = new Class[]{TindexCircleImpl.class, TindexRelList.class, TindexLucene.class};
		
		Random ranGen = new Random();
		int u1 = 50 + ranGen.nextInt(10);
		int u2 = 50 + ranGen.nextInt(10);
		int u3 = 50 + ranGen.nextInt(10);
		
		long startTime = 39999600005L ;  //ranGen.nextInt(9) * (long)(1000 * 60 * 60) - 1;
		long endTime = startTime + 35;//startTime + (long)(1000 * 60 * 60) + 10;
		
		List<long[]> resultListC = new ArrayList<long[]>();
		List<long[]> resultListR = new ArrayList<long[]>();
		List<long[]> resultListI = new ArrayList<long[]>();
		
		//run for the first 10
		System.out.println("result for users: " + u1  + "," + u2 + "," + u3 +" from: "+ startTime + "-" + endTime);
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		//for 60-70
		u1 = 60 + ranGen.nextInt(10);
		u2 = 60 + ranGen.nextInt(10);
		u3 = 60 + ranGen.nextInt(10);

		startTime = 39999600005L + ranGen.nextInt(9) * (long)(1000 * 60 * 60) - 1;
		endTime = startTime + (long)(1000 * 60 * 60) + 10;
		
		System.out.println("result for users: " + u1  + "," + u2 + "," + u3 +" from: "+ startTime + "-" + endTime);
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		//for 70-80
		u1 = 70 + ranGen.nextInt(10);
		u2 = 70 + ranGen.nextInt(10);
		u3 = 70 + ranGen.nextInt(10);

		startTime = 40039200007L + ranGen.nextInt(99) * (long)(1000 * 60 * 60) - 1;
		endTime = startTime + (long)(1000 * 60 * 60) + 10;
		
		System.out.println("result for users: " + u1  + "," + u2 + "," + u3 +" from: "+ startTime + "-" + endTime);
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		//for 80-90
		u1 = 80 + ranGen.nextInt(10);
		u2 = 80 + ranGen.nextInt(10);
		u3 = 80 + ranGen.nextInt(10);

		startTime = 40399200008L + ranGen.nextInt(999) * (long)(1000 * 60 * 60) - 1;
		endTime = startTime + (long)(1000 * 60 * 60) + 10;
		
		System.out.println("result for users: " + u1  + "," + u2 + "," + u3 +" from: "+ startTime + "-" + endTime);
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		//for 90-100
		u1 = 90 + ranGen.nextInt(10);
		u2 = 90 + ranGen.nextInt(10);
		u3 = 90 + ranGen.nextInt(10);

		startTime = 43999200009L + ranGen.nextInt(9999) * (long)(1000 * 60 * 60) - 1;
		endTime = startTime + (long)(1000 * 60 * 60) + 10;
		
		System.out.println("result for users: " + u1  + "," + u2 + "," + u3 +" from: "+ startTime + "-" + endTime);
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		System.out.println("result for Circulr index");
		PrintList(resultListC);
		System.out.println("result for Rel index");
		PrintList(resultListR);
		System.out.println("result for Index index");
		PrintList(resultListI);
		
	}
	
	
	static private void Ex3() throws Exception{
		String dbPathCircle = "D:\\indexTests\\indexTests1\\CircleN\\";
		String dbPathRel = "D:\\indexTests\\indexTests1\\RelN\\";
		String dbPathIndex = "D:\\indexTests\\indexTests1\\IndexN\\";
		
		@SuppressWarnings("unchecked")
		Class<TemporalIndex>[] IndexCalsses = new Class[]{TindexCircleImpl.class, TindexRelList.class, TindexLucene.class};
		
		Random ranGen = new Random();
		int u1 = ranGen.nextInt(10);
		int u2 = ranGen.nextInt(10);
		int u3 = ranGen.nextInt(10);
		
		long startTime = 0;  //ranGen.nextInt(9) * (long)(1000 * 60 * 60) - 1;
		long endTime = 3600000;//startTime + (long)(1000 * 60 * 60) + 10;
		
		List<long[]> resultListC = new ArrayList<long[]>();
		List<long[]> resultListR = new ArrayList<long[]>();
		List<long[]> resultListI = new ArrayList<long[]>();
		
		//run for the first 10
		System.out.println("result for users: " + u1  + "," + u2 + "," + u3 +" from: "+ startTime + "-" + endTime);
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		
		//for 10-20
		u1 = 10 + ranGen.nextInt(10);
		u2 = 10 + ranGen.nextInt(10);
		u3 = 10 + ranGen.nextInt(10);

		startTime = 3600001L;
		endTime = 39600001L;
		
		System.out.println("result for users: " + u1  + "," + u2 + "," + u3 +" from: "+ startTime + "-" + endTime);
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		
		//for 20-30
		u1 = 20 + ranGen.nextInt(10);
		u2 = 20 + ranGen.nextInt(10);
		u3 = 20 + ranGen.nextInt(10);

		startTime = 39600002L;
		endTime = 399600002L;
		
		System.out.println("result for users: " + u1  + "," + u2 + "," + u3 +" from: "+ startTime + "-" + endTime);
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		
		//for 30-40
		u1 = 30 + ranGen.nextInt(10);
		u2 = 30 + ranGen.nextInt(10);
		u3 = 30 + ranGen.nextInt(10);

		startTime = 399600003L;
		endTime = 3999600003L;
		
		System.out.println("result for users: " + u1  + "," + u2 + "," + u3 +" from: "+ startTime + "-" + endTime);
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		
		//for 40-50
		u1 = 40 + ranGen.nextInt(10);
		u2 = 40 + ranGen.nextInt(10);
		u3 = 40 + ranGen.nextInt(10);

		startTime = 3999600004L;
		endTime = 39999600004L;
		
		System.out.println("result for users: " + u1  + "," + u2 + "," + u3 +" from: "+ startTime + "-" + endTime);
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[2], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		
		System.out.println("result for Circulr index");
		PrintList(resultListC);
		System.out.println("result for Rel index");
		PrintList(resultListR);
		System.out.println("result for Index index");
		PrintList(resultListI);
		
	}
	
	static private void Ex4() throws Exception{
		
		List<long[]> resultListC = new ArrayList<long[]>();
		List<long[]> resultListR = new ArrayList<long[]>();
		List<long[]> resultListRt = new ArrayList<long[]>();
		List<long[]> resultListI = new ArrayList<long[]>();

//		long startTime = 3999600004L;
//		long endTime = 39999600004L/10;
		
//		for(int i=1; i<=10 ;i++)
			runOne(396000002L + (1000*60*60*50), 396000002L + (1000*60*60*53), resultListC, resultListR, resultListRt, resultListI);
		
		
		System.out.println("result for Circulr index");
		PrintList(resultListC);
		System.out.println("result for RelList index");
		PrintList(resultListR);
		System.out.println("result for RelTree index");
		PrintList(resultListRt);
		System.out.println("result for Index index");
		PrintList(resultListI);
		
	}
	
static private void Ex6() throws Exception{
		
		List<long[]> resultListC = new ArrayList<long[]>();
		List<long[]> resultListR = new ArrayList<long[]>();
		List<long[]> resultListRt = new ArrayList<long[]>();
		List<long[]> resultListI = new ArrayList<long[]>();

		
		long startTime = 1000000000L;
		for(int i=0; i<100 ; i = i +10){
			//long endTime = startTime + (long)(60*60*1000*i*100);
			runOne6(startTime, startTime + (long)(60*60*1000*2), resultListC, resultListR, resultListRt, resultListI, i);
			startTime = startTime + (60L*60L*1000L*1000L*(long)(i/10 +1));
		}
		
		
		System.out.println("result for Circulr index");
		PrintList(resultListC);
		System.out.println("result for RelList index");
		PrintList(resultListR);
		System.out.println("result for RelTree index");
		PrintList(resultListRt);
		System.out.println("result for Index index");
		PrintList(resultListI);
		
	}
	
	static private void runOne6(long startTime, long endTime, List<long[]> resultListC, List<long[]> resultListR, List<long[]> resultListRt, List<long[]> resultListI, int startFrom) throws Exception{
		String dbPathCircle = "D:\\indexTests\\indexTests6\\CircleN\\";
		String dbPathRel = "D:\\indexTests\\indexTests6\\RelN\\";
		String dbPathRelTree = "D:\\indexTests\\indexTests6\\RelTreeN\\";
		String dbPathIndex = "D:\\indexTests\\indexTests6\\IndexN\\";
		
		@SuppressWarnings("unchecked")
		Class<TemporalIndex>[] IndexCalsses = new Class[]{TindexCircleImpl.class, TindexRelList.class, TindexRelTree.class ,TindexLucene.class};
		
		Random ranGen = new Random();
		
//		int u1 = 40 + ranGen.nextInt(10);
//		int u2 = 40 + ranGen.nextInt(10);
//		int u3 = 40 + ranGen.nextInt(10);
		int u1 = startFrom + ranGen.nextInt(10);
		int u2 = startFrom + ranGen.nextInt(10);
		int u3 = startFrom + ranGen.nextInt(10);
		
		System.out.println("result for users: " + u1  + "," + u2 + "," + u3 +" from: "+ startTime + "-" + endTime + 
				"(" + ((endTime - startTime)/(60*60*1000)) + ")");
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListRt.add(SearchNodes(dbPathRelTree, IndexCalsses[2], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[3], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListRt.add(SearchNodes(dbPathRelTree, IndexCalsses[2], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[3], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListRt.add(SearchNodes(dbPathRelTree, IndexCalsses[2], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[3], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
	}
	
	
	static private void Ex5() throws Exception{
		
		List<long[]> resultListC = new ArrayList<long[]>();
		List<long[]> resultListR = new ArrayList<long[]>();
		List<long[]> resultListI = new ArrayList<long[]>();

//		long startTime = 3999600004L;
//		long endTime = 39999600004L/10;
		
		runOne5(36000001L+(1000*60*60*2), 36000001L + (1000*60*60*5), resultListC, resultListR ,resultListI, 100);
			
//		for(int i=1; i<=10 ;i++)
//			runOne5(396000002L, 396000002L + (3996000002L/10)*i, resultListC, resultListR,  resultListI, 200);
		
		
		System.out.println("result for Circulr index");
		PrintList(resultListC);
		System.out.println("result for Rel index");
		PrintList(resultListR);
		System.out.println("result for Index index");
		PrintList(resultListI);
		
	}
	
	static private void runOne5(long startTime, long endTime, List<long[]> resultListC, List<long[]> resultListR, List<long[]> resultListI, int startFrom) throws Exception{
//		String dbPathCircle = "D:\\indexTests\\indexTests\\CircleN\\";
//		String dbPathRel = "D:\\indexTests\\indexTests\\RelN\\";
//		String dbPathIndex = "D:\\indexTests\\indexTests\\IndexN\\";
//		@SuppressWarnings("unchecked")
//		Class<TemporalIndex>[] IndexCalsses = new Class[]{TindexCircleImpl.class, TindexRelList.class, TindexLucene.class};
		
		String dbPathCircle = "D:\\indexTests\\indexTests4\\CircleN\\";
		String dbPathRel = "D:\\indexTests\\indexTests4\\RelN\\";
		String dbPathRelTree = "D:\\indexTests\\indexTests4\\RelTreeN\\";
		String dbPathIndex = "D:\\indexTests\\indexTests4\\IndexN\\";
		
		@SuppressWarnings("unchecked")
		Class<TemporalIndex>[] IndexCalsses = new Class[]{TindexCircleImpl.class, TindexRelList.class, TindexRelTree.class ,TindexLucene.class};
		
//		Random ranGen = new Random();
		
		int[] u1 = new int[50];
		int[] u2 = new int[50];
		int[] u3 = new int[50];
		
		for(int i = 0; i < 50; i++){
			u1[i] = startFrom + 0 + i;
		}
		for(int i = 0; i < 50; i++){
			u2[i] = startFrom + 33 + i;
		}
		for(int i = 0; i < 50; i++){
			u3[i] = startFrom + 49 + i;
		}
		
		System.out.println("result for users: " + u1.length  + "," + u2.length + "," + u3.length +" from: "+ startTime + "-" + endTime + 
				"(" + ((endTime - startTime)/(60*60*1000)) + ")");
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], u1, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], u1, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathRelTree, IndexCalsses[2], u1, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[3], u1, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], u2, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], u2, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathRelTree, IndexCalsses[2], u2, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[3], u2, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], u3, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], u3, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathRelTree, IndexCalsses[2], u3, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[3], u3, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, false));
	}
	
	static private void runOne(long startTime, long endTime, List<long[]> resultListC, List<long[]> resultListR,List<long[]> resultListRt, List<long[]> resultListI) throws Exception{
		String dbPathCircle = "D:\\indexTests\\indexTests4\\CircleN\\";
		String dbPathRel = "D:\\indexTests\\indexTests4\\RelN\\";
		String dbPathRelTree = "D:\\indexTests\\indexTests4\\RelTreeN\\";
		String dbPathIndex = "D:\\indexTests\\indexTests4\\IndexN\\";
		
		@SuppressWarnings("unchecked")
		Class<TemporalIndex>[] IndexCalsses = new Class[]{TindexCircleImpl.class, TindexRelList.class, TindexRelTree.class ,TindexLucene.class};
		
		Random ranGen = new Random();
		
//		int u1 = 40 + ranGen.nextInt(10);
//		int u2 = 40 + ranGen.nextInt(10);
//		int u3 = 40 + ranGen.nextInt(10);
		int u1 = 20 + ranGen.nextInt(10);
		int u2 = 20 + ranGen.nextInt(10);
		int u3 = 20 + ranGen.nextInt(10);
		
		System.out.println("result for users: " + u1  + "," + u2 + "," + u3 +" from: "+ startTime + "-" + endTime + 
				"(" + ((endTime - startTime)/(60*60*1000)) + ")");
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListRt.add(SearchNodes(dbPathRelTree, IndexCalsses[2], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[3], new int[] {u1}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListRt.add(SearchNodes(dbPathRelTree, IndexCalsses[2], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[3], new int[] {u2}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		
		resultListC.add(SearchNodes(dbPathCircle, IndexCalsses[0], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListR.add(SearchNodes(dbPathRel, IndexCalsses[1], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListRt.add(SearchNodes(dbPathRelTree, IndexCalsses[2], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
		resultListI.add(SearchNodes(dbPathIndex, IndexCalsses[3], new int[] {u3}, bulidTempoTestDB.USERS_KEY, startTime, endTime, 5, true));
	}
	
	static private void PrintList(List<long[]> printMe){
		for(long[] tempArray:printMe){
			for(long tempLong:tempArray){
				System.out.print(tempLong + ",");
			}
			System.out.println();
		}
	}
	
	static private long[] SearchNodes(String dbsPath, Class<TemporalIndex> timeIndexClasses, int[] startNodesId, 
			String indexKey, long startTime, long endTime, int numOfRuns, boolean printResult) throws Exception{
		
		GraphDatabaseService gdb = new EmbeddedGraphDatabase(dbsPath);
		long[] runTimes = new long[numOfRuns];
		//get start nodes
		for (int n = 0; n < numOfRuns; n ++){
			long runtime = System.currentTimeMillis();
			List<TemporalIndex> startNodes = new ArrayList<TemporalIndex>();
			Index<Node> theIndex = gdb.index().forNodes(bulidTempoTestDB.THE_INDEX);
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
			
			System.out.println("result for run num " + n + " found " + finalResult.size() + "(" + timeIndexClasses.getCanonicalName() + ")");
			if(printResult){
				for(NodeWrapper temp:finalResult.values())
				{
					int id = -1;
					if(temp.hasProperty(bulidTempoTestDB.USER_ID)) id = (Integer)temp.getProperty(bulidTempoTestDB.USER_ID);
					if(temp.hasProperty(bulidTempoTestDB.LOCATION_ID)) id = (Integer)temp.getProperty(bulidTempoTestDB.LOCATION_ID);
					if(id < 0) return null;
					System.out.println(id + " wiht " + temp.getLeads() + " leads (" + temp + ")");
				}
			}
		}		
		gdb.shutdown();

		return runTimes;
	}
}

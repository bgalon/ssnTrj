package ac.technion.geoinfo.ssnTrj.apps;

import java.util.Random;

import ac.technion.geoinfo.ssnTrj.generator.BuildDBs;
import ac.technion.geoinfo.ssnTrj.indexes.temporal.TemporalIndex;
import ac.technion.geoinfo.ssnTrj.indexes.temporal.TindexCircleImpl;
import ac.technion.geoinfo.ssnTrj.indexes.temporal.TindexLucene;
import ac.technion.geoinfo.ssnTrj.indexes.temporal.TindexRelList;
import ac.technion.geoinfo.ssnTrj.indexes.temporal.TindexRelTree;

public class bulidTempoTestDB {
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
		String dbPathCircle = "indexTests7F/CircleN/";
		String dbPathRel = "indexTests7F/RelN/";
		String dbPathRelTree = "indexTests7F/RelTreeN/";
		String dbPathIndex = "indexTests7F/IndexN/";
		
		@SuppressWarnings("unchecked")
		Class<TemporalIndex>[] IndexCalsses = new Class[]{TindexCircleImpl.class, TindexRelList.class, TindexRelTree.class ,TindexLucene.class};
		
//		Random rangen = new Random();
		
		BuildDBs dbBuilder = new BuildDBs(new String[]{dbPathCircle, dbPathRel, dbPathRelTree, dbPathIndex}, IndexCalsses, 1000, 1000);
		dbBuilder.BuildUsersNLocations();
//		int iterCount = 1;
		long startTime = 0;
//		for(int i = 0; i < 100; i = i + 10){
//			long endTime  = startTime + (long)(60*60*1000) * iterCount * 1000;
//			int locStart = rangen.nextInt(11100 - iterCount * 1000);
//			dbBuilder.BuildTimesForUsers(i, i + 9, locStart , locStart + iterCount*1000 , startTime, endTime);
//			
//			
//			System.out.println("done users " + i + "-" + (i + 9) + ", Time: " + startTime + "-" + endTime + "(" + 
//					((endTime-startTime)/(1000 * 60 * 60))  + "h), locations: " + locStart + "-" + (locStart + iterCount*1000));
//			
//			iterCount++;
//			startTime = endTime + 1;
//		}
		
		long endTime = (long)(60L*60L*1000L*1000L);
		dbBuilder.BuildTimesForUsers(0, 1000, 0 , 1000 , 0, endTime);
		
		
		System.out.println("done users " + 0 + "-" + 1000 + ", Time: " + 0 + "-" + (long)(60*60*1000*1000) + "(" + 
				((endTime-startTime)/(1000 * 60 * 60))  + "h), locations: " + 0 + "-" + 1000);
		
		
		
		dbPathCircle = "indexTests7E/CircleN/";
		dbPathRel = "indexTests7E/RelN/";
		dbPathRelTree = "indexTests7E/RelTreeN/";
		dbPathIndex = "indexTests7E/IndexN/";
		
		dbBuilder = new BuildDBs(new String[]{dbPathCircle, dbPathRel, dbPathRelTree, dbPathIndex}, IndexCalsses, 1000, 1000);
		dbBuilder.BuildUsersNLocations();
		
//		iterCount = 10;
//		for(int i = 300; i < 6000; i=i+100){
//			long endTime  = startTime + (long)(60*60*1000) * iterCount;
//			int locStart = rangen.nextInt(111000 - iterCount/10);
//			dbBuilder.BuildTimesForUsers(i, i + 99, locStart , locStart + iterCount/10 , startTime, endTime);
//			
//			System.out.println("done users " + i + "-" + (i + 99) + ", Time: " + startTime + "-" + endTime + "(" + 
//					((endTime-startTime)/(1000 * 60 * 60))  + "h), locations: " + locStart + "-" + (locStart + iterCount));
//			
//			iterCount = iterCount * 10;
//			startTime = endTime + 1;
//		}
				
		dbBuilder.PrintDBreport();
		dbBuilder.Dispose();
	}
}

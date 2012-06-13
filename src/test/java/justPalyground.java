import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import ac.technion.geoinfo.ssnTrj.generator.BuildDBs;
import ac.technion.geoinfo.ssnTrj.indexes.temporal.timeHierarchy;


public class justPalyground {

	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
		
		Random rangen = new Random();
		
		//BuildDBs dbBuilder = new BuildDBs(new String[]{dbPathCircle, dbPathRel, dbPathRelTree, dbPathIndex}, IndexCalsses, 1000, 11100);
		//dbBuilder.BuildUsersNLocations();
		int iterCount = 1;
		long startTime = 0;
		for(int i = 0; i < 100; i = i + 10){
			long endTime  = startTime + (long)(60*60*1000) * iterCount * 1000;
			int locStart = rangen.nextInt(11100 - iterCount * 1000);
			//dbBuilder.BuildTimesForUsers(i, i + 9, locStart , locStart + iterCount , startTime, endTime);
			
			
			System.out.println("done users " + i + "-" + (i + 9) + ", Time: " + startTime + "-" + endTime + "(" + 
					((endTime-startTime)/(1000 * 60 * 60))  + "h), locations: " + locStart + "-" + (locStart + iterCount));
			
			iterCount++;
			startTime = endTime + 1;
		}
		
//		Calendar testCal = Calendar.getInstance();
//		testCal.setTime(new Date(System.currentTimeMillis()));
//		
//		System.out.println(testCal.get(Calendar.YEAR));
//
//		DateFormat formatter = new SimpleDateFormat("dd-MM-yy");
//		Calendar getDate = Calendar.getInstance();  
//		getDate.setTime(formatter.parse("26-02-12"));
//		System.out.println(getDate.getTime());
//		System.out.println();
//		
//		Date tempMin = new Date(getWeekMin(getDate.get(Calendar.YEAR),getDate.get(Calendar.MONTH), getDate.get(Calendar.WEEK_OF_MONTH)));
//		Date tempMax = new Date(getWeekMax(getDate.get(Calendar.YEAR),getDate.get(Calendar.MONTH), getDate.get(Calendar.WEEK_OF_MONTH)));
//		System.out.println(tempMin);
//		System.out.println(tempMax);
		
//		tempMin = new Date(getWeekMin(2012,2, 5));
//		tempMax = new Date(getWeekMax(2012,2, 5));
//		System.out.println(tempMin);
//		System.out.println(tempMax);
		
//		System.out.println();
//		
//		Calendar test1 = Calendar.getInstance();
//		System.out.println(test1.getTime());
//		System.out.println(test1.get(Calendar.WEEK_OF_MONTH) + " out of " + test1.getActualMaximum(Calendar.WEEK_OF_MONTH));
//		System.out.println(test1.get(Calendar.DAY_OF_MONTH) + " out of " + test1.getActualMaximum(Calendar.DAY_OF_MONTH));
//		
//		test1.add(Calendar.MONTH, -1);
//		System.out.println(test1.getTime());
//		System.out.println(test1.get(Calendar.WEEK_OF_MONTH) + " out of " + test1.getActualMaximum(Calendar.WEEK_OF_MONTH));
//		System.out.println(test1.get(Calendar.DAY_OF_MONTH) + " out of " + test1.getActualMaximum(Calendar.DAY_OF_MONTH));
//		
//		test1.add(Calendar.MONTH, -1);
//		System.out.println(test1.getTime());
//		System.out.println(test1.get(Calendar.WEEK_OF_MONTH) + " out of " + test1.getActualMaximum(Calendar.WEEK_OF_MONTH));
//		System.out.println(test1.get(Calendar.DAY_OF_MONTH) + " out of " + test1.getActualMaximum(Calendar.DAY_OF_MONTH));
		
		
//		System.out.println(temp.getTimeInMillis());
//		System.out.println(temp2.getTimeInMillis());
		
//		System.out.println(temp.getTime());
//		System.out.println(temp2.getTime());
		
		
	}
	
	
	private static long getWeekMax(int theYear, int theMonth, int theWeek){
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
	
	private static long getWeekMin(int theYear, int theMonth, int theWeek){
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

}

package ac.technion.geoinfo.trajectories;

import java.io.File;
import java.io.IOException;

import ac.technion.geoinfo.ssnTrj.trajectories.TaxiGPSTrj;

public class TrjPlayground {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		  String path = "C:\\cabspottingdata\\"; 
		  String resultPath = path + "kml\\";
		 
		  String files;
		  File folder = new File(path);
		  File[] listOfFiles = folder.listFiles(); 
		  
//		  System.out.print("name,no data,no calssification,error data,route,stay points,start time,end time,total time,");
//		  System.out.println("no data dis,no calssification dis,error data dis,route dis,stay points dis,total dis");
//		  BufferedWriter writer = new BufferedWriter(new FileWriter(path + "classResult.txt1"));
		  try
		  {
			  for (int i = 0; i < listOfFiles.length; i++) 
			  {
				  if (listOfFiles[i].isFile()) 
				  {
					  files = listOfFiles[i].getName();
					  if (files.endsWith(".txt") || files.endsWith(".TXT"))
					  {
						  TaxiGPSTrj testMe = TaxiGPSTrj.Txt2GPStrj(path + files);
						  testMe.analyizeMe(300000, 50, 1000000);
						  String name = files.substring(0, files.length() - 4);
//						  int[] testMeAsArray = testMe.getTrjAsArray(1211018404000L, 1213089934000L, 1000*60*30);
//						  writer.write(name + ",");
//						  for(int j:testMeAsArray)
//							  writer.write(j + ",");
//						  writer.newLine();
						  testMe.bulidKML(resultPath + name + ".kml");
						  
						  System.out.print(name + "," + testMe.totalNO_DATA + "," +  testMe.totalNO_CLASSIFICATION  + "," + testMe.totalERROR_DATA  + "," + testMe.totalROUTE  + "," + testMe.totalSTAY_POINT + "," + testMe.getStartTime() + "," + testMe.getEndTime() + "," + testMe.getTotaltime() + ",");
						  System.out.println(testMe.NO_DATADis + "," +  testMe.NO_CLASSIFICATIONDis  + "," + testMe.ERROR_DATADis  + "," + testMe.ROUTEDis  + "," + testMe.STAY_POINTDIs + "," + testMe.TOTALDis + ",");
			        }
			     }
			  }
		  }
		  catch(IOException e)
		  {
			  e.printStackTrace();
		  }
		  finally
		  {
//			  writer.close();
		  }
		
		
//		TaxiGPSTrj testMe = TaxiGPSTrj.Txt2GPStrj("C:\\cabspottingdata\\new_udwadla.txt");
//		testMe.analyizeMe(300000, 15, 900000);
//		//900000 mSec = 15 Min;
//		int[] testMeAsArray = testMe.getTrjAsArray(1211018404000L, 1213089934000L, 1000*60);
//		for(int i:testMeAsArray)
//			System.out.print(i + ",");
//		System.out.println();
//		
//		testMe.bulidKML("C:\\cabspottingdata\\new_upthin.kml");
//		
//		System.out.println("total file time: " + testMe.getTotaltime());
//		System.out.println("No Data time: " + testMe.totalNO_DATA);
//		System.out.println("No Classification time: " + testMe.totalNO_CLASSIFICATION);
//		System.out.println("Error Data time: " + testMe.totalERROR_DATA);
//		System.out.println("Routes time: " + testMe.totalROUTE);
//		System.out.println("Stay Point time: " + testMe.totalSTAY_POINT);
//		testMe.print();
	}

}

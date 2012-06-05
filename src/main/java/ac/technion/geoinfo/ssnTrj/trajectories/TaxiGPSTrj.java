package ac.technion.geoinfo.ssnTrj.trajectories;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.LinkLoopException;

public class TaxiGPSTrj {
	
	private LinkedList<PIT4Texi> pntSq = new LinkedList<PIT4Texi>();
	private LinkedList<Leg> legLst = new LinkedList<Leg>();;
	
	public long totalNO_DATA = 0;
	public long totalNO_CLASSIFICATION = 0;
	public long totalERROR_DATA = 0;
	public long totalROUTE = 0;
	public long totalSTAY_POINT = 0;
	
	public double NO_DATADis = 0;
	public double NO_CLASSIFICATIONDis = 0;
	public double ERROR_DATADis = 0;
	public double ROUTEDis = 0;
	public double STAY_POINTDIs = 0;
	
	public double TOTALDis = 0; 
	
	static public TaxiGPSTrj Txt2GPStrj(String fileName)
	{
		TaxiGPSTrj tempGPSTrj = new TaxiGPSTrj(); 
		try{
			BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
			try 
			{
				String inputLine;
				while ((inputLine=bufferedReader.readLine())!=null){
					String[] splitLine = inputLine.split("\\ ");
					PIT4Texi tempPnt = new PIT4Texi();
					tempPnt.lat = Float.parseFloat(splitLine[0]);
					tempPnt.lon = Float.parseFloat(splitLine[1]);
					if (Integer.parseInt(splitLine[2]) == 1) tempPnt.HasPassenger = true;
					tempPnt.t = new Date(Long.parseLong(splitLine[3])*1000);
					tempGPSTrj.pntSq.add(0,tempPnt);
				}
			}
			finally
			{
				bufferedReader.close();			
			}
		}
		catch (Exception  e) {
			throw new IllegalArgumentException("Error reading file " + fileName );
		}
		return tempGPSTrj;
	}
	
	public long getTotaltime()
	{
		if (pntSq != null)
			return pntSq.getLast().t.getTime() - pntSq.getFirst().t.getTime();
		return 0;
	}
	
	public void analyizeMe(long dt, float dis, long noDataDt) throws Exception
	//split the route to no-data, stay-point and routes between stayPoint.
	//dt, noDataDt in milliseconds and dis in meters
	{
		legLst.add(new Leg());
		//long startTime = System.nanoTime();
		
		PIT4Texi lastPnt = null;
		PointInTime middle = null; // the lat, lon are the mean and the time is the start time
		LinkedList<PIT4Texi> SPs = null;
		boolean hasPass;
		
		for(PIT4Texi tempTP:pntSq)
		{
			if(lastPnt == null) 
			{
				//for the first interation 
				legLst.getLast().addPnt(tempTP);
				lastPnt = tempTP;
				hasPass = tempTP.HasPassenger;
				continue;
			}
			long pntDt = tempTP.t.getTime() - lastPnt.t.getTime();
			if(pntDt > noDataDt)
			{
				//lastPnt.PITClass = RouteClassification.NO_DATA;
				tempTP.PITClass = RouteClassification.NO_DATA;
				lastPnt = tempTP;
				continue;
			}
			
			tempTP.Dis = tempTP.Distance(lastPnt); //this calc now for validation  
			TOTALDis += tempTP.Dis;
			
			if(tempTP.Dis > dis)
			{
				double time4Speed = (tempTP.t.getTime() - lastPnt.t.getTime()) / (double)(1000*60*60);
				if ((tempTP.Dis/1000)/(time4Speed)> 200) //if case drive speed is higher then 200 KPH
				{
					lastPnt.PITClass = RouteClassification.ERROR_DATA;
					tempTP.PITClass = RouteClassification.ERROR_DATA;
				} else {
					lastPnt.PITClass = RouteClassification.ROUTE;
					tempTP.PITClass = RouteClassification.ROUTE;
				}
			}
			else
			{
				if(middle != null)
					//we on a potential stay point
				{
					double STdis = tempTP.Distance(middle);
					if(STdis < dis)
					{ //this pnt can be in the stay point
						middle.lat = (middle.lat * SPs.size() + tempTP.lat)/(SPs.size() + 1);
						middle.lon = (middle.lon * SPs.size() + tempTP.lon)/(SPs.size() + 1);
						SPs.add(tempTP);
					}
					else 
					{
						if(SPs.getLast().t.getTime() - SPs.getFirst().t.getTime() >= dt)
						{// the collection is a stay point
							RouteClassification tempCalss = SPs.getFirst().PITClass;
							for(PIT4Texi tempSP:SPs) 
							{
								tempSP.PITClass = RouteClassification.STAY_POINT;
							}
							SPs.getFirst().PITClass = tempCalss;
						}
						middle = null;
						SPs = null;
					}
					lastPnt = tempTP;
					continue;	
				}
				
				
				// start a new stay point
				middle = new PointInTime();
				middle.lat = (lastPnt.lat + tempTP.lat) / 2;
				middle.lon = (lastPnt.lon + tempTP.lon) / 2;
				middle.t = lastPnt.t;
				SPs = new LinkedList<PIT4Texi>();
				SPs.add(lastPnt);
				SPs.add(tempTP);
			}
			lastPnt = tempTP;
		}
		buildLegs();
		//long totalTime = startTime - System.nanoTime();
		//System.out.println(totalTime);
	}
	
	private void buildLegs() throws Exception
	{
		PIT4Texi lastPnt = null;
		LinkedList<PIT4Texi> pntLst = null;
		RouteClassification lstClass = null;
		for(PIT4Texi tempPnt:pntSq)
			{
				if(lastPnt == null) 
				{//for the first run
					pntLst = new LinkedList<PIT4Texi>();
					pntLst.add(tempPnt);
//					totalDis += tempPnt.Dis;
					lstClass = tempPnt.PITClass;

					lastPnt = tempPnt;
					continue;
				}
				
				if(tempPnt.PITClass != lstClass)
				{//store the data and start a new list
					for(PIT4Texi pnt4Leg:pntLst)
						addTolegs(pnt4Leg);
					pntLst = null;
//					totalDis = 0;
				}
				else
				{//add this item to the list
					pntLst.add(tempPnt);
				}
				if(pntLst == null)
				{//start a new lst
					pntLst = new LinkedList<PIT4Texi>();
					pntLst.add(lastPnt);
					pntLst.add(tempPnt);
					lstClass = tempPnt.PITClass;
				}
				
				lastPnt = tempPnt;
			}
			for(PIT4Texi pnt4Leg:pntLst)
				addTolegs(pnt4Leg);
	}
	
	private void addTolegs(PIT4Texi tempPnt) throws Exception
	{
		if(legLst.getLast().getLegClass() != tempPnt.PITClass) 
		{
				PIT4Texi openLegWith = legLst.getLast().getLast();
				legLst.add(new Leg());
				legLst.getLast().addPnt(openLegWith);	
		}
		legLst.getLast().addPnt(tempPnt);
	}
	

	
	public void bulidKML(String fileName) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		try
		{
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); writer.newLine();
			writer.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">"); writer.newLine();
			writer.write("<Document>"); writer.newLine();
			
			writer.write("<Style id=\"route\">"); writer.newLine();
			writer.write("<LineStyle>"); writer.newLine();
			writer.write("<color>7f00ffff</color>"); writer.newLine();
			writer.write("<width>4</width>"); writer.newLine();
			writer.write("</LineStyle>"); writer.newLine();
			writer.write("</Style>"); writer.newLine();
			
			writer.write("<Style id=\"stay\">"); writer.newLine();
			writer.write("<PolyStyle>"); writer.newLine();
			writer.write("<color>7dff0000</color>"); writer.newLine();
			writer.write("</PolyStyle>"); writer.newLine();
			writer.write("</Style>"); writer.newLine();
			
			writer.write("<Style id=\"err\">"); writer.newLine();
			writer.write("<LineStyle>"); writer.newLine();
			writer.write("<color>80990000</color>"); writer.newLine();
			writer.write("<width>4</width>"); writer.newLine();
			writer.write("</LineStyle>"); writer.newLine();
			writer.write("</Style>"); writer.newLine();

			for(Leg templeg:legLst)
			{
				printLst2Kml(templeg, writer);
			}
			
			writer.write("</Document>"); writer.newLine();
			writer.write("</kml>");writer.newLine();
		}
		catch(IOException e)
		{
			
		}
		finally
		{
			writer.close();
		}
	}
	
	private void printLst2Kml(Leg legToWrite,BufferedWriter writer) throws IOException
	{
		switch (legToWrite.getLegClass()) {
		case NO_CLASSIFICATION:
			writeNoClass(legToWrite, writer);
			totalNO_CLASSIFICATION += legToWrite.getTotaltime();
			NO_CLASSIFICATIONDis += legToWrite.getLegDis();
			break;
		case NO_DATA:
			writeNoData(legToWrite, writer);
			totalNO_DATA += legToWrite.getTotaltime();
			NO_DATADis += legToWrite.getLegDis();
			break;
		case ERROR_DATA:
			writeErrData(legToWrite, writer);
			totalERROR_DATA += legToWrite.getTotaltime();
			ERROR_DATADis += legToWrite.getLegDis();
			break;
		case ROUTE:
			writeRoute(legToWrite, writer);
			totalROUTE += legToWrite.getTotaltime();
			ROUTEDis += legToWrite.getLegDis();
			break;
		case STAY_POINT:
			writeStayPnt(legToWrite, writer);
			totalSTAY_POINT += legToWrite.getTotaltime();
			STAY_POINTDIs += legToWrite.getLegDis();
			break;
		default:
			break;
		}
	}
	
	private void writeNoClass(Leg pntLst, BufferedWriter writer) throws IOException
	{
		double dis = CalcDis(pntLst);
		writer.write("<Placemark>"); writer.newLine();
		//writer.write("<name>" + tempPnt.t.toString() + "</name>");writer.newLine();
		writer.write("<description><![CDATA[");writer.newLine();
		writer.write("No Classifcation <br/>");writer.newLine(); 
		writer.write("start time: " + pntLst.getStartTime().toString() +  "<br/>");writer.newLine();
		writer.write("end time: " + pntLst.getLast().t.toString() +  "<br/>");writer.newLine();
		writer.write("total time [min]: " + pntLst.getTotaltime()/(long)(1000*60) +  "<br/>");writer.newLine();
		writer.write("Distance [m]: " + dis +  "<br/>");writer.newLine();
		writer.write("]]></description>");writer.newLine();
		writer.write("<MultiGeometry>");writer.newLine();
		for(PIT4Texi tempPnt:pntLst)
		{
			writer.write("<Point><coordinates>" + tempPnt.lon + "," + tempPnt.lat + ",0" +"</coordinates></Point>");writer.newLine(); 
		}
		writer.write("</MultiGeometry>");writer.newLine();
		writer.write("</Placemark>"); writer.newLine();
	}
	
	private void writeNoData(Leg pntLst, BufferedWriter writer) throws IOException
	{
		double dis = CalcDis(pntLst);
		writer.write("<Placemark>"); writer.newLine();
		//writer.write("<name>" + tempPnt.t.toString() + "</name>");writer.newLine();
		writer.write("<description><![CDATA[");writer.newLine();
		writer.write("No Data  <br/>");writer.newLine();
		writer.write("start time: " + pntLst.getStartTime().toString() +  "<br/>");writer.newLine();
		writer.write("end time: " + pntLst.getLast().t.toString() +  "<br/>");writer.newLine();
		writer.write("total time [min]: " + pntLst.getTotaltime()/(long)(1000*60) +  "<br/>");writer.newLine();
		writer.write("Distance [m]: " + dis +  "<br/>");writer.newLine();
		writer.write("]]></description>");writer.newLine();
		writer.write("<MultiGeometry>");writer.newLine();
		for(PIT4Texi tempPnt:pntLst)
		{
			writer.write("<Point><coordinates>" + tempPnt.lon + "," + tempPnt.lat + ",0" +"</coordinates></Point>");writer.newLine(); 
		}
		writer.write("</MultiGeometry>");writer.newLine();
		writer.write("</Placemark>"); writer.newLine();
	}
	
	private void writeErrData(Leg pntLst, BufferedWriter writer) throws IOException
	{
		writer.write("<Placemark>"); writer.newLine();
		
		//writer.write("<name>" + tempPnt.t.toString() + "</name>");writer.newLine();
		writer.write("<LineString><coordinates>");writer.newLine();
		double dis = 0;
		for(PIT4Texi tempPnt:pntLst)
		{
			writer.write( tempPnt.lon + "," + tempPnt.lat + ",0");writer.newLine(); 
			dis = dis + tempPnt.Dis;
		}
		writer.write("</coordinates></LineString >");writer.newLine();
		writer.write("<description><![CDATA[");writer.newLine();
		writer.write("<h1>Data Error</h1>");writer.newLine();
		writer.write("start time: " + pntLst.getStartTime().toString() +  "<br/>");writer.newLine();
		writer.write("end time: " + pntLst.getLast().t.toString() +  "<br/>");writer.newLine();
		writer.write("total time [min]: " + pntLst.getTotaltime()/(long)(1000*60) +  "<br/>");writer.newLine();
		writer.write("Distance [m]: " + dis +  "<br/>");writer.newLine();
		writer.write("]]></description>");writer.newLine();
		writer.write("<styleUrl>#err</styleUrl>"); writer.newLine();
		writer.write("</Placemark>"); writer.newLine();
	}
	
	private void writeRoute(Leg pntLst, BufferedWriter writer) throws IOException
	{
		writer.write("<Placemark>"); writer.newLine();
		
		//writer.write("<name>" + tempPnt.t.toString() + "</name>");writer.newLine();
		writer.write("<LineString><coordinates>");writer.newLine();
		double dis = 0;
		for(PIT4Texi tempPnt:pntLst)
		{
			writer.write( tempPnt.lon + "," + tempPnt.lat + ",0");writer.newLine(); 
			dis = dis + tempPnt.Dis;
		}
		writer.write("</coordinates></LineString >");writer.newLine();
		writer.write("<description><![CDATA[");writer.newLine();
		writer.write("<h1>Route</h1>");writer.newLine();
		writer.write("start time: " + pntLst.getStartTime().toString() +  "<br/>");writer.newLine();
		writer.write("end time: " + pntLst.getLast().t.toString() +  "<br/>");writer.newLine();
		writer.write("total time [min]: " + pntLst.getTotaltime()/(long)(1000*60) +  "<br/>");writer.newLine();
		writer.write("Distance [m]: " + dis +  "<br/>");writer.newLine();
		writer.write("]]></description>");writer.newLine();
		writer.write("<styleUrl>#route</styleUrl>"); writer.newLine();
		writer.write("</Placemark>"); writer.newLine();
	}
	
	private void writeStayPnt(Leg pntLst, BufferedWriter writer) throws IOException
	{
		double dis = CalcDis(pntLst);
		writer.write("<Placemark>"); writer.newLine();
		writer.write("<styleUrl>#stay</styleUrl>"); writer.newLine();
		//writer.write("<name>" + tempPnt.t.toString() + "</name>");writer.newLine();
		writer.write("<description><![CDATA[");writer.newLine();
		writer.write("Stay Point  <br/>");writer.newLine();
		writer.write("start time: " + pntLst.getStartTime().toString() +  "<br/>");writer.newLine();
		writer.write("end time: " + pntLst.getLast().t.toString() +  "<br/>");writer.newLine();
		writer.write("total time [min]: " + pntLst.getTotaltime()/(long)(1000*60) +  "<br/>");writer.newLine();
		writer.write("Distance [m]: " + dis +  "<br/>");writer.newLine();
		writer.write("]]></description>");writer.newLine();
		writer.write("<MultiGeometry>");writer.newLine(); 
		for(PIT4Texi tempPnt:pntLst)
		{
			writer.write("<Point><coordinates>" + tempPnt.lon + "," + tempPnt.lat + ",0" +"</coordinates></Point>");writer.newLine();
		}
		writeCircle(pntLst.getMeanPnt(), pntLst.getRadiusInDeg(), writer);
		writer.write("</MultiGeometry>");writer.newLine();
		writer.write("</Placemark>"); writer.newLine();
	}
	
	private double CalcDis(Leg pntLst)
	{
		double dis = 0;
		for(PIT4Texi tempPnt:pntLst)
		{
			dis = dis + tempPnt.Dis;
		}
		return dis;
	}
	
	private void writeCircle(PointInTime meanPnt, double r, BufferedWriter writer) throws IOException
	{
		writer.write("<Polygon><extrude>1</extrude><altitudeMode>relativeToGround</altitudeMode>");writer.newLine();
		writer.write("<outerBoundaryIs><LinearRing><coordinates>");writer.newLine();
		for(int i = 0; i < 360; i++)
		{
			double angle = i * Math.PI / 180;
			double newlat = meanPnt.lat + r * Math.cos(angle);
			double newlon = meanPnt.lon + r * Math.sin(angle);
			writer.write( newlon + "," + newlat + ",0");writer.newLine(); 
		}
		writer.write("</coordinates></LinearRing></outerBoundaryIs></Polygon>");writer.newLine();
	}
	
	public long getStartTime()
	{
		return pntSq.getFirst().t.getTime();
	}
	
	public long getEndTime()
	{
		return pntSq.getLast().t.getTime();
	}
	
	public int[] getTrjAsArray(long startTime, long endTime, long dt)
	{
		//test if the start time before the end time
		int arrayLength = (int) Math.ceil((endTime - startTime)/dt);
		int[] tempArray = new int[arrayLength];
		long thisTime = startTime;
		Iterator<Leg> iter = legLst.iterator();
		Leg thisLeg = iter.next();
		//find the start point
//		while(thisTime > thisLeg.getStartTime().getTime() && iter.hasNext())
//		{
//			thisLeg = iter.next();
//		}
		
		int i = 0;
		while(i < tempArray.length)
		{
			if(thisTime > thisLeg.getStartTime().getTime())
			{
				if(thisTime < thisLeg.getEndTiem().getTime())
				{
					tempArray[i] = thisLeg.getLegClass().ordinal();
					thisTime += dt;
					i++;
				}
				else
				{
					if (iter.hasNext())
					{
						thisLeg = iter.next();
					}
					else 
					{
						for(int j = i; j< tempArray.length; j++) 
							tempArray[j] = -1;
						break;
					}
				}
			}
			else
			{
				tempArray[i] = -1;
				thisTime += dt;
				i++;
			}
		}
		
		return tempArray;
	}
	
	public void print()
	{
		if (pntSq.isEmpty())
		{
			System.out.println("the Gps trajectory have no points");
		}
		for(PointInTime tempPnt:pntSq)
		{
			System.out.println(tempPnt.toString());
		}
		
	}
}

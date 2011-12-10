package ac.technion.geoinfo.ssnTj.trajectories;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

public class Leg implements Iterable<PIT4Texi>{
	private RouteClassification legClass = null;
	private double legDis = 0;
	private PointInTime mean;
	private double r = 0;
	private double rInDeg = 0;
	
	private LinkedList<PIT4Texi> pntLst = new LinkedList<PIT4Texi>(); 
	
	public long getTotaltime()
	{
		if (!pntLst.isEmpty())
			return pntLst.getLast().t.getTime() - pntLst.getFirst().t.getTime();
		return 0;
	}
	
	public Date getStartTime()
	{
		return pntLst.getFirst().t;
	}
	
	public Date getEndTiem()
	{
		return pntLst.getLast().t;
	}
	
	public double getLegDis()
	{
		return legDis;
	}
	
	public double getRadius()
	{
		return r;
	}
	
	public double getRadiusInDeg()
	{
		return rInDeg;
	}
	
	public void addPnt(PIT4Texi theNewPnt) throws Exception
	{
		if(pntLst.size() < 2)
			 legClass = theNewPnt.PITClass;
		if(legClass == theNewPnt.PITClass)
		{
			if (!pntLst.isEmpty())
				legDis += theNewPnt.Distance(pntLst.getLast());
			if(mean == null) 
			{
				mean = new PointInTime();
				mean.lat = theNewPnt.lat;
				mean.lon = theNewPnt.lon;
				mean.t = theNewPnt.t;
			}
			else
			{
				mean.lat = (mean.lat * pntLst.size() + theNewPnt.lat)/(pntLst.size() + 1);
				mean.lon = (mean.lon * pntLst.size() + theNewPnt.lon)/(pntLst.size() + 1);
				mean.t.setTime((getEndTiem().getTime() - getStartTime().getTime()) / 2);
				//WORING THIS IS A WORNG WAY TO  CLACULATE R
				double newR = mean.Distance(theNewPnt);
				if (newR > r) r = newR;
				double newRinDeg = Math.sqrt(Math.pow(mean.lat - theNewPnt.lat, 2) + Math.pow(mean.lon - theNewPnt.lon, 2));
				if(newRinDeg > rInDeg) rInDeg = newRinDeg;
			}
			pntLst.add(theNewPnt);
		}
		else
		{
			throw new Exception("the point type is not the same as the leg type");
		}
	}
	
	public PIT4Texi getLast()
	{
		return pntLst.getLast();
	}
	
	public RouteClassification getLegClass()
	{
		return legClass;
	}
	
	public PointInTime getMeanPnt()
	{
		return mean;
	}

	public Iterator<PIT4Texi> iterator() {
		return pntLst.iterator();
	}
}

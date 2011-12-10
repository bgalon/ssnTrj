package ac.technion.geoinfo.ssnTj.trajectories;

import java.util.Date;

public class PointInTime {
	public float lat;
	public float lon;
	public Date t;
	
	final int R = 6371009;
	
	public double Distance(PointInTime otherPnt)
	{
		double dLat = (otherPnt.lat - lat) * (Math.PI / 180);
		double meanLat = ((otherPnt.lat + lat) / 2) * (Math.PI / 180);
		double dLon = (otherPnt.lon - lon) * (Math.PI / 180);
		double dis = R * Math.sqrt(Math.pow(dLat, 2) + 
							Math.pow(Math.cos(meanLat) * dLon, 2));
		return dis;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return lat + " " + lon + " " + t.getTime();
	}
}

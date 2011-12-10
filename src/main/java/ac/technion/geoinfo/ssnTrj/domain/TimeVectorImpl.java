package ac.technion.geoinfo.ssnTrj.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TimeVectorImpl implements TimeVector {

	private final Map<Integer, Integer> theTVec;
	private Date startAt;
	
	public TimeVectorImpl()
	{
		theTVec = new HashMap<Integer, Integer>();
	}
	
	public boolean hasTime(Date checkTime) {
		// TODO Auto-generated method stub
		return false;
	}

	public Date getStartTime() {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getEndTime() {
		// TODO Auto-generated method stub
		return null;
	}

	public void insertTime(Date startTime, Date endTime, double c) {
		//figure out what to do in case that 
		if (startAt == null) startAt = startTime;
		if (startTime.before(startAt))
		{
			
		}
		if (startTime.after(startAt) || startTime.equals(startAt))
		{
			
		}
		}
		
		
	}

}

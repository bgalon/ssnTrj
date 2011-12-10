package ac.technion.geoinfo.ssnTrj.domain;

import java.util.Date;

public interface TimeVector {
	
	/*
	 * holds a time vector, and have methods to retrieve data form this vector
	 * 
	 * 
	 */
	
	boolean hasTime(Date checkTime);
	
	Date getStartTime();
	Date getEndTime();
	
	void insertTime(Date startTime, Date endTime, double c);
}

package ac.technion.geoinfo.ssnTrj.domain;

import java.util.Date;

public class TimeIntervalImpl implements TimeInterval {

	long startTime;
	long endTime;
	
	public TimeIntervalImpl(long theStartTime, long theEndTime) throws Exception{
		if (theEndTime <= theStartTime)
			throw new Exception("end time is biger or equal to start time");
		startTime = theStartTime;
		endTime = theEndTime;
	}
	
	public long GetStartTime() {
		return startTime;
	}

	public long GetEndTime() {
		return endTime;
	}

	public long GetDuration() {
		return endTime - startTime;
	}

	public Date GetStartDate() {
		return new Date(startTime);
	}

	public Date GetEndDate() {
		return new Date(endTime);
	}

	public TimeIntervalIntersection intersect(TimeInterval other) {
		/**
		 * Return 0 if this interval is before the other interval
		 * Return 1 if the Intervals intersects
		 * Return 2 if this Interval is after the other interval 
		 **/
		if (startTime > other.GetStartTime()){
			if (startTime > other.GetEndTime()) return TimeIntervalIntersection.after;
			return TimeIntervalIntersection.intersect;
		}else{ //in case (i1start <= i2start)
			if (other.GetStartTime() > endTime) return TimeIntervalIntersection.before;
			return TimeIntervalIntersection.intersect;
		}
	}

}

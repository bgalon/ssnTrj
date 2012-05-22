package ac.technion.geoinfo.ssnTrj.domain;

import java.io.Serializable;
import java.util.Date;

public interface TimeInterval extends Serializable{
	long GetStartTime();
	long GetEndTime();
	long GetDuration();
	Date GetStartDate();
	Date GetEndDate();
	TimeIntervalIntersection intersect(TimeInterval other);
}

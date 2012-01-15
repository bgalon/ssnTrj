package ac.technion.geoinfo.ssnTrj.domain;

public interface TimePattern {

	TimeFrame getFrame();
	int[] getUnits();
	int getStart();
	int getEnd();
	boolean isDoual();
	
	User getUser();
	SpatialEntity getSpatialEntity() throws Exception;
	
	double intersectDiff(TimePattern otherTimePattern);
}

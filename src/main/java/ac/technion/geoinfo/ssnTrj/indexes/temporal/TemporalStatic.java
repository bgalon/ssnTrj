package ac.technion.geoinfo.ssnTrj.indexes.temporal;

import ac.technion.geoinfo.ssnTrj.domain.Static;

public interface TemporalStatic extends Static {
	//properties
	public static final String START_TIME = "time_index_start_time";
	public static final String END_TIME = "time_index_end_time";
	public static final String TIME_HIERARCHY = "hierarchy";
	public static final String HIERARCHY_VALUE = "hierarchy_value";
	public static final String NUMBER_OF_LEAVES = "leaves_num";
	
	//values for time hierarchy property
	public static final String YEAR = "year";
	public static final String MANTH = "manth";
	public static final String WEEK = "week";
	public static final String DAY = "day";
	public static final String TIME_IN_DAY = "time";
}

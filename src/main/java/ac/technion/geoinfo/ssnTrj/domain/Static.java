package ac.technion.geoinfo.ssnTrj.domain;

public interface Static {
	//general attributes
	public static final String SSN_TYPE = "ssnType";
	public static final String FULLTEXT_PROPERTY = "fulTxtProp";
	
	//type option
	public static final String TYPE_INDEX = "type";
	public static final String TEMPORAL_INDEX = "temopral_index";
	public static final String ROAD_SEGMENT = "roadSegment";
	public static final String ONEWAY_PROPERTY = "osm_oneway";
	public static final String BULIDING = "buliding";
	public static final String ROUTE = "route";
	public static final String SPATIAL_GROUP ="spatialGroup";
	public static final String USER = "user";
	

	//network option
	// static final String S
	public static final String SPATIAL_LAYER = "spatialLyr";
	public static final String ROUTE_LAYER = "routeLyr";
	
	//Spatial node attributes 
	//public static final String GEOMETRY = "geometry";
	public static final String SPATIAL_KEY = "spatialKey";
	
	//Spatial relationship attributes
	public static final String SEGMENT_NUMBER ="segmentNum";
	
	//Spatial indexes
	public static final String SPATIAL_FULLTEXT_INDEX = "spatialFullText";
	public static final String SPATIAL_FULLTEXT_KEY = "spatialMainKey"; 
	
	//Social attributes
	
	//Social indexes
	public static final String SOCIAL_KEY_INDEX = "socialKey";
	public static final String SOCIAL_KEY_INDEX_KEY = "uName";
	public static final String SOCIAL_FULLTEXT_INDEX = "socialFullText";
	public static final String SOCIAL_FULLTEXT_KEY = "socialMainKey";
	
	//time pattern attributes
	public static final String TIME_PATTERN_PORP = "timePattren";
	public static final String CONFIDENT_PROP = "confident";
}

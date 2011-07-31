package ac.technion.geoinfo.ssnTrj.domain;

public interface Static {
	//general attributes
	public static final String SSN_TYPE = "ssnType";
	public static final String FULLTEXT_PROPERTY = "fulTxtProp";
	
	//type option
	public static final String ROAD_SEGMENT = "roadSegment";
	public static final String BULIDING = "buliding";
	public static final String ROUTE = "route";
	public static final String SPATIAL_GROUP ="spatialGroup";
	public static final String USER = "user";

	//network option
	// static final String S
	
	//Spatial attributes 
	public static final String GEOMETRY = "geometry";
	
	public static final String SPATIAL_LAYER = "spatialData";
	
	//Spatial indexes
	public static final String SPATIAL_FULLTEXT_INDEX = "SpatialFullText";
	public static final String SPATIAL_FULLTEXT_KEY = "SpatialMainKey"; 
	
	//Social attributes
	
	//Social indexes
	public static final String SOCIAL_KEY_INDEX = "SocialKey";
	public static final String SOCIAL_KEY_INDEX_KEY = "uname";
	public static final String SOCIAL_FULLTEXT_INDEX = "SocialFullText";
	public static final String SOCIAL_FULLTEXT_KEY = "SocialMainKey";
	
	//time pattern attributes
	public static final String TIME_PATTERN_PORP = "timePattren";
	public static final String CONFIDENT_PROP = "confident";
}

package ac.technion.geoinfo.ssnTrj.domain;

import org.neo4j.graphdb.RelationshipType;

public enum SocialRelation implements RelationshipType {
	Friend, Family;
	
	public static SocialRelation Parse(String value)
	{
		if (value.equalsIgnoreCase("Friend")) return SocialRelation.Friend;
		if (value.equalsIgnoreCase("Family")) return SocialRelation.Family;
		return null;
	}
}

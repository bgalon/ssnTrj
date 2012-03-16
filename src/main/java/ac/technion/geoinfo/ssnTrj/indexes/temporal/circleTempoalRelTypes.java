package ac.technion.geoinfo.ssnTrj.indexes.temporal;

import org.neo4j.graphdb.RelationshipType;

public enum circleTempoalRelTypes implements RelationshipType {
	//year, manth, week, day
	next_time, inclue_time, time_reference
}

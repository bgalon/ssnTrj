package ac.technion.geoinfo.ssnTrj.domain;

import org.neo4j.graphdb.RelationshipType;

public enum SpatialRelation implements RelationshipType {
	touch, within, lead_to,
	//route relation
	include, startAt, endAt
}

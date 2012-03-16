package ac.technion.geoinfo.ssnTrj.domain;

import org.neo4j.graphdb.RelationshipType;

public enum TimePatternRelation implements RelationshipType {
	tpToRoute, tpToSpatialEntity, tpIndex
}

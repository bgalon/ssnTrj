package ac.technion.geoinfo.ssnTrj.domain;

import org.neo4j.graphdb.Node;

public interface NodeWrapper extends Node {
	Node getNode();
	String getType();
	double getExpected();
}

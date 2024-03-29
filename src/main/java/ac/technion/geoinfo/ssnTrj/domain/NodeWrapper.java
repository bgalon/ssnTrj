package ac.technion.geoinfo.ssnTrj.domain;

import org.neo4j.graphdb.Node;

public interface NodeWrapper extends Node {
	Node getNode();
	String getType();
	double getExpected();
	
	void addLaed();
	void addLaeds(int numberOfLeaeds);
	int getLeads();
	
	@Override
    public boolean equals(Object obj);
	@Override
	public int hashCode();
}

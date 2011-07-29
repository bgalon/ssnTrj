package ac.technion.geoinfo.ssnTrj.domain;

import org.neo4j.graphdb.Node;

public class UserImpl extends NodeWarpperImpl implements User {
 
	public UserImpl(Node theNode)
	{
		super(theNode);
	}
}

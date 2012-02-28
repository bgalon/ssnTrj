package ac.technion.geoinfo.ssnTrj.geometry;

import org.neo4j.gis.spatial.query.AbstractSearchIntersection;
import org.neo4j.graphdb.Node;

import ac.technion.geoinfo.ssnTrj.domain.Static;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class ContainsBySNNtypes extends AbstractSearchIntersection implements Static {

	String[] SSNtype;
	
	public ContainsBySNNtypes(Geometry other, String[] theSSNtype) {
		super(other);
		this.SSNtype = theSSNtype;
	}

	protected void onEnvelopeIntersection(Node geomNode, Envelope geomEnvelope) {
		Geometry geometry = decode(geomNode);
		if (geomNode.hasProperty(SSN_TYPE)){ 
			boolean testType = false;
			for(String tempSSNtype:SSNtype)
			{
				if (((String)geomNode.getProperty(SSN_TYPE)).equals(tempSSNtype))
				{
					testType = true;
					break;
				}
			}
			
			if (testType) {
				if (other.contains(geometry)) {	
					add(geomNode, geometry);
				}
			}
		}
	}

}

package ac.technion.geoinfo.ssnTrj.serverPlugin;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.query.SearchInRelation;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.Name;
import org.neo4j.server.plugins.Parameter;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;

import ac.technion.geoinfo.ssnTrj.domain.Static;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;


@Description("Plugin for the socio-spatial network, this plugin allow to qurey the SSN")
public class SSNplugin extends ServerPlugin implements Static {
	
	
	@Name("Select")
	@Description("The Select operator get ..... ")
	@PluginTarget(GraphDatabaseService.class)
	public Iterable<Node> Select(	@Source GraphDatabaseService graphDb,
									@Parameter(name = "network_source") String netSource,
									@Parameter(name = "search_query") String theQuery) throws Exception
	{
		SpatialDatabaseService sgDB = new SpatialDatabaseService(graphDb);
		if (!theQuery.toLowerCase().startsWith("in:"))
			throw new Exception("the query " + theQuery + "is not a spatial query");
		int layerInd = theQuery.toLowerCase().indexOf("@layer:");
		if (layerInd < 0)
			throw new Exception("the query " + theQuery + "is not a spatial query");
		String lyr = theQuery.substring(layerInd + 7);
		String envelope = theQuery.substring(3, layerInd);
		
		EditableLayer thelayer = null;
		if(lyr.equalsIgnoreCase("spatial"))
		{
			thelayer = (EditableLayer)sgDB.getLayer(SPATIAL_LAYER);
		}
		else if (lyr.equalsIgnoreCase("route"))
		{
			thelayer = (EditableLayer)sgDB.getLayer(ROUTE_LAYER);
		}
		if (thelayer == null)
			throw new Exception("the layer " + lyr + "no exsist");
		
		String[] splitStr = envelope.split(",");
		Coordinate[] coordinates = new Coordinate[5];
		coordinates[0] = coordinates[4] = new Coordinate(Double.parseDouble(splitStr[2]),Double.parseDouble(splitStr[1]));
		coordinates[1] = new Coordinate(Double.parseDouble(splitStr[0]),Double.parseDouble(splitStr[1]));
		coordinates[2] = new Coordinate(Double.parseDouble(splitStr[0]),Double.parseDouble(splitStr[3]));
		coordinates[3] = new Coordinate(Double.parseDouble(splitStr[2]),Double.parseDouble(splitStr[3]));

		Polygon other = thelayer.getGeometryFactory().createPolygon(thelayer.getGeometryFactory().createLinearRing(coordinates),null);
		SearchInRelation search = new SearchInRelation(other, "T*****T**");
		
		thelayer.getIndex().executeSearch(search);
		
		Set<Node> returnSet = new HashSet<Node>();
		for(SpatialDatabaseRecord tempSDBR: search.getResults())
		{
			returnSet.add(tempSDBR.getGeomNode());
		}
		
		return returnSet;
	}

}
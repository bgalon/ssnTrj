package ac.technion.geoinfo.ssnTrj;

import java.util.Collection;

import org.neo4j.graphdb.RelationshipType;

import ac.technion.geoinfo.ssnTrj.domain.NodeWrapper;
import ac.technion.geoinfo.ssnTrj.domain.Route;
import ac.technion.geoinfo.ssnTrj.domain.RouteImpl;
import ac.technion.geoinfo.ssnTrj.domain.SpatialEntity;
import ac.technion.geoinfo.ssnTrj.domain.SpatialEntityImpl;
import ac.technion.geoinfo.ssnTrj.domain.TimePatternRelation;
import ac.technion.geoinfo.ssnTrj.query.SSNbfsQuery;
import ac.technion.geoinfo.ssnTrj.query.SSNquery;

public class ObjectTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final String dbPath = "C:\\neo4j-enterprise-1.4\\data\\graph.db";
		SSNonGraph testSSN = null;
		try
		{
			testSSN = new SSNonGraph(dbPath);
			SSNbfsQuery testQuery = new SSNbfsQuery(testSSN); 
//			Collection<NodeWrapper> users = testQuery.Select("social", "*Alondra1*"); //user[11]
//			Collection<NodeWrapper> routes = testQuery.Move(users, new RelationshipType[]{TimePatternRelation.TimePattren}, null);
//			
//			Collection<NodeWrapper> A = testQuery.Select(source, theQuery)
			Route route = new RouteImpl(testSSN.getNodeById(2694));
			SpatialEntity A = new SpatialEntityImpl(testSSN.getNodeById(690));
			SpatialEntity B = new SpatialEntityImpl(testSSN.getNodeById(523));
//			System.out.println(testQuery.findRoutesPassThrow(route, A, B));
			
//			for(NodeWrapper tempNode:routes)
//			{
//				System.out.println(tempNode);
//				if(tempNode instanceof Route)
//				{
//					
//				}
//			}
				
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally
		{
			if(testSSN != null)
				testSSN.Dispose();
		}
	}

}

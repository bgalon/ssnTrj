package ac.technion.geoinfo.ssnTrj;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import ac.technion.geoinfo.ssnTrj.domain.NodeWrapper;
import ac.technion.geoinfo.ssnTrj.domain.Route;
import ac.technion.geoinfo.ssnTrj.domain.SocialRelation;
import ac.technion.geoinfo.ssnTrj.domain.SpatialEntity;
import ac.technion.geoinfo.ssnTrj.domain.SpatialEntityImpl;
import ac.technion.geoinfo.ssnTrj.domain.SpatialRelation;
import ac.technion.geoinfo.ssnTrj.domain.User;
import ac.technion.geoinfo.ssnTrj.domain.UserImpl;
import ac.technion.geoinfo.ssnTrj.generator.RouteGenerator;
import ac.technion.geoinfo.ssnTrj.generator.UserGenerator;
import ac.technion.geoinfo.ssnTrj.query.SSNbfsQuery;
import ac.technion.geoinfo.ssnTrj.query.SSNquery;


public class DataGenerator {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		final String dbPath = "C:\\graphDBEx";
		final String namesFile = "C:\\Users\\Ben\\Documents\\nameList.txt";
		final String profssionFile = "C:\\Users\\Ben\\Documents\\profssionList.txt";
		final String hobbiesFile = "C:\\Users\\Ben\\Documents\\hobbiesList.txt";
		
//		SSN testSSN = null;
//		try
//		{
			for(int i = 1; i <= 10; i++)
			{
				SSN testSSN = null;
				try
				{
					String path = dbPath + "\\" + i;
//					BuildSaptialTestDB(path);
					testSSN = new SSNonGraph(path);
					System.out.println("start worknig on " + i);
					UserGenerator uGen = new UserGenerator(testSSN);
					uGen.GenerateUsers(namesFile, profssionFile, hobbiesFile, i);
					System.out.println("done soical on " + i);
					uGen.GenerateRandomPattenAndRotes(i*3, 2,path + "\\routeReslut.txt");
					System.out.println("done route on " + i);
					
					Index<Node> theInd = ((SSNonGraph)testSSN).getNodeIndex("type");
					IndexHits<Node> indResult = theInd.get("type", "buliding");
					System.out.println("in db " + i + " there are " + indResult.size() +" bulidings");
					
					indResult = theInd.get("type", "roadSegment");
					System.out.println("in db " + i + " there are " + indResult.size() +" roadSegments");
					
					indResult = theInd.get("type", "user");
					System.out.println("in db " + i + " there are " + indResult.size() +" users");
					
					indResult = theInd.get("type", "route");
					System.out.println("in db " + i + " there are " + indResult.size() +" routes");
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
			
//			BuildSaptialTestDB(dbPath);
//			System.out.println("Done Spatial");
//			
//			testSSN = new SSNonGraph(dbPath);
//			UserGenerator uGen = new UserGenerator(testSSN);
//			uGen.GenerateUsers(namesFile, profssionFile, hobbiesFile, 1);
//			System.out.println("Done Social");
//			
//			uGen.GenerateRandomPattenAndRotes(3, 1);
//			uGen.GenerateHomeWorkPattenAndRotes();
			
//			Collection<User> users = BuildSocailTestDB(testSSN);
//			BuildLifePattern(testSSN,users); 
			
//			SSNquery testQuery = new SSNbfsQuery(testSSN);
//			SpatialEntity start = new SpatialEntityImpl((testQuery.Select("spatial", "*67599549*").iterator().next()));
//			SpatialEntity end = new SpatialEntityImpl((testQuery.Select("spatial", "*67599585*").iterator().next()));
//			System.out.println(start);
//			System.out.println(end);
//			SpatialEntity[] testRouteArray = RouteGenerator.routeFind(start, end);
//			Route testRoute = testSSN.addRoute(start, end, testRouteArray);
//			System.out.println(testRoute.PrintRoute());
//			
//			Set<NodeWrapper> tempColl = new HashSet<NodeWrapper>();
//			tempColl.add(start);
//			Collection<NodeWrapper> testQ = testQuery.Move((Collection<NodeWrapper>)tempColl, 
//					new RelationshipType[] {SpatialRelation.startAt}, new String[]{null});
//	
//			System.out.println("reslut");
//			for(NodeWrapper tempNodeW:testQ)
//			{
//				System.out.println(tempNodeW);
//			}
			
//			Collection<NodeWrapper> testQ2 = testQuery.Extend(testQ, 
//					new RelationshipType[] {SpatialRelation.include}, new String[]{null});
//			
//			System.out.println("reslut");
//			for(NodeWrapper tempNodeW:testQ2)
//			{
//				System.out.println(tempNodeW);
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//		finally
//		{
//			if(testSSN != null)
//				testSSN.Dispose();
//		}
	}

	private static void BuildSaptialTestDB(String dbPath) throws Exception
	{
//		String osmFileNPath = "C:\\osmData\\Washington-border1.osm";
//		String osmFileNPath = "C:\\osmData\\Washington_varySmall.osm";
		String osmFileNPath = "C:\\osmData\\Washington_testCase.osm";
//		String osmFileNPath = "C:\\osmData\\AmericanUniversity.osm";
		OSMimpoter myOsmImporter = new OSMimpoter(osmFileNPath,dbPath);
		myOsmImporter.ImportRoads();
		myOsmImporter.ImportBulidings();
		myOsmImporter.Dispose();
//		System.out.println("Done Spatial");
	}
	
	private static Collection<User> BuildSocailTestDB(SSN testSSN) throws Exception
	{
		Set<User> returnSet = new HashSet<User>();
		returnSet.add(testSSN.AddUser("u1", null, null, new String[]{"Name"}, new String[]{"U1"}));
		returnSet.add(testSSN.AddUser("u2",new String[]{"u1"}, new String[]{"Friend"},
				new String[]{"Name"}, new String[]{"U2"}));
		System.out.println("Done Social");
		return returnSet;
	}
	
	private static void BuildLifePattern(SSN testSSN, Collection<User> users) throws Exception
	{
		SSNquery testQuery = new SSNbfsQuery(testSSN);
		Iterator<User> userIter = users.iterator();
		SpatialEntity start1 = new SpatialEntityImpl((testQuery.Select("spatial", "*67599549*").iterator().next()));
		User user1 = userIter.next();
		testSSN.addPattren(user1, start1, "D;;17;5", 0.9);
		SpatialEntity start2 = new SpatialEntityImpl((testQuery.Select("spatial", "*67599585*").iterator().next()));
		testSSN.addPattren(userIter.next(), start2, "D;;17;5", 0.9);
		System.out.println("Done Pattern");
	}
}

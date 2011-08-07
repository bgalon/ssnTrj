package ac.technion.geoinfo.ssnTrj;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import ac.technion.geoinfo.ssnTrj.domain.NodeWrapper;
import ac.technion.geoinfo.ssnTrj.domain.NodeWrapperImpl;
import ac.technion.geoinfo.ssnTrj.domain.SocialRelation;
import ac.technion.geoinfo.ssnTrj.domain.SpatialEntity;
import ac.technion.geoinfo.ssnTrj.domain.SpatialEntityImpl;
import ac.technion.geoinfo.ssnTrj.domain.TimePatternRelation;
import ac.technion.geoinfo.ssnTrj.query.SSNbfsQuery;
import ac.technion.geoinfo.ssnTrj.query.SSNquery;

public class Ex2 {

	private static final Random ranGen = new Random();
	
	public static void main(String[] args) {
		final String dbPath = "C:\\graphDBEx\\1";
		for(int i = 5; i <= 5; i++)
		{
			SSN testSSN = null;
			try
			{
				String path = dbPath + "_" + 1 + "_" + i;
				testSSN = new SSNonGraph(path);
				NodeWrapper ranUser = getRandomUser(testSSN);
				SSNquery testQury = new SSNbfsQuery(testSSN);
				SpatialEntity A = getRandomLocByType("*building*", testQury);
				SpatialEntity B = getRandomLocByType("*building*", testQury);
				
				long ranTime = FindLocPassByUser(ranUser, A, B, testQury);
				System.out.println(ranTime);
				
				ranTime = FindLocPassByUser(ranUser, A, B, testQury);
				System.out.println(ranTime);
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
	
	private static long FindLocPassByUser(NodeWrapper theUser, SpatialEntity A, SpatialEntity B, SSNquery query) throws Exception
	{
		Set<NodeWrapper> source = new HashSet<NodeWrapper>();
		source.add(theUser);
		long start = System.nanoTime();   
		Collection<NodeWrapper> q1 = query.Move(source, new RelationshipType[]{SocialRelation.Family, SocialRelation.Friend},null);
		Collection<NodeWrapper> q2 = query.Move(q1, new RelationshipType[]{TimePatternRelation.tpToRoute},null);
		Collection<NodeWrapper> q3 = ((SSNbfsQuery)query).RoutesPassThrow(q2, A, B);
		long elapsedTime = System.nanoTime() - start;
		
		System.out.println(theUser);
		System.out.println(A);
		System.out.println(B);
		System.out.println(q1);
		System.out.println(q2);
		System.out.println(q3);
		
		return elapsedTime;
	}
	
//	private static long FindLocPassByUser2(NodeWrapper theUser, SpatialEntity A, SpatialEntity B, SSNquery query) throws Exception
//	{
//		Set<NodeWrapper> source = new HashSet<NodeWrapper>();
//		source.add(theUser);
//		long start = System.nanoTime();
//		Envelope tempEnv = (A.getGeometry().union(B.getGeometry())).getEnvelopeInternal();
//		
//		
//		long elapsedTime = System.nanoTime() - start;
//		
//		System.out.println(theUser);
//		System.out.println(A);
//		System.out.println(B);
//		System.out.println(q1);
//		System.out.println(q2);
//		System.out.println(q3);
//		
//		return elapsedTime;
//	}
	
	
	private static SpatialEntity getRandomLocByType(String type,SSNquery ssnQ) throws Exception
	{
		Collection<NodeWrapper> locList = ssnQ.Select("spatial", type);
		if (locList.isEmpty()) return null;
		int randomLoc = ranGen.nextInt(locList.size());
		return new SpatialEntityImpl(((locList.toArray(new NodeWrapperImpl[0]))[randomLoc]));
		
	}
	private static NodeWrapper getRandomUser(SSN ssn) throws Exception
	{
		Index<Node> theInd = ((SSNonGraph)ssn).getNodeIndex("type");
		IndexHits<Node> indResult = theInd.get("type", "user");
		int randomUser = ranGen.nextInt(indResult.size());
		for(int i = 0; i < randomUser - 1; i++)
		{
			indResult.next();
		}
		return new NodeWrapperImpl(indResult.next()); 
	}

}

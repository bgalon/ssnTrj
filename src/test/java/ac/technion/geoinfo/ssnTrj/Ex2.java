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

	private static final int MAX_I = 8;
	private static final int MAX_J = 20;
	private static final int MAX_K = 10;
	private static final Random ranGen = new Random();
	
	public static void main(String[] args) {
		final String dbPath = "C:\\graphDBEx\\1";
		System.out.println("Start ex2");
		for(int i = 1; i <= MAX_I; i++)
		{
			SSN testSSN = null;
			String path = dbPath + "_" + 1 + "_" + i;
			System.out.println(path);
			try
			{
				testSSN = new SSNonGraph(path);
				SSNquery testQury = new SSNbfsQuery(testSSN);
				long[][] result = new long[MAX_J][MAX_K];
				long[][] result2 = new long[MAX_J][MAX_K];
				for(int j = 0; j < MAX_J; j++ ){
					NodeWrapper ranUser = getRandomUser(testSSN);
					SpatialEntity A = getRandomLocByType("*building*", testQury);
					SpatialEntity B = getRandomLocByType("*building*", testQury);
					for (int k = 0; k < MAX_K; k++)
					{
						System.out.println("Social --- > Spatial");
						long ranTime = FindLocPassByUser(ranUser, A, B, testQury);
						result[j][k] = ranTime;
//						System.osut.println(ranTime);
						System.out.println("Spatial --- > Social");
						ranTime = FindLocPassByUser2(ranUser, A, B, testQury);
						result2[j][k] = ranTime;
					}
				}
				System.out.println("Social --- > Spatial");
				for(int j = 0; j < MAX_J; j++){
					for (int k = 0; k < MAX_K; k++)
						System.out.print(result[j][k] + ",");
					System.out.println();
				}
				System.out.println("Spatial --- > Social");
				for(int j = 0; j < MAX_J; j++){
					for (int k = 0; k < MAX_K; k++)
						System.out.print(result2[j][k] + ",");
					System.out.println();
				}
				
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
		System.out.println("Done ex2");
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
	
	private static long FindLocPassByUser2(NodeWrapper theUser, SpatialEntity A, SpatialEntity B, SSNquery query) throws Exception
	{
		Set<NodeWrapper> source = new HashSet<NodeWrapper>();
		source.add(theUser);
		long start = System.nanoTime();
		Envelope tempEnv = (A.getGeometry().union(B.getGeometry())).getEnvelopeInternal();
		//the theEnvelope = "MaxX,MaxY,MinX,MinY"
		String envASstring = tempEnv.getMaxX() + "," + tempEnv.getMaxY() + "," + tempEnv.getMinX() + "," + tempEnv.getMinY();
		Collection<NodeWrapper> q1 = query.Select("spatial", "in:" + envASstring + "@layer:route");
		Collection<NodeWrapper> q2 = ((SSNbfsQuery)query).RoutesPassThrow(q1, A, B);
		Collection<NodeWrapper> q3 = query.Move(source, new RelationshipType[]{SocialRelation.Family, SocialRelation.Friend},null);
		Collection<NodeWrapper> q4 = query.Move(q3, new RelationshipType[]{TimePatternRelation.tpToRoute},null);
		Collection<NodeWrapper> q5 = query.Intersect(q4, q2);
		long elapsedTime = System.nanoTime() - start;
		
		System.out.println(theUser);
		System.out.println(A);
		System.out.println(B);
		System.out.println(q1);
		System.out.println(q2);
		System.out.println(q3);
		System.out.println(q4);
		System.out.println(q5);
		
		return elapsedTime;
	}
	
	
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

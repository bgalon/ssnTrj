package ac.technion.geoinfo.ssnTrj;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import ac.technion.geoinfo.ssnTrj.domain.NodeWrapper;
import ac.technion.geoinfo.ssnTrj.domain.NodeWrapperImpl;
import ac.technion.geoinfo.ssnTrj.domain.SocialRelation;
import ac.technion.geoinfo.ssnTrj.domain.SpatialRelation;
import ac.technion.geoinfo.ssnTrj.domain.Static;
import ac.technion.geoinfo.ssnTrj.domain.TimePatternRelation;
import ac.technion.geoinfo.ssnTrj.query.SSNbfsQuery;
import ac.technion.geoinfo.ssnTrj.query.SSNquery;

public class Ex1 implements Static {

	private static final int MAX_I = 8;
	private static final int MAX_J = 20;
	private static final int MAX_K = 10;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final String dbPath = "C:\\graphDBEx\\1";
		System.out.println("Start ex1");
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
				for(int j = 0; j < MAX_J; j++ ){
					NodeWrapper ranUser = getRandomUser(testSSN);
					for (int k = 0; k < MAX_K; k++)
					{
						System.out.println("i=" + i + ", j=" + j + ", k=" + k);
						
						long ranTime =  FindSimiler(ranUser, testQury);
						result[j][k] = ranTime;
	//					System.out.println(ranTime);
	//					ranTime =  FindSimiler(ranUser, testQury);
	//					System.out.println(ranTime);
					}
				}
				for(int j = 0; j < MAX_J; j++){
					for (int k = 0; k < MAX_K; k++)
						System.out.print(result[j][k] + ",");
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
		System.out.println("Done ex1");
	}
	
	private static long FindSimiler(NodeWrapper theUser, SSNquery query) throws Exception
	{
		Set<NodeWrapper> source = new HashSet<NodeWrapper>();
		source.add(theUser);
		long start = System.nanoTime();   
		Collection<NodeWrapper> q1 = query.Move(source, new RelationshipType[]{TimePatternRelation.tpToRoute},null);
		Collection<NodeWrapper> q2 = query.Move(q1, new RelationshipType[]{SpatialRelation.include},null);
		Collection<NodeWrapper> q3 = ((SSNbfsQuery)query).MultiMove(q2, new RelationshipType[]{SpatialRelation.include},null,0.3);
		Collection<NodeWrapper> q4 = query.Move(q3, new RelationshipType[]{TimePatternRelation.tpToRoute},null);
		long elapsedTime = System.nanoTime() - start;
		
		System.out.println(theUser);
		System.out.println(q1);
		System.out.println(q2);
		System.out.println(q3);
		System.out.println(q4);
		
		return elapsedTime;
	}
	
	private static NodeWrapper getRandomUser(SSN ssn) throws Exception
	{
		Index<Node> theInd = ((SSNonGraph)ssn).getNodeIndex("type");
		IndexHits<Node> indResult = theInd.get("type", "user");
		Random ranGen = new Random();
		int randomUser = ranGen.nextInt(indResult.size());
		for(int i = 0; i < randomUser - 1; i++)
		{
			indResult.next();
		}
		return new NodeWrapperImpl(indResult.next()); 
	}
}

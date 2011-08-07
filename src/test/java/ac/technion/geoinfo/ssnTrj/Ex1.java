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

	/**
	 * @param args
	 */
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
				long ranTime =  FindSimiler(ranUser, testQury);
				System.out.println(ranTime);
				ranTime =  FindSimiler(ranUser, testQury);
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

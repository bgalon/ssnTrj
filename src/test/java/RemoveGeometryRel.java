import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.neo4j.gis.spatial.SpatialRelationshipTypes;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;


import ac.technion.geoinfo.ssnTrj.SSNonGraph;


public class RemoveGeometryRel {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		final String dbPath = "C:\\neo4j-enterprise-1.5\\data\\graph.db";
		
		SSNonGraph ssn = new SSNonGraph(dbPath); 
		int counter = 0;
		int oldCounter = 0;
		Node mainNode = ssn.getNodeById(2);
		Set<Relationship> ToDelete = new HashSet<Relationship>();
		for (Relationship tempRel:mainNode.getRelationships(SpatialRelationshipTypes.GEOMETRIES))
		{
			ToDelete.add(tempRel);
			counter ++;
			if (counter > oldCounter + 1000)
			{
				System.out.println(counter + " relationships found");
				oldCounter = counter;
			}
		}
		System.out.println(counter + " relationships found to delete");
		Iterator<Relationship> toDelIter = ToDelete.iterator();
		counter = 0;
		oldCounter = 0;
		while (toDelIter.hasNext())
		{
			Del5000(toDelIter, ssn);
		}
		ssn.Dispose();
		
	}
	
	static private void Del5000(Iterator<Relationship> theIter,SSNonGraph ssn) throws Exception
	{
		int counter = 0;
		Transaction tx = ssn.getGDB().beginTx(); 
		try
		{
			while (counter < 5000 && theIter.hasNext())
			{
				theIter.next().delete();
				counter++;
			}
			tx.success();
			System.out.println(counter + " relationships deleted permantlly");
		}
		catch (Exception e) {
			// TODO: handle exception
			tx.failure();
			System.out.println("Error whlie delete relationship number " + counter);
			throw e;
			//e.printStackTrace();
		}
		finally
		{
			tx.finish();
		}
	}
}

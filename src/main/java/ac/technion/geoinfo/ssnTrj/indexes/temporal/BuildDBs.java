package ac.technion.geoinfo.ssnTrj.indexes.temporal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import ac.technion.geoinfo.ssnTrj.domain.NodeWrapper;
import ac.technion.geoinfo.ssnTrj.domain.NodeWrapperImpl;
import ac.technion.geoinfo.ssnTrj.domain.Static;

public class BuildDBs implements Static{
	//properties
	final static String USER_ID = "user_id";
	final static String LOCATION_ID = "location_id";
	
	//indexes
	final static String THE_INDEX ="main_index";
	final static String USERS_KEY ="users";
	final static String LOCATION_KEY ="locations";

	NodeWrapper[][] users;
	NodeWrapper[][] locations;
	
	GraphDatabaseService[] gdbs;
	List<Index<Node>> indexes = new ArrayList<Index<Node>>();
	List<Constructor<TemporalIndex>> constructors = new ArrayList<Constructor<TemporalIndex>>();
	
	public BuildDBs(String[] dbsPaths, Class<TemporalIndex>[] timeIndexClasses, int usersNum, int locNumber) throws NoSuchMethodException, SecurityException{
		
		users = new NodeWrapper[dbsPaths.length][usersNum];
		locations = new NodeWrapper[dbsPaths.length][locNumber];
		gdbs = new EmbeddedGraphDatabase[dbsPaths.length];
		
		for(int i = 0; i < dbsPaths.length ;i++){
			gdbs[i] = new EmbeddedGraphDatabase(dbsPaths[i]);
			constructors.add(i, timeIndexClasses[i].getConstructor(new Class[] {Node.class}));
		}
	}
	
	public void BuildUsersNLocations() throws Exception{
		
		Transaction[] txs = new Transaction[gdbs.length];
		
		for(int i = 0; i < gdbs.length ;i++){
			txs[i] = gdbs[i].beginTx();
		}
		try{
			//build indexs
			Node[] userRef  = new Node[gdbs.length];
			for(int i = 0; i < gdbs.length ;i++){
				indexes.add(i, gdbs[i].index().forNodes(THE_INDEX));
				Node tempUserRefNode = gdbs[i].createNode();
				userRef[i] = (gdbs[i].getReferenceNode().createRelationshipTo(tempUserRefNode, TestTIndexRel.user_ref)).getEndNode();
			}
			
			//build users
			for (int i = 0; i < users[0].length; i++){
				for (int j = 0; j < gdbs.length ; j++){
					NodeWrapper tempNode = new NodeWrapperImpl(gdbs[j].createNode());
					users[j][i] = tempNode;
					tempNode.setProperty(SSN_TYPE, USER);
					tempNode.setProperty(USER_ID, i);
					indexes.get(j).add(tempNode, USERS_KEY, i);
					
					userRef[j].createRelationshipTo(tempNode, TestTIndexRel.user_ref);
				}
			}
			
			//build location
			for (int i = 0; i < locations[0].length; i++){
				for (int j = 0; j < gdbs.length ; j++){
					NodeWrapper tempNode = new NodeWrapperImpl(gdbs[j].createNode());
					locations[j][i] = tempNode;
					tempNode.setProperty(SSN_TYPE, BULIDING);
					tempNode.setProperty(LOCATION_ID, i);
					indexes.get(j).add(tempNode, LOCATION_KEY, i);
				}
			}
			
			for(int i = 0; i < txs.length ;i++){
				txs[i].success();
			}
			
		}catch(Exception e){
			for(int i = 0; i < txs.length ;i++){
				txs[i].failure();
			}
			throw e;
		}finally{
			for(int i = 0; i < txs.length ;i++){
				txs[i].finish();
			}
		}	
	}
	
	public void PrintDBreport() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		TemporalIndex tempIndex;
		for (int i = 0; i < users[0].length; i++){
			for (int j = 0; j < gdbs.length ; j++){
				tempIndex = constructors.get(j).newInstance(new Object[]{users[j][i]});
				
				System.out.println("user: " + i + " db: " + j + " number of nodes: " + tempIndex.NumberOfLeaves()
						+ " strat at: " + new Date(tempIndex.getStartTime()) + "(" + tempIndex.getStartTime() + ")"
						+ " end time: " + new Date(tempIndex.getEndTime()) + "(" + tempIndex.getEndTime() + ")");
			}
		}
		
		for (int i = 0; i < locations[0].length; i++){
			for (int j = 0; j < gdbs.length ; j++){
				tempIndex = constructors.get(j).newInstance(new Object[]{locations[j][i]});
				
				System.out.println("location: " + i + " db: " + j + " number of nodes: " + tempIndex.NumberOfLeaves()
						+ " strat at: " + new Date(tempIndex.getStartTime()) + "(" + tempIndex.getStartTime() + ")"
						+ " end time: " + new Date(tempIndex.getEndTime()) + "(" + tempIndex.getEndTime() + ")");
			}
		}
	}
	
	public void BuildTimesForUsers(int startUser, int endUser, int startLoc, int endLoc, long startTime, long endTime) throws Exception{
		int i = startUser;
		while(i <= endUser){
			BuildTimes(i, startLoc, endLoc, startTime, endTime);
			i++;
		}
		
	}
	
	private void BuildTimes(int user, int startLoc, int endLoc, long startTime, long endTime) throws Exception{
		long time = startTime;
		int loc = startLoc;
		
		while(time < endTime){
			for (int j = 0; j < gdbs.length ; j++){
				NodeWrapper tempUser = users[j][user];
				NodeWrapper tempHomeLoc = locations[j][loc];
				TemporalIndex userIndex = constructors.get(j).newInstance(new Object[]{tempUser});
				TemporalIndex locationIndex = constructors.get(j).newInstance(new Object[]{tempHomeLoc});
				
				userIndex.Add(tempHomeLoc, time, time + 1000 * 60 * 50);
				locationIndex.Add(tempUser, time, time + 1000 * 60 * 50);
			}
			
			loc++;
			if (loc > endLoc) loc = startLoc;
			time = time + 1000 * 60 * 60;
		}	
	}
	
	public void Dispose(){
		for(int i = 0; i < gdbs.length ;i++){
			gdbs[i].shutdown();
		}
	}
	
	protected void finalize() {
		Dispose();
	}
	
}

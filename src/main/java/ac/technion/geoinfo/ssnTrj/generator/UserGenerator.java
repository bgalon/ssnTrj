package ac.technion.geoinfo.ssnTrj.generator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import ac.technion.geoinfo.ssnTrj.domain.TimePattern;
import ac.technion.geoinfo.ssnTrj.SSN;
import ac.technion.geoinfo.ssnTrj.SSNonGraph;
import ac.technion.geoinfo.ssnTrj.domain.NodeWrapperImpl;
import ac.technion.geoinfo.ssnTrj.domain.NodeWrapper;
import ac.technion.geoinfo.ssnTrj.domain.Route;
import ac.technion.geoinfo.ssnTrj.domain.SocialRelation;
import ac.technion.geoinfo.ssnTrj.domain.SpatialEntity;
import ac.technion.geoinfo.ssnTrj.domain.SpatialEntityImpl;
import ac.technion.geoinfo.ssnTrj.domain.TimePatternImpl;
import ac.technion.geoinfo.ssnTrj.domain.User;
import ac.technion.geoinfo.ssnTrj.domain.UserImpl;
import ac.technion.geoinfo.ssnTrj.query.SSNbfsQuery;
import ac.technion.geoinfo.ssnTrj.query.SSNquery;

public class UserGenerator {
	
	private final SSN ssn;
	
	private final Random ranGen = new Random();
	
	private static final int MINIMUM_FRIENDS = 1;
	private static final int MAXIMUM_FRIENDS = 30; 
	
	private static final int MINIMUM_HOBBIES = 1;
	private static final int MAXIMUM_HOBBIES = 5; 
	
	private static final int MINIMUM_PATTERN = 10;
	private static final int MAXIMUM_PATTERN = 30; 
	
	private List<NodeWrapper> userNameLst;
	
	public UserGenerator(SSN theSSN)
	{
		this.ssn = theSSN;
	}
	
	public List<NodeWrapper> GenerateUsers(String namesFile, String profssionFile, String hobbiesFile, double l) throws Exception{
		//"uName,fullName,profession,(hobbies),(friendList)"
		
		List<String> nameList = FileToList(namesFile);
		List<String> fullNameList = FileToList(namesFile);
		List<String> profssionList = FileToList(profssionFile);
		List<String> hobbiesList = FileToList(hobbiesFile);
//		List<String> usedNameList = new LinkedList<String>();
//		List<String> persList = new LinkedList<String>();
//		userNameLst = new LinkedList<NodeWrapper>();
		List<String> usedNameList = new ArrayList<String>();
		List<String> persList = new ArrayList<String>();
		userNameLst = new ArrayList<NodeWrapper>();
		
		Random ranGen = new Random();
		
		while(!nameList.isEmpty()){
			int ranName = ranGen.nextInt(nameList.size());
			for (int k = 1; k <= l; k++){
//				if ((ranGen.nextInt(100)/100) > l ) 
//					continue;
				int ranSeconedName = ranGen.nextInt(fullNameList.size());
				String perStr = new String();
				perStr = nameList.get(ranName) + k + ",";
				
				perStr = perStr + nameList.get(ranName) + " " + fullNameList.get(ranSeconedName) + ",";
				
				int ranProfssion = ranGen.nextInt(profssionList.size());
				perStr = perStr + profssionList.get(ranProfssion) + ",";
				
				int numOfHoobies = MINIMUM_HOBBIES + ranGen.nextInt(MAXIMUM_HOBBIES);
//				List<String> tempHobbiesList = new LinkedList<String>();
				List<String> tempHobbiesList = new ArrayList<String>();
				perStr = perStr + "(";
				for(int i = 0; i <= numOfHoobies; i++ ){
					int hobbyNum = ranGen.nextInt(hobbiesList.size());
					perStr = perStr + hobbiesList.get(hobbyNum) + ",";
					tempHobbiesList.add(hobbiesList.remove(hobbyNum));
				}
				perStr = perStr.substring(0, perStr.length()-1);
				hobbiesList.addAll(tempHobbiesList);
				
				int numOfFriend = (int)(MINIMUM_FRIENDS*l + ranGen.nextInt((int)(MAXIMUM_FRIENDS*l)));
				numOfFriend = Math.min(numOfFriend, usedNameList.size());
				perStr = perStr + "),( ";
//				List<String> friendsList = new LinkedList<String>();
				List<String> friendsList = new ArrayList<String>();
				for(int i = 0; i < numOfFriend; i++ ){
					int friendNum = ranGen.nextInt(usedNameList.size());
					perStr = perStr + usedNameList.get(friendNum) + ",";
					friendsList.add(usedNameList.remove(friendNum));
				}
				perStr = perStr.substring(0, perStr.length()-1) + ")";
				usedNameList.addAll(friendsList);
				usedNameList.add(nameList.get(ranName) + k); 
//				persList.add(perStr);
				
				String[] tempSplit = perStr.split("\\(");
				String[] hoobyData = tempSplit[0].substring(0, tempSplit[0].trim().length()-1).split("\\,");
				String[] tempFriends = tempSplit[2].trim().substring(0, tempSplit[2].trim().length()-1).split("\\,");
				String uName = hoobyData[0].trim();
				String fName = hoobyData[1].trim();
				String profession = hoobyData[2].trim();
				String hobbies = tempSplit[1].substring(0, tempSplit[1].trim().length()-2);
				String[] friends = new String[tempFriends.length];
				String[] friendsRelType = new String[tempFriends.length];
				for(int i = 0; i < tempFriends.length; i++ ){
					friends[i] = tempFriends[i].trim();
					int ftr = ranGen.nextInt(100);
					if(ftr < 70)
					{
						friendsRelType[i] = "Friend";
					}
					else
					{
						friendsRelType[i] = "Family";
					}
				} 
				if (friends.length == 0) friends = null;
				
				//"uName,fullName,profession,(hobbies),(friendList)"
				userNameLst.add(ssn.AddUser(uName, friends, friendsRelType, new String[]{"profession","hobbies"},
						new String[]{profession, hobbies}));
				//SSNDomain.createUser(graphDB, uName, fName, profession, hobbies, friends);
				//System.out.println(uName + " , " + fName + "has been added");
			}
			nameList.remove(ranName);
		}
		
//		for (String tempPer:persList)
//		{
//			String[] tempSplit = tempPer.split("\\(");
//			String[] hoobyData = tempSplit[0].substring(0, tempSplit[0].trim().length()-1).split("\\,");
//			String[] tempFriends = tempSplit[2].trim().substring(0, tempSplit[2].trim().length()-1).split("\\,");
//			String uName = hoobyData[0].trim();
//			String fName = hoobyData[1].trim();
//			String profession = hoobyData[2].trim();
//			String hobbies = tempSplit[1].substring(0, tempSplit[1].trim().length()-2);
//			String[] friends = new String[tempFriends.length];
//			String[] friendsRelType = new String[tempFriends.length];
//			for(int i = 0; i < tempFriends.length; i++ ){
//				friends[i] = tempFriends[i].trim();
//				int ftr = ranGen.nextInt(100);
//				if(ftr < 70)
//				{
//					friendsRelType[i] = "Friend";
//				}
//				else
//				{
//					friendsRelType[i] = "Family";
//				}
//			} 
//			if (friends.length == 0) friends = null;
//			
//			//"uName,fullName,profession,(hobbies),(friendList)"
//			userNameLst.add(ssn.AddUser(uName, friends, friendsRelType, new String[]{"profession","hobbies"},
//					new String[]{profession, hobbies}));
//			//SSNDomain.createUser(graphDB, uName, fName, profession, hobbies, friends);
//			//System.out.println(uName + " , " + fName + "has been added");
//		}
		return userNameLst;
	}
	
	public Set<Route> GenerateHomeWorkPattenAndRotes() throws Exception
	{
		if (userNameLst.isEmpty()) 
			throw new Exception("the user list in empty. ran userGenerator first");
		Set<Route> retrunSet = new HashSet<Route>();
		for(NodeWrapper tempUser:userNameLst){
			System.out.println();
			//patterns for person
			SpatialEntity home = null;
			SpatialEntity office = null;
			//night pattern for a house
			int percent = 1 + ranGen.nextInt(100);
			if (percent < 97)
			{
				String thePattern = makeDailyPatternAsTP(4).toString();
				double confi = ((double)0.8 + (double)ranGen.nextInt(18)/100);
				NodeWrapper tempNodew = getRandomLocByType("*building*");
				if (tempNodew == null) continue;
				home = new SpatialEntityImpl(tempNodew);
				
					ssn.addPattren((User)tempUser, home, thePattern, confi);
				//SSNDomain.createPattern(graphDB, perStr,locStr , thePattern);
				System.out.println(tempUser + "---" + thePattern + "---->" + home);
			}
			
			//day pattern for work
			percent = 1 + ranGen.nextInt(100);
			int endTime = 13;
			if (percent < 80){
				percent = 1 + ranGen.nextInt(100);
				if (percent < 60){
					endTime = 18;
				}
				String thePattern = makeWorkdayPatternAsTP(7,endTime);
				double confi = ((double)0.6 + (double)ranGen.nextInt(35)/100);
				NodeWrapper tempNodew = getRandomLocByType("*building*");
				if (tempNodew == null) continue;
				office = new SpatialEntityImpl(tempNodew);
					ssn.addPattren((User)tempUser, office, thePattern, confi);
				//SSNDomain.createPattern(graphDB, perStr,locStr , thePattern);
				System.out.println(tempUser + "---" + thePattern + "---->" + office);
			}
			
			if (home == null || office == null) continue;
			if (home == office) continue;
			
			SpatialEntity[] home2officeSegment = RouteGenerator.routeFind(home, office);
			Route home2offcieRoute = ssn.addRoute(home, office, home2officeSegment);
			double confi = ((double)0.6 + (double)ranGen.nextInt(35)/100);
			TimePattern tempP = ssn.addPattren((User)tempUser, home2offcieRoute,  makeWorkdayPatternAsTP(6,7), confi);
			System.out.println(tempUser + "---" + tempP.toString() + "---->" + office);
			retrunSet.add(home2offcieRoute);
			System.out.println(home2offcieRoute);
			System.out.println(home2offcieRoute.RouteAsString());
		}
		return retrunSet;
	}
	
	public void GenerateRandomPattenAndRotes(int avgNumOfRoute, int std, String filePath) throws Exception
	{
		if (userNameLst == null || userNameLst.isEmpty())
			fillUserList();
			//throw new Exception("the user list in empty. ran userGenerator first");
		
//		Set<Route> retrunSet = new HashSet<Route>();
		BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
		for(NodeWrapper tempUser:userNameLst)
		{
//			System.out.println();
			int numOfRoutes4User = avgNumOfRoute - std + ranGen.nextInt(std*2);
			for (int i = 0; i < numOfRoutes4User; i++)
			{
				NodeWrapper startNode = getRandomLocByType("*building*");
				if (startNode == null) continue;
				SpatialEntity start = new SpatialEntityImpl(startNode);
				
				NodeWrapper endNode = getRandomLocByType("*building*");
				if (endNode == null) continue;
				SpatialEntity end = new SpatialEntityImpl(endNode);
				
				if(start.equals(end)) continue;
				SpatialEntity[] theSegments = RouteGenerator.routeFind(start, end);
				if(theSegments == null) continue;
				
				String[] patters = makeFlowedWeeklyPattern();
				
				Route returnRoute = ssn.addRoute(start, end, theSegments);
				if (returnRoute == null) continue;
				
				ssn.addPattren((User)tempUser, start, patters[0], ((double)0.6 + (double)ranGen.nextInt(40)/100));
//				System.out.println(tempUser + "---" + patters[0] + "---->" + start);
				out.write(tempUser + "---" + patters[0] + "---->" + start);
				out.newLine();
				
				ssn.addPattren((User)tempUser, returnRoute, patters[1], ((double)0.6 + (double)ranGen.nextInt(40)/100));
//				System.out.println(returnRoute.RouteAsString());
				out.write(returnRoute.RouteAsString());
				out.newLine();
//				System.out.println(tempUser + "---" + patters[1] + "---->" + returnRoute);
				out.write(tempUser + "---" + patters[1] + "---->" + returnRoute);
				out.newLine();
				
				ssn.addPattren((User)tempUser, end, patters[2], ((double)0.6 + (double)ranGen.nextInt(40)/100));
//				System.out.println(tempUser + "---" + patters[2] + "---->" + end);
				out.write(tempUser + "---" + patters[2] + "---->" + end);
				out.newLine();
				out.newLine();
//				retrunSet.add(returnRoute);
			}
		}
		out.close();
//		return retrunSet;
	}
	
	private void fillUserList()
	{
//		userNameLst = new LinkedList<NodeWrapper>();
		userNameLst = new ArrayList<NodeWrapper>();
		Index<Node> theInd = ((SSNonGraph)ssn).getNodeIndex("type");
		IndexHits<Node> indResult = theInd.get("type", "user");
		for(Node tempNode:indResult)
		{
			userNameLst.add(new UserImpl(tempNode));
		}
	}
	
	
//	public void GeneratePatterns(double k)
//	{
//		//in a format of "frame;units;start;end@confident"
//    	//frame can be:Y - year, M - month, W - week and D - Day
//    	//units is an array with time units in the time frame
//		
//		//List<String> locsLst = FileToList(locsFile);
//		//List<String> persLst = FileToList(persFile);
//		
//		//List<String> patternLst = new LinkedList<String>();
//		
////		GraphDatabaseService graphDB = null;
////		try {
////			graphDB = new EmbeddedGraphDatabase(dataFolder);
////			Iterable<Node> persLst = getAllUsers(graphDB);
//			
//			//persons
//		if (userNameLst.isEmpty()) 
//			throw new Exception("the user list in empty. ran userGenerator first");
//		for(NodeWrapper tempUser:userNameLst){
//				//String perStr = (String) tempNode.getProperty("uName");
//				int patternNum = (int)(MINIMUM_PATTERN*k + ranGen.nextInt((int)(MAXIMUM_PATTERN*k)));
//				//patterns for person
//				
//				//night pattern for a house
//				int percent = 1 + ranGen.nextInt(100);
//				if (percent < 97)
//				{
//					String thePattern = makeDailyPatternAsTP(4).toString();
//					double confi = ((double)0.8 + (double)ranGen.nextInt(18)/100);
//					NodeWrapper theSE = getRandomLocByType("*building*");
//					if (theSE == null) continue;
//					ssn.addPattren((User)tempUser, (SpatialEntity)theSE, thePattern, confi);
//					//SSNDomain.createPattern(graphDB, perStr,locStr , thePattern);
//					System.out.println(tempUser + "---" + thePattern + "---->" + theSE);
//				}
//				
//				//day pattern for work
//				percent = 1 + ranGen.nextInt(100);
//				int endTime = 13;
//				if (percent < 80){
//					percent = 1 + ranGen.nextInt(100);
//					if (percent < 60){
//						endTime = 18;
//					}
//					String thePattern = makeWorkdayPatternAsTP(6,endTime) + "@"  + ((double)0.6 + (double)ranGen.nextInt(35)/100);
//					String locStr = getRandomLocByType(graphDB,"school");
//					if (locStr == null) continue;
//					SSNDomain.createPattern(graphDB, perStr,locStr , thePattern);
//					System.out.println(perStr + "---" + thePattern + "---->" + locStr);
//				}
//				
//				//random patterns
//				for (int i = 0; i < patternNum; i++){
//					//String locStr = locsLst.get(ranGen.nextInt(locsLst.size())).split(",")[0].trim();
//					int randomPatternType = ranGen.nextInt(4);
//					
//					TimePattren pattern = null;
//					switch (randomPatternType) {
//					case 0: pattern = makeDailyPatternAsTP(0); break;
//					case 1: pattern = makeWeeklyPatternAsTP(); break;
//					case 2: pattern = makeWorkdayPatternAsTP(); break;
//					case 3: pattern = makeWeekendPatternAsTP(); break;
//					default:
//						break;
//					}
//					double probability = (double)(30 + ranGen.nextInt(70)) / 100;
//					
//					String thePattern = pattern + "@" + probability;
//					String locStr = getRandomLocByType(graphDB,"*");
//					if (locStr == null) continue;
//					SSNDomain.createPattern(graphDB, perStr,locStr , thePattern);
//					System.out.println(perStr + "---" + thePattern + "---->" + locStr);
//				}
//			}
////		} catch (Exception e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}finally{
////			if (graphDB != null)
////				graphDB.shutdown();
////		}
//	}

	private NodeWrapper getRandomLocByType(String type) throws Exception
	{
		SSNquery ssnQ = new SSNbfsQuery(ssn);
		Collection<NodeWrapper> locList = ssnQ.Select("spatial", type);
		if (locList.isEmpty()) return null;
		int randomLoc = ranGen.nextInt(locList.size());
		return ((locList.toArray(new NodeWrapperImpl[0]))[randomLoc]);
		
	}
	
	
	
	//*****************************************************************************
	
	private String makeDailyPatternAsTP(int startBase) throws Exception{
		//in a string format of "frame;units;start;end"
		//frame can be:Y - year, M - month, W - week and D - Day
		//units an array with time units in the time frame 
		Random ranGen = new Random();
		int percent = 1 + ranGen.nextInt(100);
		int base;
		if ( startBase >= 1 && startBase <= 4) {
			base = startBase;
		}
		else {
			if (percent <= 30)
				base = 1; //morning
			else if (percent <= 60)
				base = 2; //noon
			else if (percent <= 90)
				base = 3; //evening
			else 
				base = 4; //night
		}
		String stringRe = "D;;" + AddStartEndTime(base); 
		return stringRe;
	}
		
	private String makeJoggingPatternAsTP(int startTime) throws Exception
	{
		Random ranGen = new Random();
		int joggTime = 1 + ranGen.nextInt(3);
		String stringRe = "W;1";
		for (int i = 2; i <= 7; i++){
			int percent = ranGen.nextInt(100);
			if (percent <= 60)
				stringRe = stringRe + i + ",";
		}
		stringRe = stringRe.substring(0, stringRe.length() - 1);
		stringRe = stringRe + ";" + startTime + ";" + startTime + joggTime;
		return stringRe;
	}
		
	private String makeWorkdayPatternAsTP() throws Exception
	{
		//in a format of "frame;units;start;end"
		Random ranGen = new Random();
		int percent = 1 + ranGen.nextInt(100);
		int base;
		if (percent <= 60)
			base = 1; //morning
		else if (percent <= 80)
			base = 2; //noon
		else if (percent <= 95)
			base = 3; //evening
		else 
			base = 4; //night
		
		String stringRe = "W;1,2,3,4,5;" + AddStartEndTime(base); 		
		return stringRe;
	}
		
	private String makeWorkdayPatternAsTP(int startTime, int endTime) throws Exception
	{
		//in a format of "frame;units;start;end"
		String stringRe = "W;1,2,3,4,5;" + startTime + ";" + endTime;		
		return stringRe;
	}
		
	private String makeWeekendPatternAsTP() throws Exception
	{
		Random ranGen = new Random();
		int percent = 1 + ranGen.nextInt(100);
		int base;
		if (percent <= 20)
			base = 1; //morning
		else if (percent <= 55)
			base = 2; //noon
		else if (percent <= 85)
			base = 3; //evening
		else 
			base = 4; //night
		
		String stringRe = "W;6,7;" + AddStartEndTime(base); 
		return stringRe;
	}
		
	private String makeWeeklyPatternAsTP() throws Exception
	{
		Random ranGen = new Random();
		int numOfDays = 1 + ranGen.nextInt(3);
//		List<Integer> weekDay = new LinkedList<Integer>();
		List<Integer> weekDay = new ArrayList<Integer>();
		for (int i = 1; i <= 7; i++) 
			weekDay.add(i);
		int[] tempPatten = new int[numOfDays];
		for (int i = 0; i < numOfDays; i++){
			int dayInTheWeek =  ranGen.nextInt(weekDay.size());
			tempPatten[i] = weekDay.remove(dayInTheWeek).intValue();
		}
		Arrays.sort(tempPatten);
		int percent = 1 + ranGen.nextInt(100);
		int base;
		if (percent <= 30)
			base = 1; //morning
		else if (percent <= 60)
			base = 2; //noon
		else if (percent <= 90)
			base = 3; //evening
		else 
			base = 4; //night
		String stringRe = "W;";
		for (int i = 0; i < tempPatten.length; i++){
			stringRe = stringRe + i + ",";
		}
		stringRe = stringRe.substring(0 ,stringRe.length()-1) + ";" + AddStartEndTime(base);
		return stringRe;
	}
	
	private String[] makeFlowedWeeklyPattern() throws Exception
	{
		Random ranGen = new Random();
		int numOfDays = 1 + ranGen.nextInt(3);
//		List<Integer> weekDay = new LinkedList<Integer>();
		List<Integer> weekDay = new ArrayList<Integer>();
		for (int i = 1; i <= 7; i++) 
			weekDay.add(i);
		int[] tempPatten = new int[numOfDays];
		for (int i = 0; i < numOfDays; i++){
			int dayInTheWeek =  ranGen.nextInt(weekDay.size());
			tempPatten[i] = weekDay.remove(dayInTheWeek).intValue();
		}
		Arrays.sort(tempPatten);
		int percent = 1 + ranGen.nextInt(100);
		int base;
		if (percent <= 30)
			base = 1; //morning
		else if (percent <= 60)
			base = 2; //noon
		else if (percent <= 90)
			base = 3; //evening
		else 
			base = 4; //night
		String stringRe = "W;";
		for (int i = 0; i < tempPatten.length; i++){
			stringRe = stringRe + i + ",";
		}
		String[] returnArray = new String[3];
		int startTime = 6 + ranGen.nextInt(5);
		returnArray[0] = stringRe.substring(0 ,stringRe.length()-1) + ";" + startTime + ";" + (startTime + 1);
		returnArray[1] = stringRe.substring(0 ,stringRe.length()-1) + ";" + (startTime + 1) + ";" + (startTime + 2);
		returnArray[2] = stringRe.substring(0 ,stringRe.length()-1) + ";" + (startTime + 2) + ";" + (startTime + 3);
		return returnArray;
	}
			
		//*****************************************************************************
		
	
	private String AddStartEndTime(int base){
		switch (base) {
		case 1:
			return "6;12";
		case 2:
			return "12;18";
		case 3:
			return "18;24";
		case 4:
			return "0;6";
		default:
			return null;
		}
	}
	
	private List<String> FileToList(String filePath){
//		final List<String> list = new LinkedList<String>();
		final List<String> list = new ArrayList<String>();
		try{
			BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
			try {
				String inputLine;
				while ((inputLine=bufferedReader.readLine())!=null){
					if (!list.contains(inputLine))
						list.add(inputLine.trim());
				}
			}
			finally{
				bufferedReader.close();			
			}
		}
		catch (Exception  e) {
			// TODO: handle exception
			throw new IllegalArgumentException("Error reading file " + filePath );
		}
		return list;
	}
	
	
}

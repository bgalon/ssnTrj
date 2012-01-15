package ac.technion.geoinfo.ssnTrj.domain;

import org.neo4j.graphdb.Relationship;

public class TimePatternImpl implements TimePattern,Static {

		//in a string format of "frame;units;start;end"
		//frame can be:Y - year, M - month, W - week and D - Day
		//units an array with time units in the time frame 
		
		private TimeFrame frame;
		private int[] units = null; //if null the activity accrues once a... 
		private int start, end;
		private boolean isDoual;
		private Relationship theRel = null;
		
		public TimePatternImpl(Relationship theRel) throws Exception
		{
			if(!theRel.isType(TimePatternRelation.tpToRoute) && !theRel.isType(TimePatternRelation.tpToSpatialEntity))
				throw new Exception("the relationship is not timepattren type");
			if(theRel.hasProperty(TIME_PATTERN_PORP))
			{
				StartMe((String) theRel.getProperty(TIME_PATTERN_PORP));
			}
			this.theRel = theRel;
		}
			
		public TimePatternImpl(String stringRe) throws Exception
		{
			//in a string format of "frame;units;start;end"
			//frame can be:Y - year, M - month, W - week and D - Day
			//units an array with time units in the time frame 
			StartMe(stringRe);
		}
		
		private void StartMe(String stringRe) throws Exception
		{
			String[] firstSplit = stringRe.split(";");
			frame = TimeFrame.str2TF(firstSplit[0]);
			if (frame == null) throw new Exception("not a valid time pattern string, error in time frame");
			if (!firstSplit[1].isEmpty())
			{
				String[] secondSplit = firstSplit[1].split(",");
				units = new int[secondSplit.length];
				for (int i = 0; i < secondSplit.length; i++)
					units[i] = Integer.parseInt(secondSplit[i]);
			}
			start = Integer.parseInt(firstSplit[2]);
			end = Integer.parseInt(firstSplit[3]);
			if (start > 23 && start < 0) throw new Exception("nat a valid time pattern string, error in start time");
			if (end > 24 && end < 1) throw new Exception("nat a valid time pattern string, error in end time");
			if (end == start) throw new Exception("nat a valid time pattern string, error in end and start time");
			if (end < start) {
				isDoual = true;
			}else{
				isDoual = false;
			}
			if (this.frame == TimeFrame.DAY) units = new int[]{1};
		}
		
		public TimeFrame getFrame()
		{
			return frame;
		}
		
		public User getUser() {
			if(theRel != null)
				return new UserImpl(theRel.getStartNode());
			return null;
		}

		public SpatialEntity getSpatialEntity() throws Exception {
			if(theRel != null)
				return new SpatialEntityImpl(theRel.getEndNode());
			return null;
		}
		
		public int[] getUnits()
		{
			return units;
		}
		
		public int getStart()
		{
			return start;
		}
		
		public int getEnd()
		{
			return end;
		}
		
		public boolean isDoual()
		{
			return this.isDoual;
		}

		//work only for the same time pattern
		//the result is between 0 (for no overlap) to 1 (for full overlap)
		public double intersectDiff(TimePattern otherTimePattern){
			if (otherTimePattern == null ) return 1;
			if (this.frame != otherTimePattern.getFrame()) return 0;
			
			int unitIntersection = 0;
			int thisIndex = 0, otherIndex = 0;
			if (this.frame == TimeFrame.DAY) unitIntersection = 1;
			while (thisIndex <= this.units.length -1 && otherIndex <= otherTimePattern.getUnits().length -1){
				if (this.units[thisIndex] == otherTimePattern.getUnits()[otherIndex])
					unitIntersection++;
				if (this.units[thisIndex] <= otherTimePattern.getUnits()[otherIndex])
					thisIndex++;
				else
					otherIndex++;
			}
			if (unitIntersection == 0) return 0;
			
			int dayIntersection = timeIntersection(otherTimePattern);
			if (dayIntersection == 0) return 0;
			
			return (unitIntersection * (dayIntersection)) 
						/ (this.units.length * totalTime()); 
		}
		
		private int timeIntersection(TimePattern otherPatternRe){
			int[] thisTimes;
			int[] otherTimes;
			if (this.isDoual){
				thisTimes = new int[4];
				thisTimes[0] = 0;
				thisTimes[1] = this.end;
				thisTimes[2] = this.start;
				thisTimes[3] = 24;
			}else{
				thisTimes = new int[2];
				thisTimes[0] = this.start;
				thisTimes[1] = this.end;
			}
			
			if (otherPatternRe.isDoual()){
				otherTimes = new int[4];
				otherTimes[0] = 0;
				otherTimes[1] = otherPatternRe.getEnd();
				otherTimes[2] = otherPatternRe.getStart();
				otherTimes[3] = 24;
			}else{
				otherTimes = new int[2];
				otherTimes[0] = otherPatternRe.getStart();
				otherTimes[1] = otherPatternRe.getEnd();
			}
			int result = 0;
			
			for (int i = 0; i < thisTimes.length; i = i + 2){
				for (int j = 0; j < otherTimes.length; j = j + 2){
					
					if (otherTimes[j] > thisTimes[i+1]) continue;
					if (otherTimes[j + 1] < thisTimes[i]) continue;
					
					int newStart = Math.max(thisTimes[i] , otherTimes[j]);
					int newEnd = Math.min(thisTimes[i+1] , otherTimes[j + 1]);
					
					result = result + newEnd - newStart;
				}
			}
			return result;
		}
		
		private double totalTime(){
			if (isDoual){
				return (this.end + 24 - this.start);
			}else{
				return (this.end - this.start);
			}
				
		}
		
		public static double intersect(String TP1, String TP2) throws Exception
		{
			TimePattern tp1 = new TimePatternImpl(TP1);
			TimePattern tp2 = new TimePatternImpl(TP2);
			return tp1.intersectDiff(tp2);
		}
		
//		public double IntersectValue(TimePattren otherPatternRe){
//			//for know the intersection is only for patterns form the same type
//			//work on intersection and return the calc average. 
//			return this.confident * intersectDiff(otherPatternRe);
//			
//			
//		}//isIntersect
		
		@Override
		public String toString(){
			//in a string format of "frame;units;start;end"
			String rtnStr ="";
			rtnStr = rtnStr + frame.toString()+ ";";
			if (units!=null){
				for (int tempI:units)
					rtnStr = rtnStr + tempI + ",";
				rtnStr = rtnStr.substring(0,rtnStr.length()-1);
			}
			rtnStr = rtnStr + ";";
			rtnStr = rtnStr + start + ";" + end;
			return rtnStr;
		}
}//class

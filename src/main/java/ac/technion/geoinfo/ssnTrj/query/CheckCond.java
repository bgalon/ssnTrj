package ac.technion.geoinfo.ssnTrj.query;

import org.neo4j.graphdb.Relationship;

import ac.technion.geoinfo.ssnTrj.domain.TimePatternImpl;

final class CheckCond {
	
	public static boolean CheckRel4Con(Relationship theRel, String conditions) throws Exception
	{
		//condition in a format of "propertyName1 operator1 condition1;propertyName2 operator2 condition2 ..."
		//for example "name == 'hello' ; sameNumber > 50"
		//optional operator are : < , > , <= , >= , == inter 
		if(conditions == null || conditions.isEmpty())
			return true;
		String[] splitCondition = conditions.split(";");
		boolean check = true;
		for (String oneCondition:splitCondition)
		{
			int opInd;
			if ((opInd = oneCondition.indexOf('<')) > 0)
			{
				String prop = oneCondition.substring(0, opInd).trim();
				if (theRel.hasProperty(prop))
				{
					String propValue = (String) theRel.getProperty(prop);
					String comperVal = oneCondition.substring(opInd + 1).trim();
					if (IsNumber(propValue) && IsNumber(comperVal))
					{
						check = check &&  (Double.parseDouble(propValue) < Double.parseDouble(comperVal));
					}
					else
					{
						throw new Exception("error while evaluate " + oneCondition + ". " + propValue + " or " +
								" are not a number");
					}
				}
				else
				{
					throw new Exception("error while evaluate " + oneCondition + ". the reationship " + theRel.toString() 
							+ "(from type " + theRel.getType().toString() +")" + " don't have the property " + prop);
				}
			}
			else if ((opInd = oneCondition.indexOf('>')) > 0)
			{
				String prop = oneCondition.substring(0, opInd).trim();
				if (theRel.hasProperty(prop))
				{
					String propValue = (String) theRel.getProperty(prop);
					String comperVal = oneCondition.substring(opInd + 1).trim();
					if (IsNumber(propValue) && IsNumber(comperVal))
					{
						check = check &&  (Double.parseDouble(propValue) > Double.parseDouble(comperVal));
					}
					else
					{
						throw new Exception("error while evaluate " + oneCondition + ". " + propValue + " or " +
								" are not a number");
					}
				}
				else
				{
					throw new Exception("error while evaluate " + oneCondition + ". the reationship " + theRel.toString() 
							+ "(from type " + theRel.getType().toString() +")" + " don't have the property " + prop);
				}
			}
			else if ((opInd = oneCondition.indexOf("<=")) > 0)
			{
				String prop = oneCondition.substring(0, opInd).trim();
				if (theRel.hasProperty(prop))
				{
					String propValue = (String) theRel.getProperty(prop);
					String comperVal = oneCondition.substring(opInd + 1).trim();
					if (IsNumber(propValue) && IsNumber(comperVal))
					{
						check = check &&  (Double.parseDouble(propValue) <= Double.parseDouble(comperVal));
					}
					else
					{
						throw new Exception("error while evaluate " + oneCondition + ". " + propValue + " or " +
								" are not a number");
					}
				}
				else
				{
					throw new Exception("error while evaluate " + oneCondition + ". the reationship " + theRel.toString() 
							+ "(from type " + theRel.getType().toString() +")" + " don't have the property " + prop);
				}
			}
			else if ((opInd = oneCondition.indexOf(">=")) > 0)
			{
				String prop = oneCondition.substring(0, opInd).trim();
				if (theRel.hasProperty(prop))
				{
					String propValue = (String) theRel.getProperty(prop);
					String comperVal = oneCondition.substring(opInd + 1).trim();
					if (IsNumber(propValue) && IsNumber(comperVal))
					{
						check = check &&  (Double.parseDouble(propValue) >= Double.parseDouble(comperVal));
					}
					else
					{
						throw new Exception("error while evaluate " + oneCondition + ". " + propValue + " or " +
								" are not a number");
					}
				}
				else
				{
					throw new Exception("error while evaluate " + oneCondition + ". the reationship " + theRel.toString() 
							+ "(from type " + theRel.getType().toString() +")" + " don't have the property " + prop);
				}
			}
			else if ((opInd = oneCondition.indexOf("==")) > 0)
			{
				String prop = oneCondition.substring(0, opInd).trim();
				if (theRel.hasProperty(prop))
				{
					String propValue = (String) theRel.getProperty(prop);
					String comperVal = oneCondition.substring(opInd + 1).trim();
					check = check && (propValue.equals(comperVal));
				}
				else
				{
					throw new Exception("error while evaluate " + oneCondition + ". the reationship " + theRel.toString() 
							+ "(from type " + theRel.getType().toString() +")" + " don't have the property " + prop);
				}
			}
			else if ((opInd = oneCondition.indexOf("inter")) > 0)
			{
				String prop = oneCondition.substring(0, opInd).trim();
				if (theRel.hasProperty(prop))
				{
					String propValue = (String) theRel.getProperty(prop);
					String comperVal = oneCondition.substring(opInd + 5).trim();
					if (IsNumber(propValue) && IsNumber(comperVal))
					{
						check = check &&  (TimePatternImpl.intersect(propValue,comperVal) > 0);
					}
					else
					{
						throw new Exception("error while evaluate " + oneCondition + ". " + propValue + " or " +
								" are not a number");
					}
				}
				else
				{
					throw new Exception("error while evaluate " + oneCondition + ". the reationship " + theRel.toString() 
							+ "(from type " + theRel.getType().toString() +")" + " don't have the property " + prop);
				}
			}
			else
			{
				throw new Exception("error while evaluate " + oneCondition + " oprator not found");
			}
		}
		return check;
	}
	
	private static boolean IsNumber(String toTest)
	{
		if(toTest.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
			return true;
		return false;
	}
}

package ac.technion.geoinfo.ssnTrj.domain;

public enum TimeFrame {
	DAY {
			@Override
			public String toString(){return "D";}
		},
	WEEK{
			@Override
			public String toString(){return "W";}	
		},
	MONTH{
			@Override
			public String toString(){return "M";}	
		},
	YEAR{
			@Override
			public String toString(){return "Y";}	
		};
	
	public static TimeFrame str2TF(String TFasStr)
	{
		if (TFasStr.equals("Y"))
			return TimeFrame.YEAR;
		else if (TFasStr.equals("M"))
			return TimeFrame.MONTH;
		else if (TFasStr.equals("W"))
			return TimeFrame.WEEK;
		else if (TFasStr.equals("D"))
			return TimeFrame.DAY;
		else
			return null;
	}
	
}
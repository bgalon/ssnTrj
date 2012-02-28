import java.util.Date;


public class TryChachPalyground {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try
		{
			try
			{
				System.out.println(String.format("%,.2f", 5.4578984 ) + "% done");
				System.out.println("Start, in first try");
				
				String tempBordersTypes = "[buliding,roadSegment]";
				tempBordersTypes = tempBordersTypes.substring(1, tempBordersTypes.length() - 1);
				String[] bordersTypes = tempBordersTypes.split(",");
				
				System.out.print(". including spatial enteties of types:");
				for(String tempType:bordersTypes)
					System.out.print(tempType + " ");
				System.out.println(".");
				
				//System.out.println(bordersTypes);
				System.out.println((new Date()).toString());
			
			}
			catch(Exception e) 
			{
				System.out.println("in inner catch");
				e.printStackTrace();
			}
			finally
			{
				System.out.println("inner finlly");
			}
		}
		catch(Exception e) 
		{
			System.out.println("in outer catch");
			
		}
		finally
		{
			System.out.println("outer finlly");
		}
		
		
		
	}

}

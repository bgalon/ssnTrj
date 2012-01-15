package ac.technion.geoinfo.ssnTrj.trajectories;

public class PIT4Texi extends PointInTime {
	
	public RouteClassification PITClass = RouteClassification.NO_CLASSIFICATION;
	public boolean HasPassenger;
	public double Dis = 0;
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString() + " " + HasPassenger + " " + PITClass + " " + Dis;
	}
}

package ac.technion.geoinfo.ssnTrj;

import java.util.List;


import ac.technion.geoinfo.ssnTrj.domain.SpatialEntity;
import ac.technion.geoinfo.ssnTrj.domain.User;

public interface SSN {
	List<SpatialEntity> AddLocation(String geom, String[] attributes, Object[] values) throws Exception;
	User AddUser();
	void Dispose();
}

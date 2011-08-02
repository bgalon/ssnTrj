package ac.technion.geoinfo.ssnTrj.geometry;

import org.neo4j.gis.spatial.AbstractSearch;
import org.neo4j.graphdb.Node;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class ColsestRoadSearch extends AbstractSearch {

	
	/**
	 * Search for the closest objects within the Envelope window containing the
	 * buffer region.
	 * 
	 * @param other
	 *            Geometry to use
	 * @param buffer
	 *            around this object for creating the searchWindow envelope
	 */
	public ColsestRoadSearch(Geometry other, double buffer) {
		this(other, makeBufferEnvelope(other, buffer));
	}

	private static Envelope makeBufferEnvelope(Geometry other, double buffer) {
		return other.buffer(buffer).getEnvelopeInternal();
	}

	
	/**
	 * Search for the closest objects within the Envelope window.
	 * 
	 * @param other
	 *            Geometry to use
	 * @param searchWindow
	 *            Envelope within objects will be considered, or null to
	 *            consider all
	 */
	public ColsestRoadSearch(Geometry other, Envelope searchWindow) {
		this.other = other;
		this.searchWindow = searchWindow;
	}

	public boolean needsToVisit(Envelope indexNodeEnvelope) {
		return searchWindow == null || indexNodeEnvelope.intersects(searchWindow);
	}

	public final void onIndexReference(Node geomNode) {
		Envelope geomEnvelope = getEnvelope(geomNode);
		if (searchWindow == null || geomEnvelope.intersects(searchWindow)) {
			onEnvelopeIntersection(geomNode, geomEnvelope);
		}
	}

	protected void onEnvelopeIntersection(Node geomNode, Envelope geomEnvelope) {
		Geometry geometry = decode(geomNode);
		//if (geometry.intersects(other)){
			double distance = geometry.distance(other);
			if (distance < minDistance && geometry.getGeometryType().equalsIgnoreCase("LineString")) {
				clearResults();
				minDistance = distance;
				add(geomNode, geometry);
			} else if (distance == minDistance) {
				add(geomNode, geometry);
			}
		//}
	}

	protected Geometry other;
	protected Envelope searchWindow;
	protected double minDistance = Double.MAX_VALUE;
}
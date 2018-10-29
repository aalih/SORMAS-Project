package de.symeda.sormas.ui.map;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.EventObject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.util.ReflectTools;

import elemental.json.Json;
import elemental.json.JsonArray;

/**
 * JS and CSS files are in the VAADIN folder, so we can also access required
 * images and other resources.
 * 
 * @author Martin Wahnschaffe
 */
@JavaScript({ "vaadin://map/leaflet.js", "vaadin://map/leaflet.fullscreen.js", "vaadin://map/leaflet-easy-print.js", "vaadin://map/leaflet.markercluster.js", "vaadin://map/leaflet-connector.js" })
@StyleSheet({ "vaadin://map/leaflet.css", "vaadin://map/leaflet.fullscreen.css", "vaadin://map/MarkerCluster.css", "vaadin://map/MarkerCluster.Default.css" })
public class LeafletMap extends AbstractJavaScriptComponent {

	final static Logger logger = LoggerFactory.getLogger(LeafletMap.class);

	private static final long serialVersionUID = 1671451734103288729L;

	public static int currMapId = 0;

	public static int nextMapId() {
		return ++currMapId;
	}

	protected int mapId = nextMapId();

	/**
	 * Creates the chart object.
	 */
	@SuppressWarnings("serial")
	public LeafletMap() {
		setId(getDomId());
		getState().zoom = 5;
		getState().centerLatitude = 51.505;
		getState().centerLongitude = -0.09;
		getState().tileLayerVisible = true;
		getState().tileLayerOpacity = 1;

		addFunction("onClick", new JavaScriptFunction() {
			@Override
			public void call(JsonArray arguments) {
				String groupId = arguments.getString(0);
				int markerIndex = (int) arguments.getNumber(1);
				LeafletMap.this.fireEvent(new MarkerClickEvent(LeafletMap.this, groupId, markerIndex));
			}
		});
	}

	/**
	 * @return the state of the chart that is shared with the web browser
	 */
	@Override
	protected LeafletState getState() {
		return (LeafletState) super.getState();
	}

	/**
	 * @return the DOM ID of the map component
	 */
	public String getDomId() {
		return "leaflet_" + mapId;
	}

	/**
	 * Sets the Leaflet JavaScript code describing the map.
	 */
	public void setZoom(int zoom) {
		getState().zoom = zoom;
	}

	public int getZoom() {
		return getState().zoom;
	}

	public void setCenter(double lat, double lon) {
		getState().centerLatitude = lat;
		getState().centerLongitude = lon;
	}
	
	public void setTileLayerVisible(boolean tileLayerVisible) {
		getState().tileLayerVisible = tileLayerVisible;
	}

	public void setTileLayerOpacity(float tileLayerOpacity) {
		getState().tileLayerOpacity = tileLayerOpacity;
	}

	public void addMarkerClickListener(MarkerClickListener listener) {
		addListener(MarkerClickEvent.class, listener, MarkerClickListener.MARKER_CLICK_METHOD);
	}

	public void addMarkerGroup(String groupId, List<LeafletMarker> markers) {
		JsonArray markersJson = Json.createArray();
		for (LeafletMarker marker : markers) {
			markersJson.set(markersJson.length(), marker.toJson());
		}
		callFunction("addMarkerGroup", groupId, markersJson);
	}

	public void addPolygonGroup(String groupId, List<LeafletPolygon> polygons) {
		JsonArray polygonsJson = Json.createArray();
		for (LeafletPolygon polygon : polygons) {
			polygonsJson.set(polygonsJson.length(), polygon.toJson());
		}
		callFunction("addPolygonGroup", groupId, polygonsJson);
	}

	public void removeGroup(String groupId) {
		callFunction("removeGroup", groupId);
	}

	public interface MarkerClickListener extends Serializable {

		public static final Method MARKER_CLICK_METHOD = ReflectTools.findMethod(MarkerClickListener.class,
				"markerClick", MarkerClickEvent.class);

		public void markerClick(MarkerClickEvent event);

	}

	public static class MarkerClickEvent extends EventObject {
		private static final long serialVersionUID = -2607378360765308016L;
		private final String groupId;
		private final int markerIndex;

		public MarkerClickEvent(LeafletMap map, String groupId, int markerIndex) {
			super(map);
			this.groupId = groupId;
			this.markerIndex = markerIndex;
		}

		public String getGroupId() {
			return groupId;
		}

		public int getMarkerIndex() {
			return markerIndex;
		}
	}
}
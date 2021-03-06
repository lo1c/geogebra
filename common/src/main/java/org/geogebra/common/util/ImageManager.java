package org.geogebra.common.util;

import java.util.ArrayList;

import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoImage;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.main.App;

abstract public class ImageManager {

	public void setCornersFromSelection(GeoImage geoImage, App app) {
		ArrayList<GeoPointND> corners = new ArrayList<GeoPointND>();
		for (int i = 0; i < 3; i++) {
			GeoPoint p = getImageCornerFromSelection(i, app);
			if (p != null) {
				corners.add(p);
			}
		}

		GeoPointND point1 = null;

		if (corners.size() == 0) {
			point1 = new GeoPoint(app.getKernel().getConstruction());
			point1.setCoords(0, 0, 1.0);
			point1.setLabel(null);
			ensure1stCornerOnScreen(point1, app);
			corners.add(point1);
		} else if (corners.size() == 1) {
			point1 = corners.get(0);
		}

		for (int i = 0; i < corners.size(); i++) {
			geoImage.setCorner(corners.get(i), i);
		}

		if (corners.size() < 2) {
			GeoPoint point2 = new GeoPoint(app.getKernel().getConstruction());
			geoImage.calculateCornerPoint(point2, 2);
			geoImage.setCorner(point2, 1);
			point2.setLabel(null);

			// make sure 2nd corner is on screen
			ensure2ndCornerOnScreen(point1.getInhomX(), point2, app);
		}
		geoImage.setLabel(null);

		//
		GeoImage.updateInstances(app);

	}

	private GeoPoint getImageCornerFromSelection(int index, App app) {

		ArrayList<GeoElement> sel = app.getSelectionManager().getSelectedGeos();
		if (sel.size() > index) {
			GeoElement geo0 = sel.get(index);
			if (geo0.isGeoPoint()) {
				return (GeoPoint) geo0;
			}
		}
		return null;
	}

	public void ensure2ndCornerOnScreen(double x1, GeoPoint point, App app) {
		double x2 = point.inhomX;
		EuclidianView ev = app.getActiveEuclidianView();
		double xmax = ev.toRealWorldCoordX((double) (ev.getWidth()) + 1);
		if (x2 > xmax) {
			point.setCoords((x1 + 9 * xmax) / 10, point.inhomY, 1);
			point.update();
		}

	}

	private void ensure1stCornerOnScreen(GeoPointND point, App app) {
		EuclidianView ev = app.getActiveEuclidianView();
		double xmin = ev.toRealWorldCoordX(0.0);
		double xmax = ev.toRealWorldCoordX((double) (ev.getWidth()) + 1);
		double ymin = ev.toRealWorldCoordY(0.0);
		double ymax = ev.toRealWorldCoordY((double) (ev.getHeight()) + 1);
		point.setCoords(xmin + (xmax - xmin) / 5, ymax - (ymax - ymin) / 5,
				1.0);
		point.update();
	}

}

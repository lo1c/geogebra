package org.geogebra.common.geogebra3D.euclidian3D.draw;

import java.util.ArrayList;

import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.Previewable;
import org.geogebra.common.geogebra3D.euclidian3D.EuclidianView3D;
import org.geogebra.common.geogebra3D.euclidian3D.Hitting;
import org.geogebra.common.geogebra3D.euclidian3D.openGL.PlotterBrush;
import org.geogebra.common.geogebra3D.euclidian3D.openGL.Renderer;
import org.geogebra.common.geogebra3D.euclidian3D.openGL.Renderer.PickingType;
import org.geogebra.common.geogebra3D.euclidian3D.printer3D.ExportToPrinter3D;
import org.geogebra.common.geogebra3D.euclidian3D.printer3D.ExportToPrinter3D.Type;
import org.geogebra.common.geogebra3D.kernel3D.algos.AlgoPolyhedronPoints;
import org.geogebra.common.geogebra3D.kernel3D.algos.AlgoPolyhedronPointsPrism;
import org.geogebra.common.geogebra3D.kernel3D.algos.AlgoPolyhedronPointsPyramid;
import org.geogebra.common.geogebra3D.kernel3D.geos.GeoPolyhedron;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Matrix.Coords;
import org.geogebra.common.kernel.geos.GProperty;
import org.geogebra.common.kernel.geos.GeoPolygon;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.kernel.kernelND.GeoSegmentND;
import org.geogebra.common.main.Feature;

/**
 * Class for drawing 3D polygons.
 * 
 * @author matthieu
 *
 */
public class DrawPolyhedron3D extends Drawable3DSurfaces
		implements Previewable {

	private DrawPolygon3D drawPolygon3D;
	private Coords[] vertices = new Coords[0];

	private ArrayList<GeoPointND> selectedPoints;
	private ArrayList<GeoPolygon> selectedPolygons;
	private int previewMode;
	private boolean previewBasisIsFinished = false;
	private AlgoPolyhedronPoints previewAlgo;

	private Coords globalCoords, inPlaneCoords;

	/**
	 * Common constructor
	 * 
	 * @param a_view3D
	 *            view
	 * @param poly
	 *            polyhedron
	 */
	public DrawPolyhedron3D(EuclidianView3D a_view3D, GeoPolyhedron poly) {

		super(a_view3D, poly);

	}

	/**
	 * Preview constructor
	 * 
	 * @param a_view3D
	 *            view
	 * @param selectedPoints
	 *            input polyons
	 * @param selectedPolygons
	 *            input polygons
	 * @param mode
	 *            preview mode
	 */
	public DrawPolyhedron3D(EuclidianView3D a_view3D,
			ArrayList<GeoPointND> selectedPoints,
			ArrayList<GeoPolygon> selectedPolygons, int mode) {

		super(a_view3D);

		drawPolygon3D = new DrawPolygon3D(a_view3D, selectedPoints);

		this.selectedPoints = selectedPoints;
		this.selectedPolygons = selectedPolygons;

		previewMode = mode;

		// updatePreview();

	}

	// drawing

	@Override
	public void drawGeometry(Renderer renderer) {

		renderer.setLayer(getLayer()); // +0f for z-fighting with planes
		renderer.getGeometryManager().draw(getGeometryIndex());
		renderer.setLayer(0);

	}

	@Override
	public void drawOutline(Renderer renderer) {

		if (isVisible()) {

			setHighlightingColor();

			renderer.getTextures()
					.setDashFromLineType(getGeoElement().getLineType());
			drawGeometry(renderer);
		}

		drawTracesOutline(renderer, false);

	}

	@Override
	public void drawGeometryHiding(Renderer renderer) {
		drawSurfaceGeometry(renderer);
	}

	@Override
	public void drawGeometryHidden(Renderer renderer) {
		drawGeometry(renderer);
	}

	@Override
	protected void drawGeometryForPicking(Renderer renderer, PickingType type) {
		if (type == PickingType.POINT_OR_CURVE) {
			drawGeometry(renderer);
		} else {
			if (getAlpha() > 0) { // surface is pickable only if not totally
									// transparent
				drawSurfaceGeometry(renderer);
			}
		}
	}

	@Override
	protected void drawSurfaceGeometry(Renderer renderer) {

		renderer.setLayer(getLayer()); // +0f to avoid z-fighting with planes
		renderer.getGeometryManager().draw(getSurfaceIndex());
		renderer.setLayer(0);

	}

	@Override
	public int getPickOrder() {
		return DRAW_PICK_ORDER_SURFACE;
	}

	@Override
	public void addToDrawable3DLists(Drawable3DLists lists) {

		addToDrawable3DLists(lists, DRAW_TYPE_CLOSED_SURFACES_NOT_CURVED);
		addToDrawable3DLists(lists, DRAW_TYPE_CURVES);

	}

	@Override
	public void removeFromDrawable3DLists(Drawable3DLists lists) {

		removeFromDrawable3DLists(lists, DRAW_TYPE_CLOSED_SURFACES_NOT_CURVED);
		removeFromDrawable3DLists(lists, DRAW_TYPE_CURVES);

	}

	@Override
	protected boolean updateForItSelf() {

		Renderer renderer = getView3D().getRenderer();

		// outline
		updateOutline(renderer);

		// surface
		updateSurface(renderer);

		return true;

	}

	private void updateSurface(Renderer renderer) {
		int index = renderer.startPolygons(getReusableSurfaceIndex());
		for (GeoPolygon p : ((GeoPolyhedron) getGeoElement())
				.getPolygonsLinked()) {
			drawPolygon(renderer, p);
		}
		for (GeoPolygon p : ((GeoPolyhedron) getGeoElement()).getPolygons()) {
			drawPolygon(renderer, p);
		}
		renderer.endPolygons();

		setSurfaceIndex(index);
	}

	private void updateOutline(Renderer renderer) {

		GeoPolyhedron poly = (GeoPolyhedron) getGeoElement();

		int thickness = poly.getLineThickness();
		if (thickness == 0) {
			setGeometryIndex(-1);
		} else {
			PlotterBrush brush = renderer.getGeometryManager().getBrush();
			brush.start(getReusableGeometryIndex());
			brush.setThickness(thickness, (float) getView3D().getScale());

			for (GeoPolygon p : ((GeoPolyhedron) getGeoElement()).getPolygonsLinked()) {
				// draw segments for polygons that have no label
				if (p.isEuclidianVisible() && !p.isLabelSet()) {
					for (GeoSegmentND seg : p.getSegments()) {
						drawSegment(brush, seg);
					}
				}
			}
			for (GeoSegmentND seg : poly.getSegments()) {
				drawSegment(brush, seg);
			}

			setGeometryIndex(brush.end());
		}

	}

	private static void drawSegment(PlotterBrush brush, GeoSegmentND seg) {

		// draw only segments that have no label
		if (!seg.isEuclidianVisible() || seg.isLabelSet()) {
			return;
		}

		brush.setAffineTexture(0.5f, 0.25f);
		brush.segment(seg.getStartInhomCoords(), seg.getEndInhomCoords());

	}

	private void drawPolygon(Renderer renderer, GeoPolygon polygon) {

		// draw only polygons that have no label
		if (!polygon.isEuclidianVisible() || polygon.isLabelSet()) {
			return;
		}

		int pointLength = polygon.getPointsLength();

		if (pointLength < 3) { // no polygon
			return;
		}

		if (vertices.length < pointLength) {
			vertices = new Coords[pointLength];
			for (int i = 0; i < pointLength; i++) {
				vertices[i] = new Coords(3);
			}
		}

		for (int i = 0; i < pointLength; i++) {
			vertices[i].setValues(polygon.getPoint3D(i), 3);
		}

		DrawPolygon3D.drawPolygon(renderer, polygon, vertices,
				polygon.getPointsLength());

	}

	@Override
	protected void updateForView() {

		if (getView3D().viewChangedByZoom()) {

			Renderer renderer = getView3D().getRenderer();

			// outline
			updateOutline(renderer);

			if (getView3D().getApplication()
					.has(Feature.DIFFERENT_AXIS_RATIO_3D)) {
				// surface
				updateSurface(renderer);
			}

			recordTrace();

		}
	}

	@Override
	public void updatePreview() {

		if (previewBasisIsFinished) {
			getView3D().getCursor3D().updateCascade();
			return;
		}

		if (selectedPolygons.size() == 1) {
			previewBasisIsFinished = true;

			Construction cons = getView3D().getKernel().getConstruction();

			switch (previewMode) {
				default:
				case EuclidianConstants.MODE_PYRAMID:
					previewAlgo = new AlgoPolyhedronPointsPyramid(cons, null,
							selectedPolygons.get(0), getView3D().getCursor3D());
					break;
				case EuclidianConstants.MODE_PRISM:
					previewAlgo = new AlgoPolyhedronPointsPrism(cons, null,
							selectedPolygons.get(0), getView3D().getCursor3D());
					break;
			}

			// set visibilities
			previewAlgo.removeOutputFromAlgebraView();
			previewAlgo.removeOutputFromPicking();
			previewAlgo.setOutputPointsEuclidianVisible(false);
			previewAlgo.notifyUpdateOutputPoints();

			// ensure correct drawing of visible parts of the previewable
			previewAlgo.setOutputOtherEuclidianVisible(true);
			previewAlgo.notifyUpdateOutputOther();

		} else {
			drawPolygon3D.updatePreview();
		}

	}

	@Override
	public void disposePreview() {
		super.disposePreview();

		if (previewBasisIsFinished) {
			previewBasisIsFinished = false;

			if (previewAlgo != null) {
				previewAlgo.remove();
				previewAlgo = null;
			}
		} else {
			drawPolygon3D.disposePreview();
		}

	}

	// public void hidePreview(){
	// if (previewBasisIsFinished){
	// previewBasisIsFinished = false;
	//
	// if (previewAlgo != null){
	// previewAlgo.remove();
	// previewAlgo = null;
	// }
	// }else{
	// drawPolygon3D.hidePreview();
	// }
	// }

	@Override
	public void updateMousePos(double x, double y) {
		// TODO Auto-generated method stub
	}

	/**
	 * tells that the preview basis is done
	 */
	public void previewBasisIsFinished() {
		previewBasisIsFinished = true;

		// dispose polygon preview
		drawPolygon3D.disposePreview();

		// create polyhedron
		GeoPointND[] points = new GeoPointND[selectedPoints.size() + 1];
		for (int i = 0; i < selectedPoints.size(); i++) {
			points[i] = selectedPoints.get(i);
		}
		points[selectedPoints.size()] = getView3D().getCursor3D();

		Construction cons = getView3D().getKernel().getConstruction();

		switch (previewMode) {
			default:
			case EuclidianConstants.MODE_PYRAMID:
				previewAlgo = new AlgoPolyhedronPointsPyramid(cons, null,
						points);
				break;
			case EuclidianConstants.MODE_PRISM:
				previewAlgo = new AlgoPolyhedronPointsPrism(cons, null, points);
				break;
		}

		// set visibilities
		previewAlgo.removeOutputFromAlgebraView();
		previewAlgo.removeOutputFromPicking();
		previewAlgo.setOutputPointsEuclidianVisible(false);
		previewAlgo.notifyUpdateOutputPoints();

		// ensure correct drawing of visible parts of the previewable
		previewAlgo.setOutputOtherEuclidianVisible(true);
		previewAlgo.notifyUpdateOutputOther();


	}

	@Override
	public boolean hit(Hitting hitting) {

		if (waitForReset) { // prevent NPE
			return false;
		}

		// project hitting origin on polygon plane
		if (globalCoords == null) {
			globalCoords = new Coords(4);
			inPlaneCoords = new Coords(4);
		}

		double d = Double.NaN;
		for (GeoPolygon p : ((GeoPolyhedron) getGeoElement())
				.getPolygonsLinked()) {
			d = hitPolygon(d, hitting, p, globalCoords, inPlaneCoords);
		}
		for (GeoPolygon p : ((GeoPolyhedron) getGeoElement()).getPolygons()) {
			d = hitPolygon(d, hitting, p, globalCoords, inPlaneCoords);
		}

		if (!Double.isNaN(d)) {
			setZPick(d, d);
			setPickingType(PickingType.SURFACE);
			return true;
		}

		return false;
	}

	static private double hitPolygon(double currentDistance, Hitting hitting,
			GeoPolygon polygon, Coords globalCoords, Coords inPlaneCoords) {
		if (!polygon.isEuclidianVisible() || polygon.isLabelSet()) {
			return currentDistance;
		}

		hitting.origin.projectPlaneThruVIfPossible(
				polygon.getCoordSys().getMatrixOrthonormal(), hitting.direction,
				globalCoords, inPlaneCoords);

		if (!hitting.isInsideClipping(globalCoords)) {
			return currentDistance;
		}

		// check if hitting projection hits the polygon
		if (polygon.isInRegion(inPlaneCoords.getX(), inPlaneCoords.getY())) {
			double parameterOnHitting = inPlaneCoords.getZ();// TODO use
																// other for
																// non-parallel
																// projection
																// :
																// -hitting.origin.distance(project[0]);
			if (parameterOnHitting < currentDistance) { // currentDistance may
														// be NaN
				return currentDistance;
			}
			return parameterOnHitting;
		}

		return currentDistance;
	}

	@Override
	public void exportToPrinter3D(ExportToPrinter3D exportToPrinter3D) {
		if (isVisible()) {
			// faces
			for (GeoPolygon p : ((GeoPolyhedron) getGeoElement())
					.getPolygonsLinked()) {
				exportToPrinter3D(exportToPrinter3D, p);
			}
			for (GeoPolygon p : ((GeoPolyhedron) getGeoElement())
					.getPolygons()) {
				exportToPrinter3D(exportToPrinter3D, p);
			}
			// edges
			exportToPrinter3D.export(getGeometryIndex(), Type.CURVE, "SEGMENT",
					getGeoElement());
		}
	}

	private void exportToPrinter3D(ExportToPrinter3D exportToPrinter3D,
			GeoPolygon polygon) {
		// export only polygons that have no label
		if (!polygon.isEuclidianVisible() || polygon.isLabelSet()) {
			return;
		}

		int pointLength = polygon.getPointsLength();

		if (pointLength < 3) { // no polygon
			return;
		}

		if (vertices.length < pointLength) {
			vertices = new Coords[pointLength];
			for (int i = 0; i < pointLength; i++) {
				vertices[i] = new Coords(3);
			}
		}

		for (int i = 0; i < pointLength; i++) {
			vertices[i].setValues(polygon.getPoint3D(i), 3);
		}

		exportToPrinter3D.export(polygon, vertices, getGeoElement().getObjectColor(), getGeoElement().getAlphaValue());

	}

	@Override
	public void setWaitForUpdateVisualStyle(GProperty prop) {
		super.setWaitForUpdateVisualStyle(prop);
		if (prop == GProperty.LINE_STYLE) {
			// also update for line width (e.g when translated)
			setWaitForUpdate();
		}
	}

}

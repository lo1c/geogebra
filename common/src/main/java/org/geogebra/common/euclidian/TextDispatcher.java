package org.geogebra.common.euclidian;

import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GPoint;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.Path;
import org.geogebra.common.kernel.Region;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.algos.AlgoArcLength;
import org.geogebra.common.kernel.algos.AlgoClosestPoint;
import org.geogebra.common.kernel.algos.AlgoPolygon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoFunction;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPolyLine;
import org.geogebra.common.kernel.geos.GeoPolygon;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoConicND;
import org.geogebra.common.kernel.kernelND.GeoConicPartND;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.kernel.kernelND.GeoLineND;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.main.Localization;
import org.geogebra.common.util.StringUtil;

public class TextDispatcher {
	protected Localization loc;
	protected Kernel kernel;
	private EuclidianView view;

	public TextDispatcher(Kernel kernel, EuclidianView view) {
		this.kernel = kernel;
		this.view = view;
		this.loc = kernel.getLocalization();
	}

	protected static String removeUnderscoresAndBraces(String label) {
		// remove all subscripts
		return label.replaceAll("_", "").replaceAll("\\{", "").replaceAll("\\}",
				"");
	}

	protected void setNoPointLoc(GeoText text, GPoint loc) {
		text.setAbsoluteScreenLocActive(true);
		text.setAbsoluteScreenLoc(loc.x, loc.y);
	}

	public GeoElement[] getAreaText(GeoElement conic, GeoNumberValue area,
			GPoint loc0) {
		// text
		GeoText text = createDynamicTextForMouseLoc("AreaOfA", "Area of %0",
				conic, area,
				loc0);
		if (conic.isLabelSet()) {
			if (!area.isLabelSet()) {
				area.setLabel(removeUnderscoresAndBraces(
						StringUtil.toLowerCaseUS(loc.getCommand("Area"))
								+ conic.getLabelSimple()));
			}
			text.setLabel(removeUnderscoresAndBraces(
					loc.getMenu("Text") + conic.getLabelSimple()));
		}
		return new GeoElement[] { text };
	}

	private String descriptionPoints(String type, String default0,
			GeoPolygon poly) {
		// build description text including point labels
		StringBuilder descText = new StringBuilder();

		// use points for polygon with static points (i.e. no list of points)
		GeoPointND[] points = null;
		if (poly.getParentAlgorithm() instanceof AlgoPolygon) {
			points = ((AlgoPolygon) poly.getParentAlgorithm()).getPoints();
		}

		if (points != null) {
			descText.append(" \"");
			boolean allLabelsSet = true;
			for (int i = 0; i < points.length; i++) {
				if (points[i].isLabelSet()) {
					descText.append(" + Name["
							+ points[i].getLabel(StringTemplate.defaultTemplate)
							+ "]");
				} else {
					allLabelsSet = false;
					i = points.length;
				}
			}

			if (allLabelsSet) {
				descText.append(" + \"");
				for (int i = 0; i < points.length; i++) {
					points[i].setLabelVisible(true);
					points[i].updateRepaint();
				}
			} else {
				return loc.getPlainDefault(type, default0,
						"\" + Name["
								+ poly.getLabel(StringTemplate.defaultTemplate)
								+ "] + \"");
			}
		} else {
			return loc.getPlainDefault(type, default0, "\" + Name["
					+ poly.getLabel(StringTemplate.defaultTemplate) + "] + \"");
		}

		return loc.getPlainDefault(type, default0, descText.toString());
	}

	/**
	 * Creates a text that shows a number value of geo.
	 */
	private GeoText createDynamicText(String type, String default0,
			GeoElement object,
			GeoElementND value) {
		// create text that shows length
		try {

			// type might be eg "Area of %0" or "XXX %0 YYY"

			String descText;

			if (object.isGeoPolygon()) {
				descText = descriptionPoints(type, default0,
						(GeoPolygon) object);
			} else {
				descText = loc.getPlainDefault(type, default0,
						"\" + Name["
								+ object.getLabel(
										StringTemplate.defaultTemplate)
								+ "] + \"");
			}

			// create dynamic text
			String dynText = "\"" + descText + " = \" + "
					+ value.getLabel(StringTemplate.defaultTemplate);

			// checkZooming();

			GeoText text = kernel.getAlgebraProcessor().evaluateToText(dynText,
					true, true);
			return text;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Creates a text that shows a number value of geo at the current mouse
	 * position.
	 */
	protected GeoText createDynamicTextForMouseLoc(String type, String default0,
			GeoElement object, GeoElementND value, GPoint loc) {

		GeoText text = createDynamicText(type, default0, object, value);
		if (text != null) {
			GeoPointND P = null;
			if (object.isRegion()) {
				P = getPointForDynamicText((Region) object, loc);
			} else if (object.isPath()) {
				P = getPointForDynamicText((Path) object, loc);
			} else {
				P = getPointForDynamicText(loc);
			}

			if (P != null) {
				P.setAuxiliaryObject(true);
				P.setEuclidianVisible(false);
				P.updateRepaint();
				try {
					text.setStartPoint(P);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			} else {
				setNoPointLoc(text, loc);
			}

			text.checkVisibleIn3DViewNeeded();
			text.setBackgroundColor(GColor.WHITE);
			text.updateRepaint();
		}

		return text;

	}

	protected GeoPointND getPointForDynamicText(Region object, GPoint loc0) {
		double rwx = 0, rwy = 0;
		if (loc0 != null) {
			rwx = view.toRealWorldCoordX(loc0.x);
			rwy = view.toRealWorldCoordY(loc0.y);
		} else if (object instanceof GeoPolygon) {
			GeoPointND[] pts = ((GeoPolygon) object).getPointsND();
			for (GeoPointND pt : pts) {
				rwx += pt.getCoordsInD2().getX();
				rwy += pt.getCoordsInD2().getY();
			}
			rwx = rwx / pts.length;
			rwy = rwy / pts.length;
		} else if (object instanceof GeoConicND) {
			rwx = ((GeoConicND) object).getTranslationVector().getX();
			rwy = ((GeoConicND) object).getTranslationVector().getY();
		}
		return view.getEuclidianController().createNewPoint(
				removeUnderscoresAndBraces(loc.getMenu("Point")
						+ object.getLabel(StringTemplate.defaultTemplate)),
				false, object, rwx, rwy, 0, false, false);
	}

	protected GeoPointND getPointForDynamicText(Path object, GPoint loc0) {

		return view.getEuclidianController().getCompanion().createNewPoint(
				removeUnderscoresAndBraces(loc.getMenu("Point")
						+ object.getLabel(StringTemplate.defaultTemplate)),
				false, object, view.toRealWorldCoordX(loc0.x),
				view.toRealWorldCoordY(loc0.y), 0, false, false);
	}

	protected GeoPointND getPointForDynamicText(GPoint loc) {

		return null;
	}

	/**
	 * Creates a text that shows the distance length between geoA and geoB at
	 * the given startpoint.
	 */
	public GeoText createDistanceText(GeoElementND geoA, GeoElementND geoB,
			GeoPointND textCorner, GeoNumeric length) {
		StringTemplate tpl = StringTemplate.defaultTemplate;
		// create text that shows length
		try {
			String strText = "";
			boolean useLabels = geoA.isLabelSet() && geoB.isLabelSet();
			if (useLabels) {
				length.setLabel(removeUnderscoresAndBraces(
						StringUtil.toLowerCaseUS(loc.getCommand("Distance"))
								// .toLowerCase(Locale.US)
								+ geoA.getLabel(tpl) + geoB.getLabel(tpl)));
				// strText = "\"\\overline{\" + Name["+ geoA.getLabel()
				// + "] + Name["+ geoB.getLabel() + "] + \"} \\, = \\, \" + "
				// + length.getLabel();

				// DistanceAB="\\overline{" + %0 + %1 + "} \\, = \\, " + %2
				// or
				// DistanceAB=%0+%1+" \\, = \\, "+%2
				strText = "Name[" + geoA.getLabel(tpl) + "] + Name["
						+ geoB.getLabel(tpl) + "] + \" = \" + "
						+ length.getLabel(tpl);
				// Application.debug(strText);
				makeLabelNameVisible(geoA);
				makeLabelNameVisible(geoB);
				geoA.updateRepaint();
				geoB.updateRepaint();
			} else {
				length.setLabel(removeUnderscoresAndBraces(
						StringUtil.toLowerCaseUS(loc.getCommand("Distance"))));
				// .toLowerCase(Locale.US)));
				strText = "\"\"" + length.getLabel(tpl);
			}

			// create dynamic text
			// checkZooming();

			GeoText text = kernel.getAlgebraProcessor().evaluateToText(strText,
					true, true);
			if (useLabels) {
				text.setLabel(removeUnderscoresAndBraces(loc.getMenu("Text")
						+ geoA.getLabel(tpl) + geoB.getLabel(tpl)));
			}

			text.checkVisibleIn3DViewNeeded();
			text.setStartPoint(textCorner);
			text.setBackgroundColor(GColor.WHITE);
			text.updateRepaint();
			return text;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static void makeLabelNameVisible(GeoElementND geo) {
		// make sure that name of the geo will be visible
		if (!geo.isLabelVisible()) {
			if (geo.getLabelMode() != GeoElement.LABEL_NAME_VALUE) {
				geo.setLabelMode(GeoElement.LABEL_NAME);
			}
			geo.setLabelVisible(true);
		} else {
			if (geo.getLabelMode() == GeoElement.LABEL_VALUE) {
				geo.setLabelMode(GeoElement.LABEL_NAME_VALUE);
			}
		}
	}

	public GeoElement[] createCircumferenceText(GeoConicND conic, GPoint loc0) {
		if (conic.isGeoConicPart()) {

			Construction cons = kernel.getConstruction();
			AlgoArcLength algo = new AlgoArcLength(cons, null,
					(GeoConicPartND) conic);
			// cons.removeFromConstructionList(algo);
			GeoNumeric arcLength = algo.getArcLength();

			GeoText text = createDynamicTextForMouseLoc("ArcLengthOfA",
					"Arc length of %0", conic,
					arcLength, loc0);
			text.setLabel(removeUnderscoresAndBraces(
					loc.getMenu("Text") + conic.getLabelSimple()));
			GeoElement[] ret = { text };
			return ret;

		}

		// standard case: conic
		// checkZooming();

		GeoNumeric circumFerence = kernel.getAlgoDispatcher()
				.Circumference(null, conic);

		// text
		GeoText text = createDynamicTextForMouseLoc("CircumferenceOfA",
				"Circumference of %0", conic,
				circumFerence, loc0);
		if (conic.isLabelSet()) {
			circumFerence.setLabel(removeUnderscoresAndBraces(
					StringUtil.toLowerCaseUS(loc.getCommand("Circumference"))
							+ conic.getLabel(StringTemplate.defaultTemplate)));
			text.setLabel(removeUnderscoresAndBraces(loc.getMenu("Text")
					+ conic.getLabel(StringTemplate.defaultTemplate)));
		}
		GeoElement[] ret = { text };
		return ret;
	}

	public GeoElement[] createPerimeterText(GeoPolygon poly, GPoint mouseLoc) {
		GeoNumeric perimeter = kernel.getAlgoDispatcher().Perimeter(null, poly);

		// text
		GeoText text = createDynamicTextForMouseLoc("PerimeterOfA",
				"Perimeter of %0", poly,
				perimeter, mouseLoc);

		if (poly.isLabelSet()) {
			perimeter.setLabel(removeUnderscoresAndBraces(
					StringUtil.toLowerCaseUS(loc.getCommand("Perimeter"))
							+ poly.getLabelSimple()));
			text.setLabel(removeUnderscoresAndBraces(
					loc.getMenu("Text") + poly.getLabelSimple()));
		}
		text.checkVisibleIn3DViewNeeded();
		GeoElement[] ret = { text };
		return ret;
	}

	public GeoElement[] createPerimeterText(GeoPolyLine poly, GPoint mouseLoc) {
		// text
		GeoText text = createDynamicTextForMouseLoc("PerimeterOfA",
				"Perimeter of %0", poly, poly,
				mouseLoc);

		if (poly.isLabelSet()) {
			text.setLabel(removeUnderscoresAndBraces(
					loc.getMenu("Text") + poly.getLabelSimple()));
		}
		text.checkVisibleIn3DViewNeeded();
		GeoElement[] ret = { text };
		return ret;
	}

	public GeoElement[] createSlopeText(GeoLine line, GeoFunction f,
			GPoint mouseLoc) {
		GeoNumeric slope;
		/*
		 * if ("de_AT".equals(strLocale)) { slope = kernel.Slope("k", line); }
		 * else { slope = kernel.Slope("m", line); }
		 */

		String label = loc.getPlainDefault("ExplicitLineGradient", "m");

		// make sure automatic naming goes m, m_1, m_2, ..., m_{10}, m_{11}
		// etc
		if (kernel.lookupLabel(label) != null) {
			int i = 1;
			while (kernel.lookupLabel(
					i > 9 ? label + "_{" + i + "}" : label + "_" + i) != null) {
				i++;
			}
			label = i > 9 ? label + "_{" + i + "}" : label + "_" + i;
		}

		// checkZooming();

		slope = kernel.getAlgoDispatcher().Slope(label, line, f);

		// show value
		if (slope.isLabelVisible()) {
			slope.setLabelMode(GeoElement.LABEL_NAME_VALUE);
		} else {
			slope.setLabelMode(GeoElement.LABEL_VALUE);
		}
		slope.setLabelVisible(true);
		slope.updateRepaint();
		GeoElement[] ret = { slope };
		return ret;
	}

	public GeoElement createDistanceText(GeoPointND point1, GeoPointND point2) {
		GeoNumeric length = kernel.getAlgoDispatcher().Distance(null, point1,
				point2);

		// set startpoint of text to midpoint of two points
		GeoPointND midPoint = MidpointForDistance(point1, point2);
		return this.createDistanceText(point1, point2, midPoint, length);
	}

	/**
	 * Creates Midpoint M = (P + Q)/2 without label (for use as e.g. start
	 * point)
	 */
	private final GeoPointND MidpointForDistance(GeoPointND P, GeoPointND Q) {

		return (GeoPointND) view.getEuclidianController().getCompanion()
				.midpoint(P, Q);
	}

	public GeoElement createDistanceText(GeoPointND point, GeoLineND line) {
		GeoNumeric length = kernel.getAlgoDispatcher().Distance(null, point,
				line);

		// set startpoint of text to midpoint between point and line
		GeoPointND midPoint = MidpointForDistance(point,
				ClosestPoint(point, (Path) line));
		return this.createDistanceText(point, line, midPoint, length);
	}

	/**
	 * Returns the projected point of P on line g (or nearest for a Segment)
	 */
	final private GeoPointND ClosestPoint(GeoPointND P, Path g) {

		Construction cons = kernel.getConstruction();

		boolean oldMacroMode = cons.isSuppressLabelsActive();
		cons.setSuppressLabelCreation(true);

		AlgoClosestPoint cp = kernel.getAlgoDispatcher()
				.getNewAlgoClosestPoint(cons, g, P);

		cons.setSuppressLabelCreation(oldMacroMode);
		return cp.getP();
	}
}

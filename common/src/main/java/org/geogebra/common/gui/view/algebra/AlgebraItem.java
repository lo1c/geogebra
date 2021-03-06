package org.geogebra.common.gui.view.algebra;

import org.geogebra.common.gui.view.algebra.AlgebraView.SortMode;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.cas.AlgoSolve;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.geos.HasSymbolicMode;
import org.geogebra.common.kernel.kernelND.GeoPlaneND;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.main.Feature;
import org.geogebra.common.util.IndexHTMLBuilder;

import com.himamis.retex.editor.share.util.Unicode;

/**
 * Utitlity class for AV items
 */
public class AlgebraItem {
	private static String undefinedVariables;

	/**
	 * Changes the symbolic flag of a geo or its parent algo
	 * 
	 * @param geo
	 *            element that we want to change
	 * @return whether it's symbolic after toggle
	 */
	public static boolean toggleSymbolic(GeoElement geo) {

		if (geo instanceof HasSymbolicMode) {
			if (geo.getParentAlgorithm() instanceof AlgoSolve) {
				return !((AlgoSolve) geo.getParentAlgorithm()).toggleNumeric();
			}
			((HasSymbolicMode) geo).setSymbolicMode(
					!((HasSymbolicMode) geo).isSymbolicMode(), true);
			geo.updateRepaint();
			return ((HasSymbolicMode) geo).isSymbolicMode();

		}
		return false;
	}

	public static String getOutputPrefix(GeoElement geo) {
		if (geo instanceof HasSymbolicMode
				&& !((HasSymbolicMode) geo).isSymbolicMode()) {
			if (!(geo.getParentAlgorithm() instanceof AlgoSolve)
					|| ((AlgoSolve) geo.getParentAlgorithm())
							.getClassName() == Commands.NSolve) {
				return Unicode.CAS_OUTPUT_NUMERIC + "";
			}
			
		}

		return getSymbolicPrefix(geo.getKernel());
	}

	public static boolean isSymbolicDiffers(GeoElement geo) {
		if (!(geo instanceof HasSymbolicMode)) {
			return false;
		}

		if (geo.getParentAlgorithm() instanceof AlgoSolve) {
			return !allRHSareIntegers((GeoList) geo);
		}

		HasSymbolicMode sm = (HasSymbolicMode) geo;
		boolean orig = sm.isSymbolicMode();
		String text1 = geo.getLaTeXAlgebraDescription(true,
				StringTemplate.latexTemplate);
		sm.setSymbolicMode(!orig, false);
		String text2 = geo.getLaTeXAlgebraDescription(true,
				StringTemplate.latexTemplate);

		sm.setSymbolicMode(orig, false);
		if (text1 == null) {
			return true;
		}

		return !text1.equals(text2);

	}

	private static boolean allRHSareIntegers(GeoList geo) {
		for (int i = 0; i < geo.size(); i++) {
			if (geo.get(i) instanceof GeoLine
					&& !Kernel.isInteger(((GeoLine) geo.get(i)).getZ())) {
				return false;
			}
			if (geo.get(i) instanceof GeoPlaneND
					&& !Kernel.isInteger(((GeoPlaneND) geo.get(i)).getCoordSys()
							.getEquationVector().getW())) {
				return false;
			}
			if (geo.get(i) instanceof GeoList
					&& !allRHSareIntegers(((GeoList) geo.get(i)))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isGeoFraction(GeoElement geo) {
		return geo instanceof GeoNumeric && geo.getDefinition() != null
				&& geo.getDefinition().isFraction();
	}

	public static Suggestion getSuggestions(GeoElement geo) {
		Suggestion sug = null;
		if (undefinedVariables != null) {
			sug = SuggestionSlider.get();
			if (sug != null) {
				return sug;
			}
		}
		if (geo == null || geo.getKernel()
				.getAlgebraStyle() != Kernel.ALGEBRA_STYLE_DEFINITION_AND_VALUE) {
			return null;
		}

		boolean casEnabled = geo.getKernel().getApplication().getSettings()
				.getCasSettings().isEnabled();
		if (casEnabled) {
			sug = SuggestionSolve.get(geo);
			if (sug != null) {
				return sug;
			}

		}
		if (geo.getKernel().getApplication().has(Feature.SHOW_STEPS)
				&& casEnabled) {
			sug = SuggestionSteps.get(geo);

			if (sug != null) {
				return sug;
			}
		}

		sug = SuggestionRootExtremum.get(geo);
		if (sug != null) {
			return sug;
		}

		return null;
	}

	public static String getSymbolicPrefix(Kernel kernel) {
		return kernel.getLocalization().rightToLeftReadingOrder
				? Unicode.CAS_OUTPUT_PREFIX_RTL + ""
				: Unicode.CAS_OUTPUT_PREFIX + "";
	}

	public static boolean needsPacking(GeoElement geo) {
		return geo != null && geo.getParentAlgorithm() != null
				&& geo.getParentAlgorithm().getOutput().length > 1
				&& geo.getKernel().getApplication().getSettings().getAlgebra()
						.getTreeMode() == SortMode.ORDER;
	}

	public static String getUndefinedValiables() {
		return undefinedVariables;
	}

	public static void setUndefinedValiables(String undefinedValiables) {
		AlgebraItem.undefinedVariables = undefinedValiables;
	}

	public static String getDuplicateFormulaForGeoElement(GeoElement element) {
		String duplicate = "";
		if ("".equals(element.getDefinition(StringTemplate.defaultTemplate))) {
			duplicate = element.getValueForInputBar();
		} else {
			duplicate = element.getDefinitionNoLabel(
					StringTemplate.editorTemplate);
		}

		return duplicate;
	}

	public static String getOutputTextForGeoElement(GeoElement element) {
		String outputText = "";
		if (element.isLaTeXDrawableGeo()
				|| AlgebraItem.isGeoFraction(element)) {
			outputText = element.getLaTeXDescriptionRHS(true,
					StringTemplate.latexTemplate);
		} else {
			if (needsPacking(element)) {
				outputText = element.getAlgebraDescriptionDefault();
			} else {
				outputText = element.getAlgebraDescriptionRHS();
			}
		}

		return outputText;
	}

	public static boolean isCompactItem(GeoElement element) {
		return element != null && element.getParentAlgorithm() != null
				&& element.getParentAlgorithm().getOutput(0) != element
				&& element.getKernel().getApplication().getSettings().getAlgebra()
				.getTreeMode() == SortMode.ORDER;
	}

	public static boolean buildPlainTextItemSimple(GeoElement geo1,
			IndexHTMLBuilder builder) {
		int avStyle = geo1.getKernel().getAlgebraStyle();
		if (geo1.isIndependent() && geo1.getDefinition() == null) {
			geo1.getAlgebraDescriptionTextOrHTMLDefault(builder);
			return true;
		}
		if (geo1.isIndependent() && geo1.isGeoPoint()
				&& avStyle == Kernel.ALGEBRA_STYLE_DESCRIPTION
				&& geo1.getKernel().getApplication()
						.has(Feature.GEO_AV_DESCRIPTION)) {
			builder.clear();
			builder.append(((GeoPointND) geo1)
					.toStringDescription(StringTemplate.defaultTemplate));
			return true;
		}
		switch (avStyle) {
		case Kernel.ALGEBRA_STYLE_VALUE:
			geo1.getAlgebraDescriptionTextOrHTMLDefault(builder);
			return true;

		case Kernel.ALGEBRA_STYLE_DESCRIPTION:
			geo1.addLabelTextOrHTML(geo1.getDefinitionDescription(
					StringTemplate.defaultTemplate), builder);
			return true;

		case Kernel.ALGEBRA_STYLE_DEFINITION:
			geo1.addLabelTextOrHTML(
					geo1.getDefinition(StringTemplate.defaultTemplate),
					builder);
			return true;
		default:
		case Kernel.ALGEBRA_STYLE_DEFINITION_AND_VALUE:

			return false;
		}

	}

	public static boolean isTextItem(GeoElement geo) {
		return geo instanceof GeoText && !((GeoText) geo).isLaTeX()
				&& !(geo).isTextCommand();
	}

	public static boolean shouldShowSymbolicOutputButton(GeoElement geo) {
		return isSymbolicDiffers(geo) && !isTextItem(geo);
	}
}

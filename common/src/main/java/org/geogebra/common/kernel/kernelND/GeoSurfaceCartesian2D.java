package org.geogebra.common.kernel.kernelND;

import java.util.TreeMap;

import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Matrix.Coords3;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.ExpressionValue;
import org.geogebra.common.kernel.arithmetic.FunctionNVar;
import org.geogebra.common.kernel.arithmetic.ValueType;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoVec2D;
import org.geogebra.common.plugin.GeoClass;

public class GeoSurfaceCartesian2D extends GeoSurfaceCartesianND {

	public GeoSurfaceCartesian2D(Construction c) {
		super(c);
	}

	public GeoSurfaceCartesian2D(Construction cons, ExpressionNode point,
			FunctionNVar[] fun) {
		super(cons, point, fun);
	}

	public void evaluatePoint(double u, double v, Coords3 point) {
		double[] tmp = { u, v };
		point.set(fun[0].evaluate(tmp), fun[1].evaluate(tmp), 0);
	}

	public boolean evaluateNormal(Coords3 p, double u, double v,
			Coords3 normal) {
		p.set(0, 0, 1);
		return true;
	}

	public ValueType getValueType() {
		return ValueType.PARAMETRIC2D;
	}

	public void printCASEvalMapXML(StringBuilder sb) {
		// TODO Auto-generated method stub

	}

	public void updateCASEvalMap(TreeMap<String, String> casMap) {
		// TODO Auto-generated method stub

	}

	@Override
	public GeoClass getGeoClassType() {
		return GeoClass.SURFACECARTESIAN3D;
	}

	@Override
	public GeoElement copy() {
		GeoSurfaceCartesian2D ret = new GeoSurfaceCartesian2D(cons);
		ret.set(this);
		return ret;
	}

	@Override
	public void set(GeoElementND geo) {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean showInEuclidianView() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEqual(GeoElementND geo) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public HitType getLastHitType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExpressionValue evaluateSurface(double u, double v) {
		double[] tmp = { u, v };

		return new GeoVec2D(kernel, fun[0].evaluate(tmp), fun[1].evaluate(tmp));
	}

	@Override
	public boolean hasDrawable3D() {
		return true;
	}

}

package org.geogebra.common.kernel.cas;

import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.arithmetic.Command;
import org.geogebra.common.kernel.commands.CommandProcessor;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoFunction;
import org.geogebra.common.kernel.kernelND.GeoConicND;
import org.geogebra.common.main.MyError;

/**
 * Coefficients
 */
public class CmdCoefficients extends CommandProcessor {

	/**
	 * Create new command processor
	 * 
	 * @param kernel
	 *            kernel
	 */
	public CmdCoefficients(Kernel kernel) {
		super(kernel);
	}

	@Override
	final public GeoElement[] process(Command c) throws MyError {
		int n = c.getArgumentNumber();
		GeoElement[] arg;
		arg = resArgs(c);

		switch (n) {
		case 1:
			if ((arg[0].isGeoFunction())) {

				AlgoCoefficients algo = new AlgoCoefficients(cons, c.getLabel(),
						(GeoFunction) arg[0]);
				GeoElement[] ret = { algo.getResult() };
				return ret;
			} else if ((arg[0].isGeoConic())) {

				AlgoConicCoefficients algo = new AlgoConicCoefficients(cons,
						c.getLabel(), (GeoConicND) arg[0]);
				GeoElement[] ret = { algo.getResult() };
				return ret;
			} else
				throw argErr(app, c, arg[0]);

			// more than one argument
		default:
			throw argNumErr(app, c.getName(), n);
		}
	}
}

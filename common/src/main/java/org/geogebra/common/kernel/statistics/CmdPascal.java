package org.geogebra.common.kernel.statistics;

import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.arithmetic.Command;
import org.geogebra.common.kernel.commands.CommandProcessor;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.main.MyError;

/**
 * Pascal
 */
public class CmdPascal extends CommandProcessor {

	/**
	 * Create new command processor
	 * 
	 * @param kernel
	 *            kernel
	 */
	public CmdPascal(Kernel kernel) {
		super(kernel);
	}

	@Override
	public GeoElement[] process(Command c) throws MyError {
		int n = c.getArgumentNumber();
		boolean[] ok = new boolean[n];
		GeoElement[] arg;

		switch (n) {

		case 2:
			arg = resArgs(c);
			if ((ok[0] = arg[0] instanceof GeoNumberValue)
					&& (ok[1] = arg[1] instanceof GeoNumberValue)) {

				AlgoPascalBarChart algo = new AlgoPascalBarChart(cons,
						c.getLabel(), (GeoNumberValue) arg[0],
						(GeoNumberValue) arg[1]);

				GeoElement[] ret = { algo.getSum() };
				return ret;

			} else if (!ok[0])
				throw argErr(app, c, arg[0]);
			else
				throw argErr(app, c, arg[1]);

		case 3:
			arg = resArgs(c);
			if ((ok[0] = arg[0] instanceof GeoNumberValue)
					&& (ok[1] = arg[1] instanceof GeoNumberValue)
					&& (ok[2] = arg[2].isGeoBoolean())) {

				AlgoPascalBarChart algo = new AlgoPascalBarChart(cons,
						c.getLabel(), (GeoNumberValue) arg[0],
						(GeoNumberValue) arg[1], (GeoBoolean) arg[2]);

				GeoElement[] ret = { algo.getSum() };
				return ret;

			} else if (!ok[0])
				throw argErr(app, c, arg[0]);
			else if (!ok[1])
				throw argErr(app, c, arg[1]);
			else
				throw argErr(app, c, arg[2]);

		case 4:
			arg = resArgs(c);
			if ((ok[0] = arg[0] instanceof GeoNumberValue)
					&& (ok[1] = arg[1] instanceof GeoNumberValue)
					&& (ok[2] = arg[2] instanceof GeoNumberValue)
					&& (ok[3] = arg[3].isGeoBoolean())) {

				AlgoPascal algo = new AlgoPascal(cons, c.getLabel(),
						(GeoNumberValue) arg[0], (GeoNumberValue) arg[1],
						(GeoNumberValue) arg[2], (GeoBoolean) arg[3]);

				GeoElement[] ret = { algo.getResult() };
				return ret;

			} else if (!ok[0])
				throw argErr(app, c, arg[0]);
			else if (!ok[1])
				throw argErr(app, c, arg[1]);
			else if (!ok[2])
				throw argErr(app, c, arg[2]);
			else
				throw argErr(app, c, arg[3]);

		default:
			throw argNumErr(app, c.getName(), n);
		}
	}
}

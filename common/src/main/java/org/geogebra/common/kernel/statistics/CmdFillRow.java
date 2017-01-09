package org.geogebra.common.kernel.statistics;

import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.arithmetic.Command;
import org.geogebra.common.kernel.commands.CommandProcessor;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.main.MyError;

/**
 * FillRow
 */
public class CmdFillRow extends CommandProcessor {

	/**
	 * Create new command processor
	 * 
	 * @param kernel
	 *            kernel
	 */
	public CmdFillRow(Kernel kernel) {
		super(kernel);
	}

	@Override
	final public GeoElement[] process(Command c) throws MyError {
		int n = c.getArgumentNumber();
		boolean[] ok = new boolean[n];
		GeoElement[] arg;

		switch (n) {
		case 2:
			arg = resArgs(c);
			if ((ok[0] = (arg[0].isGeoNumeric()))
					&& (ok[1] = (arg[1].isGeoList()))) {

				int row = -1 + (int) ((GeoNumeric) arg[0]).getDouble();

				if (row < 0 || row > Kernel.MAX_SPREADSHEET_ROWS_DESKTOP)
					throw argErr(app, c, arg[0]);

				GeoList list = (GeoList) arg[1];

				GeoElement[] ret = { list };

				if (list.size() == 0)
					return ret;

				for (int col = 0; col < list.size(); col++) {

					GeoElement cellGeo = list.get(col).copy();

					try {
						kernelA.getGeoElementSpreadsheet()
								.setSpreadsheetCell(app, row, col, cellGeo);
					} catch (Exception e) {
						e.printStackTrace();
						throw argErr(app, c, arg[1]);
					}

				}

				app.storeUndoInfo();
				return ret;

			} else if (!ok[0])
				throw argErr(app, c, arg[0]);
			else
				throw argErr(app, c, arg[1]);

		default:
			throw argNumErr(app, c.getName(), n);
		}
	}
}

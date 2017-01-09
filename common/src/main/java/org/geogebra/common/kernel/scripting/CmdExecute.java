package org.geogebra.common.kernel.scripting;

import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.arithmetic.Command;
import org.geogebra.common.kernel.commands.CmdScripting;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.main.MyError;

/**
 * Execute[<list of commands>]
 */
public class CmdExecute extends CmdScripting {

	/**
	 * Create new command processor
	 * 
	 * @param kernel
	 *            kernel
	 */
	public CmdExecute(Kernel kernel) {
		super(kernel);
	}

	@Override
	final public GeoElement[] perform(Command c) throws MyError {
		int n = c.getArgumentNumber();
		GeoElement[] arg;
		arg = resArgs(c);
		if (n > 10)
			throw argNumErr(app, c.getName(), n);
		if (arg[0].isGeoList() && ((GeoList) arg[0]).size() == 0
				|| !arg[0].isDefined())
			return new GeoElement[] {};
		if ((!arg[0].isGeoList()) || (!((GeoList) arg[0])
				.getGeoElementForPropertiesDialog().isGeoText()))
			throw argErr(app, c, arg[0]);
		GeoList list = (GeoList) arg[0];

		// this is new in GeoGebra 4.2 and it will stop some files working
		// but causes problems if the files are opened and edited
		// and in the web project
		boolean oldVal = kernelA.isUsingInternalCommandNames();
		kernelA.setUseInternalCommandNames(true);

		for (int i = 0; i < list.size(); i++) {
			try {
				String cmdText = ((GeoText) list.get(i)).getTextString();
				for (int k = 1; k < n; k++)
					cmdText = cmdText.replace("%" + k,
							arg[k].getLabel(StringTemplate.maxPrecision));
				kernelA.getAlgebraProcessor()
						.processAlgebraCommandNoExceptionHandling(cmdText,
								false, app.getErrorHandler(), false, null);
			} catch (MyError e) {
				app.showError(e);
				break;
			} catch (Exception e) {
				app.showError(e.getLocalizedMessage());
				e.printStackTrace();
				break;
			}
		}

		kernelA.setUseInternalCommandNames(oldVal);

		app.storeUndoInfo();
		return arg;

	}
}

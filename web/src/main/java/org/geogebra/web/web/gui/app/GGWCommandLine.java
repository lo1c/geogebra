package org.geogebra.web.web.gui.app;

import org.geogebra.common.main.App;
import org.geogebra.web.html5.main.AppW;
import org.geogebra.web.web.gui.inputbar.AlgebraInputW;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RequiresResize;

/**
 * Wraps the input bar
 *
 */
public class GGWCommandLine extends Composite implements RequiresResize{
	
	private AlgebraInputW algebraInput;

	/**
	 * Create new input bar wrapper
	 */
	public GGWCommandLine() {
		algebraInput = new AlgebraInputW();
		initWidget(algebraInput);
	}

	/**
	 * @param app
	 *            application
	 */
	public void attachApp(App app) {
	    algebraInput.init((AppW) app);
    }

	@Override
	public void onResize() {
		algebraInput.onResize();
    }

	/**
	 * @return whether input bar has focus
	 */
	public boolean hasFocus() {
		return algebraInput.hasFocus();
    }
}

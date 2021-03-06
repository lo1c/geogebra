package org.geogebra.web.web.gui.properties;

import org.geogebra.common.main.App;
import org.geogebra.web.html5.gui.util.AriaHelper;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.MenuItem;

/**
 * Button for properties stylebar
 *
 */
public class PropertiesButton extends MenuItem {

	private App app;

	/**
	 * @param app
	 *            application
	 * @param text
	 *            content
	 * @param cmd
	 *            action
	 */
	public PropertiesButton(App app, String text, Command cmd) {
		super(text, true, cmd);
		setApp(app);
	}

	/**
	 * @return application
	 */
	public App getApp() {
		return app;
	}

	/**
	 * @param app
	 *            application
	 */
	public void setApp(App app) {
		this.app = app;
	}

	@Override
	public void setTitle(String title) {
		AriaHelper.setTitle(this, title, app);
	}
}

package org.geogebra.web.web.gui.menubar;

import org.geogebra.web.html5.main.AppW;

/**
 * Help menu
 */
public class DownloadMenuW extends GMenuBar implements MenuBarI {
	/**
	 * @param app
	 *            application
	 */
	public DownloadMenuW(final AppW app) {
		super(true, "DownloadAs", app);
		if (app.isUnbundledOrWhiteboard()) {
			addStyleName("matStackPanel");
		} else {
			addStyleName("GeoGebraMenuBar");
		}
		ExportMenuW.initActions(this, app);
	}


	public void hide() {
		// no hiding needed
	}
}


package org.geogebra.web.web.gui.app;

import org.geogebra.common.euclidian.event.PointerEventType;
import org.geogebra.common.main.Feature;
import org.geogebra.keyboard.web.KeyboardResources;
import org.geogebra.keyboard.web.UpdateKeyBoardListener;
import org.geogebra.web.html5.gui.util.ClickStartHandler;
import org.geogebra.web.html5.gui.util.MathKeyboardListener;
import org.geogebra.web.html5.gui.util.NoDragImage;
import org.geogebra.web.html5.main.AppW;
import org.geogebra.web.keyboard.OnscreenTabbedKeyboard;
import org.geogebra.web.web.gui.layout.DockManagerW;
import org.geogebra.web.web.gui.layout.DockPanelW;
import org.geogebra.web.web.gui.layout.panels.AlgebraPanelInterface;
import org.geogebra.web.web.gui.view.spreadsheet.SpreadsheetViewW;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A PopupPanel in the bottom left corner of the application which represents a
 * button to open the {@link OnscreenTabbedKeyboard}
 */
public class ShowKeyboardButton extends SimplePanel {
	
	private Widget parent;
	private AppW app;

	// MathKeyboardListener mathKeyboardListener;

	/**
	 * @param listener
	 *            {@link UpdateKeyBoardListener}
	 * @param dm
	 *            {@link DockManagerW}
	 * @param parent
	 *            {@link Element}
	 * @param app
	 *            app
	 */
	public ShowKeyboardButton(final UpdateKeyBoardListener listener,
			final DockManagerW dm, Widget parent, final AppW app) {
		this.app = app;
		this.parent = parent;
		this.addStyleName(app.isUnbundled() ? "matOpenKeyboardBtn"
				: "openKeyboardButton");
		NoDragImage showKeyboard = new NoDragImage(app.isUnbundled()
				? KeyboardResources.INSTANCE.keyboard_show_material()
						.getSafeUri().asString()
				: KeyboardResources.INSTANCE
		        .keyboard_show().getSafeUri().asString());
		this.add(showKeyboard);

		//TODO: app paramater used only for feature checking so this can be removed later
		if (app.has(Feature.SHOW_ONE_KEYBOARD_BUTTON_IN_FRAME)) {
			if (listener instanceof ComplexPanel){
				((ComplexPanel)listener).add(this);
			}
		} else {
			if (parent instanceof DockPanelW) {
				((DockPanelW) parent).addSouth(this);
			}				
		}
		ClickStartHandler.init(this, new ClickStartHandler(
		        true, true) {

			@Override
			public void onClickStart(int x, int y, PointerEventType type) {
				DockPanelW panel = dm.getPanelForKeyboard();
				final MathKeyboardListener mathKeyboardListener = panel
						.getKeyboardListener();
						
				if (app.has(Feature.KEYBOARD_BEHAVIOUR)
						&& (panel instanceof AlgebraPanelInterface)) {
					listener.doShowKeyBoard(true,
							((AlgebraPanelInterface) panel)
									.updateKeyboardListener(
											mathKeyboardListener));


				} else {
					listener.doShowKeyBoard(true, mathKeyboardListener);
				}

				if ((dm.getApp() == null)
						|| (dm.getApp().getGuiManager() == null)) {
					// e.g. AppStub, Android device, do the old way
					Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

						@Override
						public boolean execute() {
							if (mathKeyboardListener != null) {
								mathKeyboardListener.ensureEditing();
								mathKeyboardListener.setFocus(true, false);
							}
							return false;
						}
					}, 0);
				} else {

					if (dm.getApp().getGuiManager().hasSpreadsheetView()) {
						((SpreadsheetViewW) dm.getApp().getGuiManager()
								.getSpreadsheetView())
								.setKeyboardEnabled(true);
					}

					// TODO: check why scheduleFixedDelay is needed,
					// would not scheduleDeferred or something like that better?
					// but it's probably Okay, as the method returns false

					dm.getApp().getGuiManager()
							.focusScheduled(false, false, false);

					Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

						@Override
						public boolean execute() {
							if (mathKeyboardListener != null) {
								mathKeyboardListener.ensureEditing();
								mathKeyboardListener.setFocus(true, true);
							}
							return false;
						}
					}, 0);
				}

			}
		});
	}

	/**
	 * 
	 * @param show
	 *            {@code true} to show the button to open the OnScreenKeyboard
	 * @param textField
	 *            {@link Widget} to receive the text input
	 */
	public void show(boolean show, MathKeyboardListener textField) {

		if (show && (parent.isVisible()
				|| app.has(Feature.SHOW_ONE_KEYBOARD_BUTTON_IN_FRAME))) {
			setVisible(true);
		} else {
			setVisible(false);
		}

	}
	/**
	 * Hide the button
	 */
	public void hide() {
		setVisible(false);
	}
}

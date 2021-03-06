package org.geogebra.common.euclidian3D;

import org.geogebra.common.euclidian.EuclidianViewInterfaceCommon;
import org.geogebra.common.kernel.Matrix.Coords;

/**
 * 
 * @author mathieu
 *
 *         Interface for 3D view
 */
public interface EuclidianView3DInterface extends EuclidianViewInterfaceCommon {

	/**
	 * rotate to default
	 */
	public void setDefaultRotAnimation();

	/**
	 * start a rotation animation to be in the vector direction
	 * 
	 * @param vn
	 *            vector direction
	 * @param checkSameValues
	 *            say if we check already in vector direction, to swap the view
	 * @param animated
	 *            say if rotation will be animated
	 */
	public void setRotAnimation(Coords vn, boolean checkSameValues,
			boolean animated);

	/**
	 * start a rotation animation to set angle around Oz axis
	 * 
	 * @param rotOz
	 *            angle around Oz
	 * @param checkSameValues
	 *            say if we check already in vector direction, to swap the view
	 * @param animated
	 *            say if rotation will be animated
	 */
	public void setRotAnimation(double rotOz, boolean checkSameValues,
			boolean animated);

	/**
	 * start a rotation animation to be in the vector direction, shortest way
	 * 
	 * @param v
	 *            vector direction
	 * @param animated
	 *            say if rotation will be animated
	 */
	public void setClosestRotAnimation(Coords v, boolean animated);

	/**
	 * @return Returns the zmin.
	 */
	public double getZmin();

	/**
	 * @return Returns the zmax.
	 */
	public double getZmax();

	/**
	 * sets the use of the clipping cube
	 * 
	 * @param flag
	 *            flag
	 */
	public void setUseClippingCube(boolean flag);

	/**
	 * sets if the clipping cube is shown
	 * 
	 * @param flag
	 *            flag
	 */
	public void setShowClippingCube(boolean flag);

	/**
	 * sets the reduction of the clipping box
	 * 
	 * @param value
	 *            reduction
	 */
	public void setClippingReduction(int value);

	/**
	 * 
	 * @param projection
	 *            projection type
	 */
	public void setProjection(int projection);

	/**
	 * sets the visibility of xOy plane grid
	 * 
	 * @param flag
	 *            flag
	 * @return whether it changed
	 */
	public boolean setShowGrid(boolean flag);

	/**
	 * sets the visibility of xOy plane
	 * 
	 * @param flag
	 *            flag
	 */
	public void setShowPlane(boolean flag);

	/**
	 * sets the visibility of xOy plane plate
	 * 
	 * @param flag
	 *            flag
	 */
	public void setShowPlate(boolean flag);

	/**
	 * sets the rotation matrix
	 * 
	 * @param theta
	 *            argument
	 * @param phi
	 *            alt angle
	 */
	public void setRotXYinDegrees(double theta, double phi);

	/**
	 * sets the origin
	 * 
	 * @param x
	 *            x coord
	 * @param y
	 *            y coord
	 * @param z
	 *            z coord
	 */
	public void setZeroFromXML(double x, double y, double z);

	/**
	 * set Matrix for view3D
	 */
	public void updateMatrix();

	/**
	 * tells the view it has changed
	 */
	public void setViewChanged();

	/**
	 * tell the view that it has to be updated
	 * 
	 */
	public void setWaitForUpdate();

	/**
	 * set if y axis is up (and not z axis)
	 * 
	 * @param flag
	 *            flag
	 */
	public void setYAxisVertical(boolean flag);

	/**
	 * @return screen z-coord of origin
	 */
	public double getZZero();

	/**
	 * update all drawables
	 */
	public void updateAllDrawables();

	/**
	 * 
	 * @return eye position
	 */
	public Coords getEyePosition();

	/**
	 * @param boundsMin2
	 *            real world view min
	 * @param boundsMax2
	 *            real world view max
	 */
	public void zoomRW(Coords boundsMin2, Coords boundsMax2);

	/**
	 * @return whether stylebar exists
	 */
	public boolean hasStyleBar();
	
	/**
	 * set flag so a Solid CAD export will be done on next 3D frame
	 */
	public void setFlagForSCADexport();
	
	/**
	 * zoom y & z axes ratio regarding x axis
	 * @param zoomFactorY
	 * @param zoomFactorZ
	 */
	public void zoomAxesRatio(double zoomFactorY, double zoomFactorZ);

}

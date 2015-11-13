/**
 * 
 */
package tileReaderInputs;

import ij.ImagePlus;
import ij.gui.Roi;

import java.awt.Point;

import settings.BasicSettings;

/**
 * @author George Kritikos
 *
 */
public class OpacityTileReaderInput extends BasicTileReaderInput {

	public Roi colonyRoi;
	public int colonySize;
	
	/**
	 * @param tileImage_
	 * @param settings_
	 */
	public OpacityTileReaderInput(ImagePlus tileImage_, BasicSettings settings_) {
		super(tileImage_, settings_);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param tileImage_
	 * @param roi_
	 * @param settings_
	 */
	public OpacityTileReaderInput(ImagePlus croppedImage_, Roi tile_roi_, BasicSettings settings_) {
		super(croppedImage_, tile_roi_, settings_);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param tileImage_
	 * @param roi_
	 * @param settings_
	 */
	public OpacityTileReaderInput(ImagePlus croppedImage_, Roi tile_roi_, Roi colonyRoi_, int colonySize_, BasicSettings settings_) {
		super(croppedImage_, tile_roi_, settings_);
		this.colonyRoi = colonyRoi_;
		this.colonySize = colonySize_;
		// TODO Auto-generated constructor stub
	}
	
	public OpacityTileReaderInput(BasicTileReaderInput that){
		super(that.tileImage, that.settings);
		this.colonyCenter = new Point(that.colonyCenter);
	}


}

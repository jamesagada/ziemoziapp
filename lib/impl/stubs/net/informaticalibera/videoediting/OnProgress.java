package net.informaticalibera.videoediting;


/**
 *  Invoked automatically every second while the video optimization is in
 *  progress, this can be used to update the UI with the progress percentage.
 * 
 *  @author Francesco Galgani
 */
public interface OnProgress {

	/**
	 *  Invoked to update the UI with the progress percentage.
	 * 
	 *  @param v the value of the percentage (0 to 99).
	 */
	public void update(int v);
}

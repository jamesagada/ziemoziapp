package net.informaticalibera.videoediting;


/**
 *  @deprecated
 *  <p>
 *  This class is deprecated because you shouldn't use it directly.</p>
 *  @author Francesco Galgani
 */
public class VideoEditingCallbacks {

	public VideoEditingCallbacks() {
	}

	/**
	 *  Sets a success callback for the given inputVideoFile, that is called when
	 *  the optimization is completed.
	 * 
	 *  @param input, it ignores the path, it consider only the file name, that
	 *  must to be unique!
	 *  @param optimizeVideoForUploadCallback callback
	 */
	public static void setOptimizedVideoCallback(String input, Runnable optimizeVideoForUploadCallback) {
	}

	/**
	 *  Sets an error callback for the given inputVideoFile, that is called when
	 *  the optimization fails.
	 * 
	 *  @param input, it ignores the path, it consider only the file name, that
	 *  must to be unique!
	 *  @param errorCallback callback
	 */
	public static void setVideoErrorCallback(String input, Runnable errorCallback) {
	}

	/**
	 *  This method is invoked by the native code.
	 * 
	 *  @param input, it ignores the path, it consider only the file name, that
	 *  must to be unique!
	 */
	public static void runVideoOptimizedCallback(String input) {
	}

	/**
	 *  This method is invoked by the native code.
	 * 
	 *  @param input, it ignores the path, it consider only the file name, that
	 *  must to be unique!
	 */
	public static void runVideoErrorCallback(String input) {
	}

	/**
	 *  Get the percentage of the progress
	 * 
	 *  @return a number from 0 to 99
	 */
	public static int getProgress() {
	}

	/**
	 *  Invoked by the native code
	 * 
	 *  @param progress percentage
	 */
	public static void setProgress(int progress) {
	}

	/**
	 *  Invoked by the native code
	 * 
	 *  @param toBeLogged by the EDT
	 */
	public static void logsFromNative(String toBeLogged) {
	}
}

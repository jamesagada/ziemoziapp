package net.informaticalibera.videoediting;


/**
 *  Video Optimizer CN1Lib public layer class: use this class to get info about a
 *  video, to get a preview of a video and to optimize a video for fast upload.
 * 
 *  @author Francesco Galgani
 */
public class VideoOptimizer {

	public VideoOptimizer() {
	}

	/**
	 *  Returns the duration of the videoFile in seconds, or -1 in case of error
	 * 
	 *  @param videoFile placed in FileSystemStorage
	 *  @return duration in seconds, or -1
	 */
	public long getVideoDuration(String videoFile) {
	}

	/**
	 *  Extract a frame from the given video and returns a jpeg file, or null in
	 *  case of error
	 * 
	 *  @param videoFile placed in FileSystemStorage
	 *  @return the jpeg preview placed in FileSystemStorage (or null in case of
	 *  error), it ensures to don't overwrite any existing file
	 */
	public String getVideoPreview(String videoFile) {
	}

	/**
	 *  Get the average bitrate (in bits/sec), or -1 in case of error.
	 * 
	 *  @param videoFile placed in FileSystemStorage
	 *  @return average bitrate (in bits/sec), or -1 in case of error
	 */
	public int getVideoBitrate(String videoFile) {
	}

	/**
	 *  Returns the size of the given video, or null in case of error
	 * 
	 *  @param videoFile placed in FileSystemStorage
	 *  @return the size of the given video, or null in case of error
	 */
	public com.codename1.ui.geom.Dimension getVideoSize(String videoFile) {
	}

	/**
	 *  <p>
	 *  Asynchronous method to optimize a video for fast upload and maximum
	 *  compatibility: it produces a new mp4 file placed in FileSystemStorage (it
	 *  ensures to don't overwrite any existing file); note that it produces
	 *  always an "mp4" file even if the source video is in QuickTime format.
	 *  </p>
	 *  <p>
	 *  This method is cpu-intensive and it can require a time to complete longer
	 *  than the video duration.
	 *  </p>
	 *  <p>
	 *  It throws an IllegalStateException if you invoke this method when there
	 *  is an ongoing video optimization: you must wait that it finishes before
	 *  invoking this method again.
	 *  </p>
	 * 
	 *  @param videoFile placed in FileSystemStorage
	 *  @param onCompleteCallback executed when the saving of the new video is
	 *  completed
	 *  @param onFailureCallback generic callback in case of error, note that
	 *  errors are logged by the EDT
	 *  @param onProgressCallback generic callback invoked every second to update
	 *  the UI with the progress percentage (from 0 to 99)
	 */
	public void optimizeVideoForUpload(String videoFile, com.codename1.util.OnComplete onCompleteCallback, Runnable onFailureCallback, OnProgress onProgressCallback) {
	}
}

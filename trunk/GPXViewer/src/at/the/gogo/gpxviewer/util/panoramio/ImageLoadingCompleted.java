package at.the.gogo.gpxviewer.util.panoramio;

import at.the.gogo.gpxviewer.util.panoramio.ImageForLocationHandler.DownloadPanoImageTask;

public interface ImageLoadingCompleted {

	public void doneLoadingImage(DownloadPanoImageTask task);

}

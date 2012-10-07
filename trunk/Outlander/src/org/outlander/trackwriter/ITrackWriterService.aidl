package org.outlander.trackwriter;

 
interface ITrackWriterService {
    /**
     * Called when the service has a new value for you.
     */

    void newPointRecorded(double lat, double lon);
    
    /**
   * Checks and returns whether we're currently recording a track.
   */
  boolean isRecording();
  
  void startNewTrack();
}

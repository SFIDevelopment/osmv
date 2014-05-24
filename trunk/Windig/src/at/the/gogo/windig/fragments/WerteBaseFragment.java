package at.the.gogo.windig.fragments;

import java.util.List;

import at.the.gogo.windig.dto.WindEntry;
import at.the.gogo.windig.notifications.DataImportReady;

import com.squareup.otto.Subscribe;

public class WerteBaseFragment  {

	
	
	@Subscribe
	public void answerFromDataImport(DataImportReady event) {

	}

	
	void onPostExecuteGetData( final List<WindEntry> entries)
	{
		
	}
		

	
	
	
	
	
	
	
	
}

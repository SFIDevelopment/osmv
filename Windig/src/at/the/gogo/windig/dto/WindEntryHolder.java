package at.the.gogo.windig.dto;

import java.util.ArrayList;
import java.util.List;

import at.the.gogo.windig.util.ParseWebpage;
import at.the.gogo.windig.util.WindInfoHandler;

public class WindEntryHolder {

	public final static int MAX_SITES = WindInfoHandler.site_urls.length;

	private static WindEntryHolder instance;
	private final List<List<WindEntry>> werteListen;

	private WindEntryHolder() {
		werteListen = new ArrayList<List<WindEntry>>();

		for (int i = 0; i < WindEntryHolder.MAX_SITES; i++) {
			werteListen.add(null);
		}

	}

	public static WindEntryHolder getInstance() {
		if (WindEntryHolder.instance == null) {
			WindEntryHolder.instance = new WindEntryHolder();
		}
		return WindEntryHolder.instance;
	}

	public List<WindEntry> getEntries(final int index, final boolean forceNew) {
		List<WindEntry> entries = null;

		if ((werteListen.get(index) == null) || (forceNew)) {
			werteListen
					.set(index, ParseWebpage
							.parseWebpage(WindInfoHandler.site_urls[index]));
		}
		entries = werteListen.get(index);
		return entries;
	}

	public WindEntry getLastEntry(final int index, final boolean force) {
		WindEntry lastEntry = null;

		final List<WindEntry> entries = getEntries(index, force);

		if ((entries != null) && (entries.size() > 0)) {
			lastEntry = entries.get(entries.size() - 1);
		}
		return lastEntry;
	}

}

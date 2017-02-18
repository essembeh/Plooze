package org.essembeh.plooze.core.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class M3UPlaylist {

	public static M3UPlaylist parse(List<String> lines) {
		M3UPlaylist out = new M3UPlaylist();
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith(M3UEntry.COMMENT_PREFIX) && (i + 1) < lines.size()) {
				String url = lines.get(i + 1);
				out.entries.add(M3UEntry.parse(url, line));
				i++;
			}
		}
		return out;
	}

	private final List<M3UEntry> entries = new ArrayList<>();

	public List<M3UEntry> getEntries() {
		return entries;
	}

	public Optional<M3UEntry> getHighestBandwidth() {
		return entries.stream().collect(Collectors.maxBy(Comparator.comparingInt(M3UEntry::getBandwidth)));
	}

	public Optional<M3UEntry> getLowestBandwidth() {
		return entries.stream().collect(Collectors.minBy(Comparator.comparingInt(M3UEntry::getBandwidth)));
	}
}

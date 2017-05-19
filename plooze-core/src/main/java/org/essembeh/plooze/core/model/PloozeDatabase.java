package org.essembeh.plooze.core.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.zip.ZipException;

import org.apache.commons.lang3.StringUtils;
import org.essembeh.plooze.core.utils.PloozeUtils;

public class PloozeDatabase {

	private final List<Episode> episodes = new ArrayList<>();

	public List<Episode> getEpisodes() {
		return new ArrayList<>(episodes);
	}

	public void refresh(Channel... channels) throws ZipException, IOException {
		episodes.clear();
		for (Channel channel : channels) {
			episodes.addAll(PloozeUtils.parseEpisodes(channel.getContentUrl()));
		}
	}

	public List<Episode> search(String arg, String[] fields) {
		List<Episode> out = new ArrayList<>();
		for (Episode episode : episodes) {
			for (String field : fields) {
				if (StringUtils.containsIgnoreCase(episode.getProperty(field), arg)) {
					out.add(episode);
					break;
				}
			}
		}
		return out;
	}

	public String[] getFields() {
		if (episodes.isEmpty()) {
			return null;
		}
		return episodes.get(0).getJson().entrySet().stream().map(Entry::getKey).toArray(String[]::new);
	}

	public Optional<Episode> findById(int id) {
		return getEpisodes().stream().filter(ep -> ep.getId() == id).findFirst();
	}
}

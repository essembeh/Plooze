package org.essembeh.plooze.core.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipException;

import org.apache.commons.lang3.StringUtils;
import org.essembeh.plooze.core.utils.PloozeUtils;

import com.google.gson.JsonObject;

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

	public List<String> getFields() {
		return episodes.stream().map(Episode::getJson).map(JsonObject::entrySet).flatMap(Collection::stream).map(Entry::getKey).filter(StringUtils::isNoneBlank).sorted().distinct()
				.collect(Collectors.toList());
	}

	public Optional<Episode> findById(int id) {
		return getEpisodes().stream().filter(ep -> ep.getId() == id).findFirst();
	}

	public Stream<Episode> stream() {
		return episodes.stream();
	}
}

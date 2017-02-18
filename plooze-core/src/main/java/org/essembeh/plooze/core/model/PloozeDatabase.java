package org.essembeh.plooze.core.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.StringUtils;
import org.essembeh.plooze.core.utils.PloozeUtils;

public class PloozeDatabase {

	private final static String[] FIELDS = { "titre", "sous_titre" };

	private final List<Episode> episodes = new ArrayList<>();

	public List<Episode> getEpisodes() {
		return Collections.unmodifiableList(episodes);
	}

	public void refresh(Path zipFile) throws ZipException, IOException {
		episodes.clear();
		try (ZipFile zf = new ZipFile(zipFile.toFile())) {
			episodes.addAll(PloozeUtils.readZip(zf));
		}
	}

	public List<Episode> search(String arg) {
		List<Episode> out = new ArrayList<>();
		for (Episode episode : episodes) {
			for (String field : FIELDS) {
				if (StringUtils.containsIgnoreCase(episode.getProperty(field), arg)) {
					out.add(episode);
					break;
				}
			}
		}
		return out;
	}

	public Optional<Episode> findById(String id) {
		return getEpisodes().stream().filter(ep -> ep.getId().equals(id)).findFirst();
	}
}

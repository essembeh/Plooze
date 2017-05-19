package org.essembeh.plooze.core.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.essembeh.plooze.core.model.Episode.Quality;
import org.essembeh.plooze.core.model.RemoteResource;
import org.essembeh.plooze.core.model.StreamUrl;

public class PlaylistUtils {

	public static final String COMMENT_PREFIX = "#";
	private static final Pattern BANDWIDTH_PATTERN = Pattern.compile(COMMENT_PREFIX + "EXT-X-STREAM-INF:.*,BANDWIDTH=(?<BANDWIDTH>\\d+)(,.*)?");
	private static final Pattern RESOLUTION_PATTERN = Pattern.compile(COMMENT_PREFIX + "EXT-X-STREAM-INF:.*,RESOLUTION=(?<RESOLUTION>\\d+x\\d+)(,.*)?");

	public static boolean isComment(String line) {
		return line.startsWith(COMMENT_PREFIX);
	}

	public static Optional<Integer> getBandwidth(String line) {
		Matcher matcher = BANDWIDTH_PATTERN.matcher(line);
		if (matcher.matches()) {
			Integer out = Integer.parseInt(matcher.group("BANDWIDTH"));
			return Optional.of(out);
		}
		return Optional.empty();
	}

	public static Optional<String> getResolution(String line) {
		Matcher matcher = RESOLUTION_PATTERN.matcher(line);
		if (matcher.matches()) {
			return Optional.of(matcher.group("RESOLUTION"));
		}
		return Optional.empty();
	}

	public static StreamUrl getStream(RemoteResource playlist, Quality quality) throws MalformedURLException, IOException {
		List<String> lines = playlist.readLines();
		SortedSet<StreamUrl> streams = new TreeSet<>();
		for (int i = 0; i < lines.size() - 1; i++) {
			String comment, url;
			if (PlaylistUtils.isComment(comment = lines.get(i)) && !PlaylistUtils.isComment(url = lines.get(i + 1))) {
				StreamUrl streamUrl = new StreamUrl(url, getBandwidth(comment).get(), getResolution(comment).get());
				streams.add(streamUrl);
			}
		}
		if (streams.isEmpty()) {
			throw new IllegalStateException("Cannot find stream");
		}
		if (quality == Quality.BEST) {
			return streams.last();
		}
		if (quality == Quality.LOWEST) {
			return streams.first();
		}
		return streams.stream().filter(s -> PloozeConstants.MEDIUM_RESOLUTION.equals(s.getResolution())).findFirst()
				.orElseThrow(() -> new IllegalStateException("Cannot find stream with resolution: " + PloozeConstants.MEDIUM_RESOLUTION));
	}
}

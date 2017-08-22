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
	public static final String STREAM_COMMENT_PREFIX = COMMENT_PREFIX + "EXT-X-STREAM-INF";
	private static final Pattern BANDWIDTH_PATTERN = Pattern.compile(STREAM_COMMENT_PREFIX + ":.*,BANDWIDTH=(?<BANDWIDTH>\\d+)(,.*)?");
	private static final Pattern RESOLUTION_PATTERN = Pattern.compile(STREAM_COMMENT_PREFIX + ":.*,RESOLUTION=(?<RESOLUTION>\\d+x\\d+)(,.*)?");

	public static boolean isStreamUrl(String line) {
		return line.startsWith("http://") || line.startsWith("https://");
	}

	public static boolean isStreamInfoComment(String line) {
		return line.startsWith(STREAM_COMMENT_PREFIX);
	}

	public static int getBandwidth(String line) {
		Matcher matcher = BANDWIDTH_PATTERN.matcher(line);
		if (matcher.matches()) {
			return Integer.parseInt(matcher.group("BANDWIDTH"));
		}
		throw new IllegalArgumentException("Cannot find bandwidth in comment: " + line);
	}

	public static Optional<String> getResolution(String line) {
		Matcher matcher = RESOLUTION_PATTERN.matcher(line);
		if (matcher.matches()) {
			return Optional.of(matcher.group("RESOLUTION"));
		}
		return Optional.empty();
	}

	public static Optional<StreamUrl> getStream(RemoteResource playlist, Quality quality) throws MalformedURLException, IOException {
		List<String> lines = playlist.readLines();
		SortedSet<StreamUrl> streams = new TreeSet<>();
		for (int i = 0; i < lines.size() - 1; i++) {
			String comment, url;
			if (PlaylistUtils.isStreamInfoComment(comment = lines.get(i)) && PlaylistUtils.isStreamUrl(url = lines.get(i + 1))) {
				Optional<String> resolution = getResolution(comment);
				if (resolution.isPresent()) {
					StreamUrl streamUrl = new StreamUrl(url, getBandwidth(comment), resolution.get());
					streams.add(streamUrl);
				}
			}
		}
		if (streams.isEmpty()) {
			return Optional.empty();
		}
		if (quality == Quality.BEST) {
			return Optional.of(streams.last());
		}
		if (quality == Quality.LOWEST) {
			return Optional.of(streams.first());
		}
		return streams.stream().filter(s -> PloozeConstants.MEDIUM_RESOLUTION.equals(s.getResolution())).findFirst();
	}
}

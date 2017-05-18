package org.essembeh.plooze.core.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.essembeh.plooze.core.model.RemoteResource;

public class PlaylistUtils {

	public static final String COMMENT_PREFIX = "#";
	private static final Pattern BANDWIDTH_PATTERN = Pattern.compile(COMMENT_PREFIX + "EXT-X-STREAM-INF:.*,BANDWIDTH=(?<BANDWIDTH>\\d+)(,.*)?");

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

	public static String getBestStream(RemoteResource playlist) throws MalformedURLException, IOException {
		int bestBandwith = 0;
		String out = null;
		List<String> lines = playlist.readLines();
		for (int i = 0; i < lines.size() - 1; i++) {
			String comment, url;
			if (PlaylistUtils.isComment(comment = lines.get(i)) && !PlaylistUtils.isComment(url = lines.get(i + 1))) {
				Optional<Integer> bandwidth = PlaylistUtils.getBandwidth(comment);
				if (bandwidth.isPresent()) {
					if (bandwidth.get() > bestBandwith) {
						out = url;
						bestBandwith = bandwidth.get();
					}
				} else {
					out = url;
				}
			}
		}
		if (StringUtils.isBlank(out)) {
			throw new IllegalStateException("Cannot find stream");
		}
		return out;
	}
}

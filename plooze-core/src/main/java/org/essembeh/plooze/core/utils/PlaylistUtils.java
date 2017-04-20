package org.essembeh.plooze.core.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.essembeh.plooze.core.model.Episode;
import org.essembeh.plooze.core.model.RemoteResource;

public class PlaylistUtils {

	public static final String COMMENT_PREFIX = "#";
	private static final Pattern BANDWIDTH_PATTERN = Pattern.compile(COMMENT_PREFIX + "EXT-X-STREAM-INF:.*,BANDWIDTH=(?<BANDWIDTH>\\d+)(,.*)?");
	private static final Pattern HD_PATTERN = Pattern.compile("standard\\d(,standard\\d)*");
	private static final String HD_STANDARD = "standard5";

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

	public static Optional<URL> getHdStream(Episode ep) {
		String suffix = ep.getUrlSuffix();
		Matcher matcher = HD_PATTERN.matcher(suffix);
		if (matcher.find()) {
			try {
				return Optional.of(getBestStream(RemoteResource.fromSuffix(matcher.replaceFirst(HD_STANDARD))));
			} catch (IOException ignored) {
			}
		}
		return Optional.empty();
	}

	public static URL getFirstStream(RemoteResource playlist) throws IOException {
		for (String line : playlist.readLines()) {
			if (StringUtils.isNotBlank(line) && !PlaylistUtils.isComment(line)) {
				return new URL(line);
			}
		}
		throw new IllegalStateException("Cannot find stream");
	}

	public static URL getBestStream(RemoteResource playlist) throws MalformedURLException, IOException {
		int bestBandwith = 0;
		String bestUrl = null;
		List<String> lines = playlist.readLines();
		for (int i = 0; i < lines.size() - 1; i++) {
			String comment, url;
			if (PlaylistUtils.isComment(comment = lines.get(i)) && !PlaylistUtils.isComment(url = lines.get(i + 1))) {
				Optional<Integer> bandwidth = PlaylistUtils.getBandwidth(comment);
				if (bandwidth.isPresent()) {
					if (bandwidth.get() > bestBandwith) {
						bestUrl = url;
						bestBandwith = bandwidth.get();
					}
				} else {
					bestUrl = url;
				}
			}
		}
		if (StringUtils.isBlank(bestUrl)) {
			throw new IllegalStateException("Cannot find stream");
		}
		return new URL(bestUrl);
	}
}

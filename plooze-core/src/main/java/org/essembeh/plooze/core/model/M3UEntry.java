package org.essembeh.plooze.core.model;

import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class M3UEntry {

	public static final String COMMENT_PREFIX = "#EXT-X-STREAM-INF:";

	public static M3UEntry parse(String url, String comments) {
		int bandwidth = 0;
		Point resolution = new Point();
		String codecs = "";

		String[] properties = StringUtils.split(comments.substring(COMMENT_PREFIX.length()), ",");
		for (String property : properties) {
			int eq = property.indexOf("=");
			if (eq > 0) {
				String key = property.substring(0, eq);
				String value = property.substring(eq + 1);
				if ("BANDWIDTH".equals(key)) {
					bandwidth = Integer.parseInt(property.substring(eq + 1));
				} else if ("BANDWIDTH".equals(key)) {
					codecs = value;
				} else if ("RESOLUTION".equals(key)) {
					int x = value.indexOf("x");
					if (x > 0) {
						resolution = new Point(Integer.parseInt(value.substring(0, x)), Integer.parseInt(value.substring(x + 1)));
					}
				}
			}
		}
		return new M3UEntry(url, bandwidth, resolution, codecs);
	}

	private final String url;
	private final int bandwidth;
	private final Point resolution;
	private final String codecs;

	public M3UEntry(String url, int bandwidth, Point resolution, String codecs) {
		super();
		this.url = url;
		this.bandwidth = bandwidth;
		this.resolution = resolution;
		this.codecs = codecs;
	}

	public String getUrl() {
		return url;
	}

	public int getBandwidth() {
		return bandwidth;
	}

	public Point getResolution() {
		return resolution;
	}

	public String getCodecs() {
		return codecs;
	}

	public MultiPartPlaylist getMultiPartPlaylist() throws IOException {
		URL netUrl = new URL(url);
		try (InputStream is = netUrl.openStream()) {
			return MultiPartPlaylist.parse(IOUtils.readLines(is));
		}

	}
}
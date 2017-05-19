package org.essembeh.plooze.core.model;

import java.util.Comparator;

public class StreamUrl implements Comparable<StreamUrl> {

	private final String url;
	private final int bandwidth;
	private final String resolution;

	public StreamUrl(String url, int bandwidth, String resolution) {
		super();
		this.url = url;
		this.bandwidth = bandwidth;
		this.resolution = resolution;
	}

	public int getBandwidth() {
		return bandwidth;
	}

	public String getResolution() {
		return resolution;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public int compareTo(StreamUrl o) {
		return Comparator.comparingInt(StreamUrl::getBandwidth).compare(this, o);
	}
}

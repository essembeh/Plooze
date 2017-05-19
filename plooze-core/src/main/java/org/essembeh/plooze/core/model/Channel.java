package org.essembeh.plooze.core.model;

import org.essembeh.plooze.core.utils.PloozeConstants;

public enum Channel {
	FRANCE2("france2"),
	FRANCE3("france3"),
	FRANCE4("france4"),
	FRANCE5("france5"),
	FRANCEO("franceo");

	final String url;

	private Channel(String s) {
		this.url = PloozeConstants.CONTENT_URL_PREFIX + s;
	}

	public String getContentUrl() {
		return url;
	}
}

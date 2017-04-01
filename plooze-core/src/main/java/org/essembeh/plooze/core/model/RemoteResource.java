package org.essembeh.plooze.core.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.essembeh.plooze.core.utils.PloozeConstants;

public class RemoteResource {

	public static boolean isValid(URL url) {
		try (InputStream is = url.openStream()) {
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static RemoteResource fromSuffix(String prefix) throws MalformedURLException, IOException {
		return new RemoteResource(PloozeConstants.URL_PREFIX + prefix);
	}

	protected final URL url;

	public RemoteResource(String url) throws IOException {
		this.url = new URL(url);
		if (!isValid(this.url)) {
			throw new IOException("Invalid URL: " + url);
		}
	}

	public URL getUrl() {
		return url;
	}

	public List<String> readLines() throws IOException {
		try (InputStream is = url.openStream()) {
			return IOUtils.readLines(is);
		}
	}

}

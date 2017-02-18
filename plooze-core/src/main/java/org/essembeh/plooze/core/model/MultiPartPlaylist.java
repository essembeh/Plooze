package org.essembeh.plooze.core.model;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.lang3.StringUtils;
import org.essembeh.plooze.core.utils.IDownloadCallback;

public class MultiPartPlaylist {

	public static MultiPartPlaylist parse(List<String> lines) {
		MultiPartPlaylist out = new MultiPartPlaylist();
		for (String line : lines) {
			if (StringUtils.isNotBlank(line) && !line.startsWith("#")) {
				out.urls.add(line);
			}
		}
		return out;
	}

	private final List<String> urls = new ArrayList<>();

	public List<String> getUrls() {
		return urls;
	}

	public void download(Path output, IDownloadCallback callback) throws IOException {
		int index = 0;
		try (OutputStream os = Files.newOutputStream(output); BufferedOutputStream bos = new BufferedOutputStream(os)) {
			for (String url : urls) {
				URL netUrl = new URL(url);
				try (InputStream is = netUrl.openStream(); BoundedInputStream bis = new BoundedInputStream(is)) {
					callback.partStart(index, urls.size(), url);
					long start = System.currentTimeMillis();
					int count = IOUtils.copy(bis, bos);
					long duration = System.currentTimeMillis() - start;
					long bytesPerMilliSeconf = count / duration;
					callback.partDone(index++, urls.size(), bytesPerMilliSeconf * 1000 / 1024);
				}
			}
		}
	}

}

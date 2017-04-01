package org.essembeh.plooze.core.model;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.essembeh.plooze.core.utils.IDownloadCallback;
import org.essembeh.plooze.core.utils.NetSpeedHelper;
import org.essembeh.plooze.core.utils.PloozeUtils;

public class MultiPartPlaylist extends RemoteResource {

	public MultiPartPlaylist(String url) throws IOException {
		super(url);
	}

	public void download(OutputStream out, IDownloadCallback callback) throws IOException {
		try (InputStream is = getUrl().openStream()) {
			List<String> subUrls = IOUtils.readLines(is).stream().filter(PloozeUtils::isHttpUrl).collect(Collectors.toList());
			NetSpeedHelper netSpeedHelper = new NetSpeedHelper();
			for (int index = 0; index < subUrls.size(); index++) {
				URL partUrl = new URL(subUrls.get(index));
				try (InputStream partInputStream = partUrl.openStream();
						BufferedInputStream partBufferedInputStream = new BufferedInputStream(partInputStream)) {
					callback.partStart(index, subUrls.size());
					long bytesPerMilliSeconf = netSpeedHelper.getBytesPerMilliSecond(IOUtils.copy(partBufferedInputStream, out));
					callback.partDone(index + 1, subUrls.size(), bytesPerMilliSeconf * 1000 / 1024);
				}
			}
		}
	}

}

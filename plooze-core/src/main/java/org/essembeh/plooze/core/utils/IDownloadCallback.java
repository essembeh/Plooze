package org.essembeh.plooze.core.utils;

public interface IDownloadCallback {
	void partStart(int index, int total, String url);

	void partDone(int index, int total, long koSec);

	void done();
}

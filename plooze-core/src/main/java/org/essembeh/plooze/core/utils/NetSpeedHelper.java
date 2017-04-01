package org.essembeh.plooze.core.utils;

public class NetSpeedHelper {

	private final long start = System.currentTimeMillis();
	private long count = 0;

	public long getBytesPerMilliSecond(long bytesConsumed) {
		return (count += bytesConsumed) / (System.currentTimeMillis() - start);
	}
}

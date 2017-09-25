package org.essembeh.plooze.core.utils;

import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrLookup;

import com.google.gson.JsonObject;

public class JsonStrLookup extends StrLookup<String> {

	private static final String NO_VALUE = null;

	private final JsonObject content;

	public JsonStrLookup(JsonObject object) {
		this.content = object;
	}

	@Override
	public String lookup(String key) {
		if (content.has(key)) {
			return PloozeUtils.sanitize(content.get(key).getAsString());
		}
		if (key.contains(PloozeConstants.OR)) {
			return Stream.of(StringUtils.splitByWholeSeparator(key, PloozeConstants.OR)).map(this::lookup).filter(StringUtils::isNotEmpty).findFirst().orElse(NO_VALUE);
		}
		return NO_VALUE;
	}

}

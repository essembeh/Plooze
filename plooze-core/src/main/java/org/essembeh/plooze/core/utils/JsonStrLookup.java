package org.essembeh.plooze.core.utils;

import org.apache.commons.lang3.text.StrLookup;

import com.google.gson.JsonObject;

public class JsonStrLookup extends StrLookup<String> {

	private final JsonObject content;

	public JsonStrLookup(JsonObject object) {
		this.content = object;
	}

	@Override
	public String lookup(String key) {
		String out = content.get(key).getAsString();
		System.err.println(key + " = " + out);
		return out;

	}

}

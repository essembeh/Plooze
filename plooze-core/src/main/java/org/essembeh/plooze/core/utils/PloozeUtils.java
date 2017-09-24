package org.essembeh.plooze.core.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.essembeh.plooze.core.model.Episode;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class PloozeUtils {

	private static final JsonParser JSON_PARSER = new JsonParser();

	public static JsonElement getJson(String url) throws IOException {
		URL netUrl = new URL(url);
		try (InputStreamReader r = new InputStreamReader(netUrl.openStream())) {
			return JSON_PARSER.parse(r);
		}
	}

	public static List<Episode> parseEpisodes(String url) throws IOException {
		JsonElement root = getJson(url);
		JsonArray programmes = root.getAsJsonObject().get("reponse").getAsJsonObject().get("emissions").getAsJsonArray();
		List<Episode> out = new ArrayList<>();
		for (JsonElement programme : programmes) {
			out.add(new Episode(programme.getAsJsonObject()));
		}
		return out;
	}

	public static String resolve(String format, Episode in) {
		StrSubstitutor substitutor = new StrSubstitutor(new JsonStrLookup(in.getJson()), "${", "}", '\\');
		return substitutor.replace(format);
	}

	public static boolean isHttpUrl(String in) {
		try {
			URL url = new URL(in);
			return url.getProtocol().toLowerCase().startsWith("http");
		} catch (MalformedURLException ignored) {
		}
		return false;
	}
	
	public static String sanitize(String in) {
		return in.replace('/', '_');
	}
}

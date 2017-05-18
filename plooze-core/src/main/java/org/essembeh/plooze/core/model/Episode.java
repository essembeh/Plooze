package org.essembeh.plooze.core.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.essembeh.plooze.core.utils.PlaylistUtils;
import org.essembeh.plooze.core.utils.PloozeConstants;
import org.essembeh.plooze.core.utils.PloozeUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Episode {

	private final JsonObject json;

	public Episode(JsonObject json) {
		super();
		this.json = json;
	}

	public int getId() {
		return Integer.parseInt(getProperty("id_diffusion"));
	}

	public String getTitle() {
		return getProperty("titre");
	}

	public String getTitle2() {
		return getProperty("soustitre");
	}

	public String getDescription() {
		return getProperty("accroche");
	}

	public int getDuration() {
		return Integer.parseInt(getProperty("duree"));
	}

	@Override
	public String toString() {
		return json.toString();
	}

	public String getProperty(String key) {
		return json.has(key) ? json.get(key).getAsString() : "";
	}

	public JsonObject getJson() {
		return json;
	}

	public String getGenre() {
		return getProperty("format") + " / " + getProperty("genre_simplifie");
	}

	public String getChannel() {
		return getProperty("chaine_label");
	}

	public String getPlaylistUrl() throws IOException {
		JsonElement details = PloozeUtils.getJson(String.format(PloozeConstants.DETAILS_URL__id, getId()));
		Map<String, String> urls = new HashMap<>();
		for (JsonElement video : details.getAsJsonObject().get("videos").getAsJsonArray()) {
			String format = video.getAsJsonObject().get("format").getAsString();
			String url = video.getAsJsonObject().get("url").getAsString();
			urls.put(format, url);
		}
		if (urls.containsKey("hls_v5_os")) {
			return urls.get("hls_v5_os");
		}
		if (urls.containsKey("m3u8-download")) {
			return urls.get("m3u8-download");
		}
		throw new IllegalStateException("Cannot find stream url for " + getId());
	}

	public String getStreamUrl() throws IOException {
		return PlaylistUtils.getBestStream(new RemoteResource(getPlaylistUrl()));
	}

}

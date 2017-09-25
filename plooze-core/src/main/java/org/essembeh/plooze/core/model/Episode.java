package org.essembeh.plooze.core.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.essembeh.plooze.core.utils.PlaylistUtils;
import org.essembeh.plooze.core.utils.PloozeConstants;
import org.essembeh.plooze.core.utils.PloozeUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Episode {

	public enum Quality {
		BEST, MEDIUM, LOWEST
	}

	private final JsonObject json;

	public Episode(JsonObject json) {
		super();
		this.json = json;
	}

	public int getId() {
		return Integer.parseInt(getProperty(PloozeConstants.JsonFields.ID_DIFFUSION));
	}

	public String getTitle() {
		return getProperty(PloozeConstants.JsonFields.TITRE);
	}

	public String getTitle2() {
		return getProperty(PloozeConstants.JsonFields.SOUSTITRE);
	}

	public String getDescription() {
		return getProperty(PloozeConstants.JsonFields.ACCROCHE);
	}

	public int getDuration() {
		return Integer.parseInt(getProperty(PloozeConstants.JsonFields.DUREE));
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
		return getProperty(PloozeConstants.JsonFields.FORMAT) + " / " + getProperty(PloozeConstants.JsonFields.GENRE_SIMPLIFIE);
	}

	public String getChannel() {
		return getProperty(PloozeConstants.JsonFields.CHAINE_LABEL);
	}

	public String getPlaylistUrl() throws IOException {
		JsonElement details = PloozeUtils.getJson(String.format(PloozeConstants.DETAILS_URL__id, getId()));
		Map<String, String> urls = new HashMap<>();
		for (JsonElement video : details.getAsJsonObject().get(PloozeConstants.JsonFields.VIDEOS).getAsJsonArray()) {
			String format = video.getAsJsonObject().get(PloozeConstants.JsonFields.FORMAT).getAsString();
			String url = video.getAsJsonObject().get(PloozeConstants.JsonFields.URL).getAsString();
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

	public Optional<StreamUrl> getStreamUrl(Quality quality) throws IOException {
		return PlaylistUtils.getStream(new RemoteResource(getPlaylistUrl()), quality);
	}

}

package org.essembeh.plooze.core.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
		return getProperty("sous_titre");
	}

	public String getCode() {
		return getProperty("code_programme");
	}

	public String getDescription() {
		return getProperty("accroche");
	}

	public int getDuration() {
		return Integer.parseInt(getProperty("duree"));
	}

	public String getUrlSuffix() {
		return getProperty("url_video");
	}

	public Date getDate() throws ParseException {
		return new SimpleDateFormat("YYYY-MM-dd").parse(getProperty("date"));
	}

	@Override
	public String toString() {
		return json.toString();
	}

	public String getProperty(String key) {
		return json.get(key).getAsString();
	}

	public JsonObject getJson() {
		return json;
	}

	public String getGenre() {
		return getProperty("format") + " / " + getProperty("genre_simplifie");
	}

	public String getChannel() {
		return getProperty("chaine");
	}

	public RemoteResource getMasterPlaylist() throws MalformedURLException, IOException {
		return RemoteResource.fromSuffix(getUrlSuffix());
	}

}

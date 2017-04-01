package org.essembeh.plooze.core.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.essembeh.plooze.core.model.Episode;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class PloozeUtils {

	public static void downloadZipFile(Path output) throws IOException {
		URL url = new URL(PloozeConstants.ZIP_URL);
		try (OutputStream os = Files.newOutputStream(output);
				BufferedOutputStream bos = new BufferedOutputStream(os);
				InputStream is = url.openStream();
				BufferedInputStream bis = new BufferedInputStream(is)) {
			IOUtils.copy(bis, bos);
		}
	}

	public static List<Episode> readZip(ZipFile zipFile) throws IOException {
		List<Episode> out = new ArrayList<>();
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry zipEntry = entries.nextElement();
			if (zipEntry.getName().startsWith("catch_up_")) {
				List<Episode> episodes = PloozeUtils.parseEpisodes(zipFile.getInputStream(zipEntry));
				out.addAll(episodes);
			}
		}
		return out;
	}

	public static List<Episode> parseEpisodes(InputStream inputStream) throws IOException {
		try (InputStreamReader r = new InputStreamReader(inputStream)) {
			JsonElement root = new JsonParser().parse(r);
			JsonArray programmes = root.getAsJsonObject().get("programmes").getAsJsonArray();
			List<Episode> out = new ArrayList<>();
			for (JsonElement programme : programmes) {
				out.add(new Episode(programme.getAsJsonObject()));
			}
			return out;
		}
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
}

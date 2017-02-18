package org.essembeh.plooze.webapp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.essembeh.plooze.core.model.Episode;
import org.essembeh.plooze.core.model.PloozeDatabase;
import org.essembeh.plooze.core.utils.IDownloadCallback;
import org.essembeh.plooze.core.utils.PloozeUtils;

import com.google.gson.GsonBuilder;

import spark.Spark;

public class Launcher {

	public static void main(String[] args) throws IOException {
		if (System.getenv("PORT") != null) {
			Spark.port(Integer.parseInt(System.getenv("PORT")));
		}
		Path zipFile = Files.createTempFile("plooze", ".zip");
		PloozeDatabase database = new PloozeDatabase();
		PloozeUtils.downloadZipFile(zipFile);
		database.refresh(zipFile);
		Spark.get("/update", (i, o) -> {
			PloozeUtils.downloadZipFile(zipFile);
			database.refresh(zipFile);
			return database.getEpisodes().size();
		});
		Spark.get("/search/:motif", (i, o) -> {
			List<Episode> episodes = database.search(i.params(":motif"));
			return new GsonBuilder().setPrettyPrinting().create().toJson(episodes.stream().map(Episode::getJson).toArray());
		});
		Spark.get("/view/:id", (i, o) -> {
			Optional<Episode> ep = database.findById(i.params(":id"));
			if (!ep.isPresent()) {
				Spark.halt(404);
			} else {
				o.type("application/x-mpegURL");
				o.redirect(ep.get().getPlaylist().getHighestBandwidth().get().getUrl());
			}
			return null;
		});
		Spark.get("/get/:id", (i, o) -> {
			Optional<Episode> ep = database.findById(i.params(":id"));
			if (!ep.isPresent()) {
				Spark.halt(404);
			} else {
				o.type("video/MP2T");
				ep.get().getPlaylist().getHighestBandwidth().get().getMultiPartPlaylist().download(o.raw().getOutputStream(), new IDownloadCallback() {
					@Override
					public void partStart(int index, int total, String url) {
					}

					@Override
					public void partDone(int index, int total, long koSec) {
					}

					@Override
					public void done() {
					}
				});
			}
			return null;
		});
	}
}

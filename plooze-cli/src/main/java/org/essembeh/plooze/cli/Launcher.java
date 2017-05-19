package org.essembeh.plooze.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.essembeh.plooze.core.model.Episode;
import org.essembeh.plooze.core.model.PloozeDatabase;
import org.essembeh.plooze.core.model.StreamUrl;
import org.essembeh.plooze.core.utils.FfmpegLauncher;
import org.essembeh.plooze.core.utils.PloozeConstants;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Launcher {

	private static final Gson JSON_PP = new GsonBuilder().setPrettyPrinting().create();

	public static void main(String[] args) throws ParseException, IOException, InterruptedException {
		AppOptions options = AppOptions.parse(args);
		if (options.canProcess()) {
			PloozeDatabase database = new PloozeDatabase();
			if (options.getCronDelay().isPresent()) {
				Timer timer = new Timer("Plooze cron", false);
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						System.out.println("[DAEMON] " + SimpleDateFormat.getTimeInstance().format(new Date()) + ": Start processing");
						try {
							process(database, options);
						} catch (IOException | InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println("[DAEMON] " + SimpleDateFormat.getTimeInstance().format(new Date()) + ": Next download in "
								+ options.getCronDelay().get() + " hour(s)");
					}
				}, 0, options.getCronDelay().get() * 3600000);
			} else {
				process(database, options);
			}
		} else {
			options.displayHelp();
		}
	}

	private static void process(PloozeDatabase database, AppOptions options) throws IOException, InterruptedException {
		database.refresh(PloozeConstants.CONTENT_URLS);
		if (options.listFields()) {
			String[] fields = database.getFields();
			if (fields == null) {
				System.out.println("Cannot retrieve fields list, you have to refresh the database first");
			} else {
				System.out.println("Available fields:");
				Stream.of(fields).sorted().map(s -> "  " + s).forEach(System.out::println);
			}
		}
		if (options.isVerbose()) {
			System.out.println("Database loaded with " + database.getEpisodes().size() + " episodes");
		}
		for (String arg : options.getArgs()) {
			if (options.isVerbose()) {
				System.out.println("Search: '" + arg + "'");
			}
			List<Episode> result = database.search(arg, options.getFields().orElse(PloozeConstants.DEFAULT_FIELDS));
			Collections.sort(result, Comparator.comparingInt(Episode::getId).reversed());
			for (Episode episode : result) {
				if (options.getDownloadFolder().isPresent()) {
					// DOWNLOAD MODE
					String filename = StringUtils.defaultIfBlank(episode.getTitle2(), "" + episode.getId());
					StreamUrl streamUrl = episode.getStreamUrl(options.getQuality());
					Path output = Paths.get(options.getDownloadFolder().get().toString(), episode.getTitle(), filename + PloozeConstants.EXTENSION);
					if (!Files.exists(output) || options.shouldOverwrite()) {
						if (!Files.isDirectory(output.getParent())) {
							Files.createDirectories(output.getParent());
						}
						System.out.println("Start downloading: " + output.toString() + ", resolution: " + streamUrl.getResolution());
						FfmpegLauncher.DEFAULT.download(streamUrl.getUrl(), output, new FfmpegLauncher.Callback() {
							@Override
							public void progress(String line) {
								System.out.print("  " + line + "\r");
							}

							@Override
							public void done(int rc) {
								System.out.println("");
								if (rc != 0) {
									System.out.println("  exit value: " + rc);
								}
							}
						});
					} else {
						if (options.isVerbose()) {
							System.out.println("File already exists: " + output);
						}
					}
				} else {
					// DISPLAY MODE
					if (options.dumpJson()) {
						System.out.println(JSON_PP.toJson(episode.getJson()));
					} else if (options.isVerbose()) {
						System.out.println("Title     : " + episode.getTitle() + " / " + episode.getTitle2());
						System.out.println("Genre     : " + episode.getGenre());
						System.out.println("Duration  : " + episode.getDuration() + " min");
						System.out.println("Channel   : " + episode.getChannel());
						System.out.println("ID        : " + episode.getId());
						if (StringUtils.isNotBlank(episode.getDescription())) {
							System.out.println("Desciption: " + episode.getDescription());
						}
						System.out.println("");
					} else {
						System.out.println(
								String.format("[%s] %s: %s (%d min)", episode.getId(), episode.getTitle(), episode.getTitle2(), episode.getDuration()));
					}
				}
			}
		}
	}
}

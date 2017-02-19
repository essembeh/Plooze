package org.essembeh.plooze.cli;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.essembeh.plooze.core.model.Episode;
import org.essembeh.plooze.core.model.PloozeDatabase;
import org.essembeh.plooze.core.utils.IDownloadCallback;
import org.essembeh.plooze.core.utils.PloozeConstants;
import org.essembeh.plooze.core.utils.PloozeUtils;

public class Launcher {

	private static final Path ZIP_FILE = Paths.get(System.getProperty("user.home"), ".cache", "plooze.zip");

	public static void main(String[] args) throws ParseException, IOException {
		AppOptions options = AppOptions.parse(args);
		if (options.canProcess()) {
			PloozeDatabase database = new PloozeDatabase();
			if (options.getCronDelay().isPresent()) {
				Timer timer = new Timer("Plooze cron", false);
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						try {
							process(database, options);
						} catch (IOException e) {
							e.printStackTrace();
						}
						System.out.println("[DAEMON] " +
								SimpleDateFormat.getTimeInstance().format(new Date()) + ": Next download in " + options.getCronDelay().get() + " hour(s)");
					}
				}, 0, options.getCronDelay().get() * 3600000);
			} else {
				process(database, options);
			}
		} else {
			options.displayHelp();
		}
	}

	private static void process(PloozeDatabase database, AppOptions options) throws IOException {
		if (options.updateZipfile()) {
			if (!Files.isDirectory(ZIP_FILE.getParent())) {
				Files.createDirectories(ZIP_FILE.getParent());
			}
			PloozeUtils.downloadZipFile(ZIP_FILE);
			System.out.println("Zip file updated: " + ZIP_FILE);
		}
		database.refresh(ZIP_FILE);
		System.out.println("Database loaded with " + database.getEpisodes().size() + " episodes");
		for (String arg : options.getArgs()) {
			if (options.isVerbose()) {
				System.out.println("Search: '" + arg + "'");
			}
			List<Episode> result = database.search(arg);
			for (Episode episode : result) {
				if (options.getDownloadFolder().isPresent()) {
					String filename = StringUtils.defaultIfBlank(episode.getTitle2(), episode.getId());
					Path output = Paths.get(options.getDownloadFolder().get().toString(), episode.getTitle(), filename + PloozeConstants.EXTENSION);
					if (!Files.exists(output) || options.shouldOverwrite()) {
						if (!Files.isDirectory(output.getParent())) {
							Files.createDirectories(output.getParent());
						}
						System.out.println("Start downloading: " + output.toString());
						try (OutputStream os = Files.newOutputStream(output)) {
							episode.getPlaylist().getHighestBandwidth().get().getMultiPartPlaylist().download(os, new IDownloadCallback() {
								@Override
								public void partStart(int index, int total, String url) {
								}

								@Override
								public void partDone(int index, int total, long koSec) {
									System.out.print(String.format("\r%d/%d (%d ko/sec)", index + 1, total, koSec));
								}

								@Override
								public void done() {
									System.out.println("");
								}
							});
						}
					} else {
						System.out.println("File already exists: " + output);
					}
				} else {
					display(episode, options.isVerbose());
				}
			}
		}
	}

	private static void display(Episode episode, boolean verbose) {
		if (verbose) {
			System.out.println("Title     : " + episode.getTitle() + " / " + episode.getTitle2());
			System.out.println("Genre     : " + episode.getGenre());
			System.out.println("Duration  : " + episode.getDuration() + " min");
			System.out.println("Desciption: " + episode.getDescription());
			System.out.println("");
		} else {
			System.out.println(String.format("[%s] %s: %s (%d min)", episode.getId(), episode.getTitle(), episode.getTitle2(), episode.getDuration()));
		}
	}
}

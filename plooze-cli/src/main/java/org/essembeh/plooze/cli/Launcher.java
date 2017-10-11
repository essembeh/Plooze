package org.essembeh.plooze.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.essembeh.plooze.core.model.Channel;
import org.essembeh.plooze.core.model.Episode;
import org.essembeh.plooze.core.model.PloozeDatabase;
import org.essembeh.plooze.core.model.StreamUrl;
import org.essembeh.plooze.core.utils.EpisodeFilter;
import org.essembeh.plooze.core.utils.FfmpegLauncher;
import org.essembeh.plooze.core.utils.PloozeConstants;
import org.essembeh.plooze.core.utils.PloozeUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Launcher {

	private static final Gson JSON_PP = new GsonBuilder().setPrettyPrinting().create();
	private static final int VALUE_MAX_LEN = 32;

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
						} catch (Exception e) {
							e.printStackTrace();
						}
						System.out.println("[DAEMON] " + SimpleDateFormat.getTimeInstance().format(new Date()) + ": Next download in " + options.getCronDelay().get() + " hour(s)");
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
		Channel[] channels = options.getChannels();
		System.out.println("Refresh content for: " + StringUtils.join(channels, ", ").toLowerCase());
		database.refresh(channels);
		if (options.isVerbose()) {
			System.out.println("Database loaded with " + database.getEpisodes().size() + " episodes");
		}
		if (options.listFields()) {
			listFieldKeys(database);
		}
		if (options.listFieldValues()) {
			// List Field values mode
			listFieldValues(database, options.getFields().orElse(PloozeConstants.DEFAULT_FIELDS));
		} else {
			EpisodeFilter filter = new EpisodeFilter(options.getFields().orElse(PloozeConstants.DEFAULT_FIELDS));
			options.getDurationMax().ifPresent(filter::setDurationMax);
			options.getDurationMin().ifPresent(filter::setDurationMin);
			for (String keyword : options.getKeywords()) {
				if (options.isVerbose()) {
					System.out.println("Search: '" + keyword + "'");
				}
				processFilter(database, filter.setMotif(keyword), options);
			}
		}
	}

	private static void listFieldKeys(PloozeDatabase database) {
		System.out.println("Available fields:");
		System.out.println(StringUtils.join(database.getFields(), ", "));
	}

	private static void processFilter(PloozeDatabase database, EpisodeFilter filter, AppOptions options) throws IOException, InterruptedException {
		List<Episode> result = database.stream().filter(filter::test).sorted(Comparator.comparingInt(Episode::getId).reversed()).collect(Collectors.toList());
		for (Episode episode : result) {
			if (options.getDownloadFolder().isPresent()) {
				downloadEpisode(episode, options);
			} else {
				displayEpisode(episode, options);
			}
		}
	}

	private static void displayEpisode(Episode episode, AppOptions options) {
		// DISPLAY MODE
		if (options.dumpJson()) {
			System.out.println(JSON_PP.toJson(episode.getJson()));
		} else if (options.isVerbose()) {
			System.out.println("Title       : " + episode.getTitle() + " / " + episode.getTitle2());
			System.out.println("Genre       : " + episode.getGenre());
			System.out.println("Duration    : " + episode.getDuration() + " min");
			System.out.println("Channel     : " + episode.getChannel());
			System.out.println("ID          : " + episode.getId());
			if (StringUtils.isNotBlank(episode.getDescription())) {
				System.out.println("Description : " + episode.getDescription());
			}
			System.out.println("");
		} else {
			System.out.println(String.format("[%s] %s: %s (%d min)", episode.getId(), episode.getTitle(), episode.getTitle2(), episode.getDuration()));
		}
	}

	private static void downloadEpisode(Episode episode, AppOptions options) throws IOException, InterruptedException {
		// Download Mode
		String filename = PloozeUtils.resolve(options.getOutputPathFormat(), episode);
		Path output = Paths.get(options.getDownloadFolder().get().toString(), filename);
		if (!Files.exists(output) || options.shouldOverwrite()) {
			if (!Files.isDirectory(output.getParent())) {
				Files.createDirectories(output.getParent());
			}
			Optional<StreamUrl> streamUrl = episode.getStreamUrl(options.getQuality());
			if (streamUrl.isPresent()) {
				System.out.println("Start downloading: " + output.toString() + ", resolution: " + streamUrl.get().getResolution());
				FfmpegLauncher.DEFAULT.download(streamUrl.get().getUrl(), output, new FfmpegLauncher.Callback() {
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
				System.out.println("Cannot find stream for " + episode.getId());
			}
		} else {
			if (options.isVerbose()) {
				System.out.println("File already exists: " + output);
			}
		}
	}

	private static void listFieldValues(PloozeDatabase database, String[] fields) {
		for (String field : fields) {
			System.out.println("Values for field: " + field);
			database.stream().map(e -> e.getProperty(field)).map(StringUtils::trim).filter(StringUtils::isNotBlank).map(s -> StringUtils.abbreviate(s, VALUE_MAX_LEN)).sorted().distinct()
					.map(s -> "  * " + s).forEach(System.out::println);
			System.out.println();
		}
	}
}

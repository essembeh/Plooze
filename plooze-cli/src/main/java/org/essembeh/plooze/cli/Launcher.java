package org.essembeh.plooze.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
			if (options.updateZipfile()) {
				if (!Files.isDirectory(ZIP_FILE.getParent())) {
					Files.createDirectories(ZIP_FILE.getParent());
				}
				System.out.println("Update zip file: " + ZIP_FILE);
				PloozeUtils.downloadZipFile(ZIP_FILE);
			}
			PloozeDatabase database = PloozeDatabase.read(ZIP_FILE);
			System.out.println("Found " + database.getEpisodes().size() + " episodes");

			for (String arg : options.getArgs()) {
				System.out.println("Search: " + arg);
				System.out.println();
				List<Episode> result = database.search(arg);
				for (Episode episode : result) {
					if (options.getDownloadFolder().isPresent()) {
						String filename = StringUtils.defaultIfBlank(episode.getTitle2(), episode.getId());
						Path output = Paths.get(options.getDownloadFolder().get().toString(), episode.getTitle(), filename + PloozeConstants.EXTENSION);
						if (!Files.isDirectory(output.getParent())) {
							Files.createDirectories(output.getParent());
						}
						System.out.println("Start downloading: " + output.toString());
						episode.getPlaylist().getHighestBandwidth().get().getMultiPartPlaylist().download(output, new IDownloadCallback() {
							@Override
							public void partStart(int index, int total, String url) {
							}

							@Override
							public void partDone(int index, int total, long koSec) {
								System.out.print(String.format("%d/%d (%d ko/sec)\r", index + 1, total, koSec));
							}

							@Override
							public void done() {
								System.out.println("");
							}
						});
					} else {
						display(episode, options.displayDescription());
					}
				}
				System.out.println("");
			}

		} else {
			options.displayHelp();
		}
	}

	private static void display(Episode episode, boolean desc) {
		System.out.println("Title:      " + episode.getTitle() + " / " + episode.getTitle2());
		System.out.println("Genre:      " + episode.getGenre());
		System.out.println("Duration:   " + episode.getDuration() + " min");
		if (desc) {
			System.out.println("Desciption: " + episode.getDescription());
		}
		System.out.println();
	}
}

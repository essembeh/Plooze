package org.essembeh.plooze.core;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.essembeh.plooze.core.model.Episode;
import org.essembeh.plooze.core.model.PloozeDatabase;
import org.essembeh.plooze.core.utils.FfmpegLauncher;
import org.essembeh.plooze.core.utils.PlaylistUtils;
import org.essembeh.plooze.core.utils.PloozeConstants;
import org.essembeh.plooze.core.utils.PloozeUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class PloozeTest {

	private static Path contentZip = null;

	@BeforeClass
	public static void init() throws IOException {
		contentZip = Files.createTempFile("plooze", ".zip");
		PloozeUtils.downloadZipFile(contentZip);
		Assert.assertNotEquals(0, Files.getFileAttributeView(contentZip, BasicFileAttributeView.class).readAttributes().size());
	}

	@Test
	public void testDownload() throws Exception {
		PloozeDatabase ploozeDatabase = new PloozeDatabase();
		ploozeDatabase.refresh(contentZip);
		Assert.assertFalse(ploozeDatabase.getEpisodes().isEmpty());
		Assert.assertTrue(ploozeDatabase.getFields().length > PloozeConstants.DEFAULT_FIELDS.length);
		for (String f : PloozeConstants.DEFAULT_FIELDS) {
			Assert.assertTrue(Arrays.asList(ploozeDatabase.getFields()).contains(f));
		}
		Optional<Episode> selection = ploozeDatabase.getEpisodes().stream().collect(Collectors.minBy(Comparator.comparingInt(Episode::getDuration)));
		Assert.assertTrue(selection.isPresent());
		URL stream = PlaylistUtils.getFirstStream(selection.get().getMasterPlaylist());
		String filename = PloozeUtils.resolve(PloozeConstants.DEFAULT_FILENAME_FORMAT, selection.get());
		Path output = Files.createTempFile("plooze", filename);
		FfmpegLauncher.DEFAULT.download(stream, output, new FfmpegLauncher.Callback() {
			@Override
			public void start(Path output) {
				System.out.println("  > Start downloading: " + output.toString());
			}

			@Override
			public void progress(String line) {
				System.out.println("  > " + line);
			}

			@Override
			public void done(int rc) {
				System.out.println("   > exit value: " + rc);
			}
		});
		Assert.assertNotEquals(0, Files.getFileAttributeView(output, BasicFileAttributeView.class).readAttributes().size());
	}

	@Test
	public void testHD() throws Exception {
		PloozeDatabase ploozeDatabase = new PloozeDatabase();
		ploozeDatabase.refresh(contentZip);
		List<Episode> episodes = ploozeDatabase.getEpisodes();
		Collections.shuffle(episodes);
		System.out.println("Search any HD stream ...");
		Optional<Episode> ep = episodes.stream().filter(e -> {
			System.out.println(e.getTitle() + ": " + PloozeConstants.URL_PREFIX + e.getUrlSuffix());
			return PlaylistUtils.getHdStream(e).isPresent();
		}).findAny();
		Assert.assertTrue(ep.isPresent());
		System.out.println("Found " + ep.get().getTitle());
	}
}

package org.essembeh.plooze.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import org.essembeh.plooze.core.model.Channel;
import org.essembeh.plooze.core.model.Episode;
import org.essembeh.plooze.core.model.Episode.Quality;
import org.essembeh.plooze.core.model.PloozeDatabase;
import org.essembeh.plooze.core.model.StreamUrl;
import org.essembeh.plooze.core.utils.FfmpegLauncher;
import org.essembeh.plooze.core.utils.PloozeConstants;
import org.essembeh.plooze.core.utils.PloozeUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class PloozeTest {

	private static final PloozeDatabase PLOOZE_DATABASE = new PloozeDatabase();

	@BeforeClass
	public static void init() throws IOException {
		PLOOZE_DATABASE.refresh(Channel.FRANCE2);
		Assert.assertFalse(PLOOZE_DATABASE.getEpisodes().isEmpty());
	}

	@Test
	public void testQuality() throws Exception {
		Optional<Episode> selection = PLOOZE_DATABASE.getEpisodes().stream().collect(Collectors.minBy(Comparator.comparingInt(Episode::getDuration)));
		Assert.assertTrue(selection.isPresent());
		for (Quality quality : Quality.values()) {
			Assert.assertNotNull(selection.get().getStreamUrl(quality));
		}
	}

	@Test
	public void testDownload() throws Exception {
		Assert.assertTrue(PLOOZE_DATABASE.getFields().length > PloozeConstants.DEFAULT_FIELDS.length);
		for (String f : PloozeConstants.DEFAULT_FIELDS) {
			Assert.assertTrue(Arrays.asList(PLOOZE_DATABASE.getFields()).contains(f));
		}
		Optional<Episode> selection = PLOOZE_DATABASE.getEpisodes().stream().collect(Collectors.minBy(Comparator.comparingInt(Episode::getDuration)));
		Assert.assertTrue(selection.isPresent());
		StreamUrl stream = selection.get().getStreamUrl(Quality.LOWEST);
		String filename = PloozeUtils.resolve(PloozeConstants.DEFAULT_FILENAME_FORMAT, selection.get());
		Path output = Files.createTempFile("plooze", filename);
		FfmpegLauncher.DEFAULT.download(stream.getUrl(), output, new FfmpegLauncher.Callback() {
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
}

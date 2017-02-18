package org.essembeh.plooze.core;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import org.essembeh.plooze.core.model.Episode;
import org.essembeh.plooze.core.model.M3UEntry;
import org.essembeh.plooze.core.model.M3UPlaylist;
import org.essembeh.plooze.core.model.MultiPartPlaylist;
import org.essembeh.plooze.core.model.PloozeDatabase;
import org.essembeh.plooze.core.utils.IDownloadCallback;
import org.essembeh.plooze.core.utils.PloozeConstants;
import org.essembeh.plooze.core.utils.PloozeUtils;
import org.junit.Assert;
import org.junit.Test;

public class PloozeTest {
	@Test
	public void testPlooze() throws Exception {
		Path zipFile = Files.createTempFile("plooze", ".zip");
		PloozeUtils.downloadZipFile(zipFile);
		Assert.assertNotEquals(0, Files.getFileAttributeView(zipFile, BasicFileAttributeView.class).readAttributes().size());
		PloozeDatabase ploozeDatabase = new PloozeDatabase();
		ploozeDatabase.refresh(zipFile);
		Optional<Episode> selection = ploozeDatabase.getEpisodes().stream().collect(Collectors.minBy(Comparator.comparingInt(Episode::getDuration)));
		Assert.assertTrue(selection.isPresent());
		M3UPlaylist playlist = selection.get().getPlaylist();
		Assert.assertNotNull(playlist);
		Assert.assertFalse(playlist.getEntries().isEmpty());
		Optional<M3UEntry> entry = playlist.getLowestBandwidth();
		Assert.assertTrue(entry.isPresent());
		MultiPartPlaylist multiPartPlaylist = entry.get().getMultiPartPlaylist();
		Assert.assertFalse(multiPartPlaylist.getUrls().isEmpty());
		String filename = PloozeUtils.resolve(PloozeConstants.DEFAULT_FILENAME_FORMAT, selection.get());
		Path output = Files.createTempFile("plooze", filename);
		try (OutputStream out = Files.newOutputStream(output)) {
			multiPartPlaylist.download(out, new IDownloadCallback() {
				@Override
				public void partStart(int index, int total, String url) {
					System.out.println(String.format("%d/%d %s", index + 1, total, url));
				}

				@Override
				public void partDone(int index, int total, long koSec) {
					System.out.println(String.format("Finished %d ko/sec", koSec));
				}

				@Override
				public void done() {

				}
			});
		}
		Assert.assertNotEquals(0, Files.getFileAttributeView(output, BasicFileAttributeView.class).readAttributes().size());
	}
}

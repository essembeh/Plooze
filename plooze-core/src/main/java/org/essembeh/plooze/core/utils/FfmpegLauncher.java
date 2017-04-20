package org.essembeh.plooze.core.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FfmpegLauncher {

	public interface Callback {
		default void start(Path output) {
		}

		default void progress(String line) {
		}

		default void done(int rc) {
		}
	}

	public static final FfmpegLauncher DEFAULT = new FfmpegLauncher("ffmpeg");

	private final String binary;

	public FfmpegLauncher(String binary) {
		this.binary = binary;
	}

	public boolean test() throws InterruptedException {
		ProcessBuilder processBuilder = new ProcessBuilder(binary, "--version");
		try {
			Process process = processBuilder.start();
			int rc = process.waitFor();
			return rc == 0;
		} catch (IOException e) {
			return false;
		}
	}

	public boolean download(URL url, Path output, Callback callback) throws IOException, InterruptedException {
		Path tmpOutput = Paths.get(output.toString() + ".part");
		List<String> command = new ArrayList<>();
		command.add(binary);
		command.addAll(Arrays.asList("-i", url.toString()));
		command.addAll(Arrays.asList("-c", "copy"));
		command.addAll(Arrays.asList("-f", "mp4"));
		command.addAll(Arrays.asList("-v", "info"));
		command.addAll(Arrays.asList("-y"));
		command.add("file:" + tmpOutput.toString());
		callback.start(output);
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();

			Thread thread = new Thread("ffmpeg-reader") {
				@Override
				public void run() {
					try (InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
							BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
						String line;
						while ((line = bufferedReader.readLine()) != null) {
							if (line.startsWith("frame=")) {
								callback.progress(line);
							}
						}
					} catch (Exception e) {
					}
				}
			};
			thread.start();
			int rc = process.waitFor();
			if (rc == 0) {
				Files.move(tmpOutput, output, StandardCopyOption.REPLACE_EXISTING);
			}
			callback.done(rc);
			return rc == 0;
		} finally {
			Files.deleteIfExists(tmpOutput);
		}
	}
}

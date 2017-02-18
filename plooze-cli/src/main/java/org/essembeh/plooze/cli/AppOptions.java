package org.essembeh.plooze.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author seb
 *
 */
public class AppOptions {
	public static final String HELP = "h";
	public static final String UPDATE = "u";
	public static final String DOWNLOAD = "d";
	public static final String DESCRIPTION = "v";

	private static final Options OPTIONS = new Options();
	static {
		OPTIONS.addOption(HELP, "help", false, "Display help");
		OPTIONS.addOption(UPDATE, "update", false, "Download latest content");
		OPTIONS.addOption(DOWNLOAD, "download", true, "Download episodes to output folder");
		OPTIONS.addOption(DESCRIPTION, "description", false, "Display episode description");
	}

	public static AppOptions parse(String... args) throws ParseException {
		CommandLineParser parser = new DefaultParser();
		return new AppOptions(parser.parse(OPTIONS, args));
	}

	private final CommandLine commandLine;

	public AppOptions(CommandLine commandLine) {
		this.commandLine = commandLine;
	}

	protected Optional<String> getOptionValue(String option) {
		if (commandLine.hasOption(option)) {
			return Optional.of(commandLine.getOptionValue(option));
		}
		return Optional.empty();
	}

	public boolean canProcess() {
		return !commandLine.hasOption(HELP) && !commandLine.getArgList().isEmpty();
	}

	public void displayHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(100);
		formatter.printHelp("plooze", OPTIONS);
	}

	public List<String> getArgs() {
		return commandLine.getArgList();
	}

	public Optional<Path> getDownloadFolder() {
		return getOptionValue(DOWNLOAD).map(Paths::get);
	}

	public boolean updateZipfile() {
		return commandLine.hasOption(UPDATE);
	}

	public boolean displayDescription() {
		return commandLine.hasOption(DESCRIPTION);
	}
}

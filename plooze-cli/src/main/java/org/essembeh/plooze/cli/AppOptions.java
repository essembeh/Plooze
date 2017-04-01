package org.essembeh.plooze.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.essembeh.plooze.core.utils.PloozeConstants;

/**
 * @author seb
 *
 */
public class AppOptions {
	public static final String HELP = "h";
	public static final String UPDATE = "u";
	public static final String DOWNLOAD = "d";
	public static final String OVERWRITE = "o";
	public static final String VERBOSE = "v";
	public static final String JSON = "j";
	public static final String HD = "H";
	public static final String FIELDS = "f";
	public static final String LIST_FIELDS = "F";
	public static final String CRON = "c";

	private static final Options OPTIONS = new Options();
	static {
		OPTIONS.addOption(HELP, "help", false, "Display help");
		OPTIONS.addOption(UPDATE, "update", false, "Download latest content");
		OPTIONS.addOption(DOWNLOAD, "download", true, "Download episodes to output folder");
		OPTIONS.addOption(HD, "hd", false, "Download HD stream if it exists");
		OPTIONS.addOption(VERBOSE, "verbose", false, "Display more information");
		OPTIONS.addOption(JSON, "json", false, "Display json content while searching");
		OPTIONS.addOption(OVERWRITE, "overwrite", false, "Overwrite file if they already exist");
		OPTIONS.addOption(CRON, "cron", true, "Run every X hours");
		OPTIONS.addOption(FIELDS, "fields", true, "Match motif on given fields (defaults are \""
				+ StringUtils.join(Arrays.asList(PloozeConstants.DEFAULT_FIELDS), PloozeConstants.FIELDS_SEPARATOR) + "\")");
		OPTIONS.addOption(LIST_FIELDS, "list-fields", false, "Run every X hours");

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
		return !commandLine.hasOption(HELP);
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

	public boolean isVerbose() {
		return commandLine.hasOption(VERBOSE);
	}

	public boolean shouldOverwrite() {
		return commandLine.hasOption(OVERWRITE);
	}

	public Optional<Integer> getCronDelay() {
		return getOptionValue(CRON).map(Integer::parseInt);
	}

	public Optional<String[]> getFields() {
		return getOptionValue(FIELDS).map(s -> StringUtils.split(s, PloozeConstants.FIELDS_SEPARATOR));
	}

	public boolean listFields() {
		return commandLine.hasOption(LIST_FIELDS);
	}

	public boolean dumpJson() {
		return commandLine.hasOption(JSON);
	}

	public boolean downloadHd() {
		return commandLine.hasOption(HD);
	}
}

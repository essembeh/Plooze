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
import org.essembeh.plooze.core.model.Channel;
import org.essembeh.plooze.core.model.Episode.Quality;
import org.essembeh.plooze.core.utils.PloozeConstants;

/**
 * @author seb
 *
 */
public class AppOptions {
	public static final String CHANNEL = "c";
	public static final String DOWNLOAD = "d";
	public static final String FIELDS = "f";
	public static final String HELP = "h";
	public static final String JSON = "j";
	public static final String OVERWRITE = "o";
	public static final String QUALITY = "q";
	public static final String VERBOSE = "v";
	public static final String DURATION_MIN = "min";
	public static final String DURATION_MAX = "max";
	public static final String OUTPUT_FORMAT = "O";
	public static final String CRON = "C";
	public static final String LIST_FIELDS = "F";
	public static final String LIST_VALUES = "V";

	private static final Options OPTIONS = new Options();
	static {
		OPTIONS.addOption(HELP, "help", false, "Display help");
		OPTIONS.addOption(VERBOSE, "verbose", false, "Display more information");
		OPTIONS.addOption(CRON, "cron", true, "Run every X hours");
		OPTIONS.addOption(DOWNLOAD, "download", true, "Download episodes to output folder");
		OPTIONS.addOption(QUALITY, "quality", true, "Change the quality of downloaded files. Values: " + StringUtils.join(Quality.values(), ", ").toLowerCase());
		OPTIONS.addOption(CHANNEL, "channel", true, "Only search in a specific channel. Values: " + StringUtils.join(Channel.values(), ", ").toLowerCase());
		OPTIONS.addOption(JSON, "json", false, "Display json content while searching");
		OPTIONS.addOption(OVERWRITE, "overwrite", false, "Overwrite files if they already exist");
		OPTIONS.addOption(FIELDS, "fields", true,
				"Match motif on given fields (defaults are \"" + StringUtils.join(Arrays.asList(PloozeConstants.DEFAULT_FIELDS), PloozeConstants.FIELDS_SEPARATOR) + "\")");
		OPTIONS.addOption(LIST_FIELDS, "list-fields", false, "List all available fields, see -f");
		OPTIONS.addOption(LIST_VALUES, "list-values", false, "List all available values for fields given with -" + FIELDS);
		OPTIONS.addOption(DURATION_MIN, "duration-min", true, "Filter elements with duration > given arg (in minutes)");
		OPTIONS.addOption(DURATION_MAX, "duration-max", true, "Filter elements with duration < given arg (in minutes)");
		OPTIONS.addOption(OUTPUT_FORMAT, "output", true, "Output path format (default: " + PloozeConstants.DEFAULT_OUTPUT_FORMAT);

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

	public List<String> getKeywords() {
		return commandLine.getArgList();
	}

	public Optional<Path> getDownloadFolder() {
		return getOptionValue(DOWNLOAD).map(Paths::get);
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

	public Quality getQuality() {
		if (commandLine.hasOption(QUALITY)) {
			return Quality.valueOf(commandLine.getOptionValue(QUALITY).toUpperCase());
		}
		return Quality.BEST;
	}

	public Channel[] getChannels() {
		if (commandLine.hasOption(CHANNEL)) {
			return new Channel[] { Channel.valueOf(commandLine.getOptionValue(CHANNEL).toUpperCase()) };
		}
		return Channel.values();
	}

	public Optional<Integer> getDurationMin() {
		if (commandLine.hasOption(DURATION_MIN)) {
			return Optional.of(Integer.parseInt(commandLine.getOptionValue(DURATION_MIN)));
		}
		return Optional.empty();
	}

	public Optional<Integer> getDurationMax() {
		if (commandLine.hasOption(DURATION_MAX)) {
			return Optional.of(Integer.parseInt(commandLine.getOptionValue(DURATION_MAX)));
		}
		return Optional.empty();
	}

	public String getOutputPathFormat() {
		if (commandLine.hasOption(OUTPUT_FORMAT)) {
			return commandLine.getOptionValue(OUTPUT_FORMAT);
		}
		return PloozeConstants.DEFAULT_OUTPUT_FORMAT;
	}

	public boolean listFieldValues() {
		return commandLine.hasOption(LIST_VALUES);
	}
}

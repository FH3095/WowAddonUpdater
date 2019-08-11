package eu._4fh.WowAddonUpdater;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import eu._4fh.WowAddonUpdater.data.AddonInfo;
import eu._4fh.WowAddonUpdater.data.AddonInfoParser;

public class Main {
	private static final Logger log = java.util.logging.Logger.getLogger(Main.class.getName());
	private static final Option installOption = new Option("i", "install", true,
			"Installs the addon from the link. Link must be in format https://www.curseforge.com/wow/addons/deadly-boss-mods/files");
	private static final Option removeOption = new Option("r", "remove", true, "Remove the addon from the link.");
	private static final Option updateOption = new Option("u", "update", false, "Updates Addons");
	private static final Option helpOption = new Option("h", "help", false, "This help");
	private static final OptionGroup operationOptions;
	private static final Options options;

	static {
		operationOptions = new OptionGroup();
		operationOptions.setRequired(true);
		operationOptions.addOption(installOption);
		operationOptions.addOption(updateOption);
		operationOptions.addOption(removeOption);
		operationOptions.addOption(helpOption);

		options = new Options();
		options.addOptionGroup(operationOptions);
	}

	public static void main(String[] args) {
		System.setProperty("java.util.logging.config.file", "logging.properties");
		try {
			LogManager.getLogManager().readConfiguration();
		} catch (Exception e) {
			e.printStackTrace();
			Runtime.getRuntime().exit(1);
		}

		final CommandLine cmd;
		try {
			cmd = new DefaultParser().parse(options, args, false);
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}

		if (cmd.hasOption(helpOption.getOpt())) {
			new HelpFormatter().printHelp("java -jar WowAddonUpdater.jar", options);
			return;
		}

		if (cmd.hasOption(installOption.getOpt())) {
			new Main().install(Objects.requireNonNull(cmd.getOptionValue(installOption.getOpt()), "Missing addon url"));
		} else if (cmd.hasOption(updateOption.getOpt())) {
			new Main().update();
		} else if (cmd.hasOption(removeOption.getOpt())) {
			new Main().remove(Objects.requireNonNull(cmd.getOptionValue(removeOption.getOpt()), "Missing addon url"));
		} else {
			throw new IllegalArgumentException("Missing parameters");
		}
	}

	private void update() {
		final ExecutorService updaterThreads = Executors.newFixedThreadPool(Config.getInstance().getNumThreads());
		final LinkedList<Future<?>> addonUpdates = new LinkedList<>();
		final Map<String, AddonInfo> infos = AddonInfoParser.readAddonInfos(Config.getInstance().getAddonsCsv(),
				Config.getInstance().getInstalledCsv());

		for (final AddonInfo info : infos.values()) {
			addonUpdates.add(updaterThreads.submit(new AddonUpdater(info)));
		}

		for (final Future<?> updaterResult : addonUpdates) {
			try {
				updaterResult.get();
			} catch (InterruptedException | ExecutionException e) {
				log.log(Level.SEVERE, "", e);
			}
		}

		updaterThreads.shutdown();
		AddonInfoParser.writeInstalledCsv(Config.getInstance().getInstalledCsv(), infos.values());
	}

	private void install(final String urlStr) {
		final URL filesUrl;
		try {
			filesUrl = new URL(urlStr);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}

		final Map<String, AddonInfo> infos = AddonInfoParser.readAddonInfos(Config.getInstance().getAddonsCsv(),
				Config.getInstance().getInstalledCsv());
		if (infos.containsKey(filesUrl.toString())) {
			throw new IllegalArgumentException("Addon already in list");
		}
		final List<AddonInfo> newAddons = new LinkedList<>(infos.values());
		newAddons.add(new AddonInfo(filesUrl, "Release"));
		AddonInfoParser.writeAddonsCsv(Config.getInstance().getAddonsCsv(), newAddons);
	}

	private void remove(final String urlStr) {
		URL filesUrl;
		try {
			filesUrl = new URL(urlStr);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}

		final Map<String, AddonInfo> infos = AddonInfoParser.readAddonInfos(Config.getInstance().getAddonsCsv(),
				Config.getInstance().getInstalledCsv());
		if (!infos.containsKey(filesUrl.toString())) {
			throw new IllegalArgumentException("Addon not installed");
		}
		final Map<String, AddonInfo> newAddons = new LinkedHashMap<>(infos);
		AddonInfoParser.writeAddonsCsv(Config.getInstance().getAddonsCsv(), newAddons.values());
	}
}

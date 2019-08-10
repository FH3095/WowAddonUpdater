package eu._4fh.WowAddonUpdater;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.json.JSONArray;
import org.json.JSONObject;

public class Config {
	private static Config config = null;

	private static final @Nonnull String userAgent = "Mozilla/5.0 WowAddonUpdater/1.0";
	private final @Nonnull File addonsFolder;
	private final @Nonnull File addonsCsv;
	private final @Nonnull File installedCsv;
	private final int numThreads;
	private final List<DownloadStep> curseSteps;

	public Config() {
		final String jsonStr;
		try {
			jsonStr = Files.readString(new File("config.json").toPath(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		final JSONObject obj = new JSONObject(jsonStr);

		addonsFolder = new File(obj.getString("WowAddonFolder"));
		addonsCsv = new File(obj.getString("AddonsFile"));
		installedCsv = new File(obj.getString("InstalledFile"));
		numThreads = obj.getInt("Threads");
		curseSteps = readSteps(obj.getJSONArray("Curse"));

		if (!addonsFolder.isDirectory()) {
			throw new RuntimeException(
					"Addons-Directory is either missing or no directory: " + addonsFolder.toString());
		}
	}

	private @Nonnull List<DownloadStep> readSteps(final @Nonnull JSONArray array) {
		final ArrayList<DownloadStep> steps = new ArrayList<>();
		for (final Iterator<Object> it = array.iterator(); it.hasNext();) {
			steps.add(readStep((JSONObject) it.next()));
		}
		steps.trimToSize();
		return Collections.unmodifiableList(steps);
	}

	private @Nonnull DownloadStep readStep(final @Nonnull JSONObject obj) {
		final String elementsSelector = obj.getString("ElementsSelector");
		final @CheckForNull String versionSelector = obj.optString("VersionSelector", null);
		final String linkSelector = obj.getString("LinkSelector");

		final ArrayList<String> tests = new ArrayList<>();
		final @CheckForNull JSONArray testsArray = obj.optJSONArray("Tests");
		if (testsArray != null) {
			for (final Iterator<Object> it = testsArray.iterator(); it.hasNext();) {
				tests.add((String) it.next());
			}
		}
		tests.trimToSize();

		return new DownloadStep(elementsSelector, Collections.unmodifiableList(tests), versionSelector, linkSelector);

	}

	public static @Nonnull Config getInstance() {
		if (config == null) {
			config = new Config();
		}
		return config;
	}

	public @Nonnull File getAddonsFolder() {
		return addonsFolder;
	}

	public @Nonnull File getAddonsCsv() {
		return addonsCsv;
	}

	public @Nonnull File getInstalledCsv() {
		return installedCsv;
	}

	public int getNumThreads() {
		return numThreads;
	}

	public @Nonnull List<DownloadStep> getCurseSteps() {
		return curseSteps;
	}

	public @Nonnull String getUserAgent() {
		return userAgent;
	}

	public static class DownloadStep {
		private final @Nonnull String elementsSelector;
		private final @Nonnull List<String> tests;
		private final @CheckForNull String versionSelector;
		private final @Nonnull String linkSelector;

		private DownloadStep(final @Nonnull String elementsSelector, final @Nonnull List<String> tests,
				final @CheckForNull String versionSelector, final @Nonnull String linkSelector) {
			this.elementsSelector = elementsSelector;
			this.tests = tests;
			this.versionSelector = versionSelector;
			this.linkSelector = linkSelector;
		}

		public @Nonnull String getElementsSelector() {
			return elementsSelector;
		}

		public @Nonnull List<String> getTests() {
			return tests;
		}

		public @CheckForNull String getVersionSelector() {
			return versionSelector;
		}

		public @Nonnull String getLinkSelector() {
			return linkSelector;
		}

		@Override
		public String toString() {
			return "DownloadStep [elementsSelector=" + elementsSelector + ", tests=" + tests + ", versionSelector="
					+ versionSelector + ", linkSelector=" + linkSelector + "]";
		}
	}
}

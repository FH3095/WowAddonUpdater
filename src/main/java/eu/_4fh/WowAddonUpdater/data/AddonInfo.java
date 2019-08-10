package eu._4fh.WowAddonUpdater.data;

import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import eu._4fh.WowAddonUpdater.data.csv.Addon;
import eu._4fh.WowAddonUpdater.data.csv.Installed;

public class AddonInfo {
	private static final String curseforgeUrl = "www.curseforge.com";

	private final @Nonnull String name;
	private final @Nonnull URL filesUrl;
	private final @Nonnull String releaseType;
	private @CheckForNull String version;

	public AddonInfo(final @Nonnull Addon addon, final @CheckForNull Installed installed) {
		try {
			this.filesUrl = new URL(addon.getUrl());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		this.name = parseNameFromUrl(filesUrl);
		this.releaseType = addon.getReleaseType();
		this.version = installed == null ? null : installed.getVersion();
	}

	public AddonInfo(final @Nonnull URL filesUrl, final @Nonnull String releaseType) {
		this.filesUrl = filesUrl;
		this.name = parseNameFromUrl(filesUrl);
		this.releaseType = releaseType;
		this.version = null;
	}

	public void setNewVersion(final @Nonnull String newVersion) {
		this.version = newVersion;
	}

	private final @Nonnull String parseNameFromUrl(final @Nonnull URL url) {
		switch (url.getHost()) {
		case curseforgeUrl:
			return parseCurse(url);
		default:
			throw new IllegalArgumentException("Cant use url " + url.toString());
		}
	}

	private void checkCurse(final @Nonnull URL url) {
		final String host = url.getHost();
		final String path = url.getPath();

		if (host.equals("www.curseforge.com") && path.startsWith("/wow/addons/") && path.endsWith("/files")) {
			return;
		}

		throw new IllegalArgumentException(
				"Curse-URL doesnt match pattern https://www.curseforge.com/wow/addons/<AddonName>/files");
	}

	private @Nonnull String parseCurse(final @Nonnull URL url) {
		checkCurse(url);

		String path = url.getPath();

		final int filesStart = path.lastIndexOf("/files");
		path = path.substring(0, filesStart);

		final int lastSlash = path.lastIndexOf("/");
		path = path.substring(lastSlash + 1);

		return path;
	}

	public @Nonnull String getName() {
		return name;
	}

	public @Nonnull URL getFilesUrl() {
		return filesUrl;
	}

	public @Nonnull String getReleaseType() {
		return releaseType;
	}

	public @CheckForNull String getVersion() {
		return version;
	}

	public @Nonnull String getReleaseTypeLetter() {
		return releaseType.substring(0, 1).toUpperCase();
	}
}

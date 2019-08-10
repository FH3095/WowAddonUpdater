package eu._4fh.WowAddonUpdater.data.csv;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.opencsv.bean.CsvBindByName;

public class Installed implements CsvBean {
	@CsvBindByName(required = true, column = "url")
	private String url;
	@CsvBindByName(required = true, column = "version")
	private String version;

	public Installed() {
	}

	public Installed(final @Nonnull String url, final @Nonnull String version) {
		this.url = url;
		this.version = version;
	}

	public @Nonnull String getUrl() {
		Objects.requireNonNull(url, "URL must be read from csv");
		return url;
	}

	public @Nonnull String getVersion() {
		Objects.requireNonNull(version, "Version must be read from csv");
		return version;
	}
}

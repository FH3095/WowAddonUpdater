package eu._4fh.WowAddonUpdater.data.csv;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.opencsv.bean.CsvBindByName;

public class Addon implements CsvBean {
	@CsvBindByName(required = true, column = "url")
	private String url;
	@CsvBindByName(required = true, column = "releaseType")
	private String releaseType;

	public Addon() {
	}

	public Addon(final @Nonnull String url, final @Nonnull String releaseType) {
		this.url = url;
		this.releaseType = releaseType;
	}

	public @Nonnull String getUrl() {
		Objects.requireNonNull(url, "URL must be read from CSV");
		return url;
	}

	public @Nonnull String getReleaseType() {
		Objects.requireNonNull(releaseType, "ReleaseType must be read from csv");
		return releaseType;
	}
}

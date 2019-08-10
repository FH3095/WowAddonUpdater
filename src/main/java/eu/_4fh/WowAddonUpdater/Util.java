package eu._4fh.WowAddonUpdater;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Nonnull;

public class Util {
	private Util() {
	}

	public static @Nonnull File createTempFile() throws IOException {
		final File tempFile = File.createTempFile("WowAddonUpdate", ".zip");
		tempFile.deleteOnExit();
		return tempFile;
	}

	public static void unzipFile(final @Nonnull File destPath, final @Nonnull File zipFile)
			throws FileNotFoundException, IOException {
		try (final ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
			ZipEntry zipEntry;
			while ((zipEntry = zis.getNextEntry()) != null) {
				final File combined = new File(destPath, zipEntry.getName());
				if (zipEntry.isDirectory()) {
					combined.mkdir();
				} else {
					try (final BufferedOutputStream os = new BufferedOutputStream(
							new FileOutputStream(combined, false))) {
						zis.transferTo(os);
					}
				}
				zis.closeEntry();
			}
		}
	}

	public static @Nonnull HttpRequest prepareHttpRequest(final @Nonnull URL url) throws URISyntaxException {
		return HttpRequest.newBuilder().GET().uri(url.toURI()).timeout(Duration.ofSeconds(90))
				.setHeader("User-Agent", Config.getInstance().getUserAgent()).build();
	}

	public static <T> void checkResponseCode(final HttpResponse<T> response, final @Nonnull URL url) {
		if (response.statusCode() < 200 || response.statusCode() > 299) {
			throw new IllegalStateException("Cant fetch " + url.toString() + ": " + response.statusCode());
		}
	}

	public static class Pair<T1, T2> {
		final private T1 value1;
		final private T2 value2;

		public Pair(final T1 value1, final T2 value2) {
			this.value1 = value1;
			this.value2 = value2;
		}

		public T1 getValue1() {
			return value1;
		}

		public T2 getValue2() {
			return value2;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((value1 == null) ? 0 : value1.hashCode());
			result = prime * result + ((value2 == null) ? 0 : value2.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Pair)) {
				return false;
			}

			@SuppressWarnings("rawtypes")
			Pair other = (Pair) obj;

			if (!Objects.equals(value1, other.value1)) {
				return false;
			}
			if (!Objects.equals(value2, other.value2)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "Pair [value1=" + value1 + ", value2=" + value2 + "]";
		}
	}
}

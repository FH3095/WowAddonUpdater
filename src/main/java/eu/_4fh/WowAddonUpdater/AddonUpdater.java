package eu._4fh.WowAddonUpdater;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import eu._4fh.WowAddonUpdater.Util.Pair;
import eu._4fh.WowAddonUpdater.data.AddonInfo;

public class AddonUpdater implements Runnable {
	private final @Nonnull AddonInfo addonInfo;

	public AddonUpdater(final @Nonnull AddonInfo addonInfo) {
		this.addonInfo = addonInfo;
	}

	private void doUpdate() throws Exception {
		final @CheckForNull Pair<String, File> addonZipFile = new AddonDownloader(addonInfo).getZipFileForNewVersion();
		if (addonZipFile == null) {
			return;
		}
		unzipAddonFile(addonZipFile.getValue2());
		addonInfo.setNewVersion(addonZipFile.getValue1());
	}

	private void unzipAddonFile(final @Nonnull File zipFile) throws FileNotFoundException, IOException {
		Output.important("Unzip " + addonInfo.getName());

		try (final ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
			ZipEntry zipEntry;
			while ((zipEntry = zis.getNextEntry()) != null) {
				final File file = new File(Config.getInstance().getAddonsFolder(), zipEntry.getName());
				if (!zipEntry.isDirectory()) {
					file.getParentFile().mkdirs();
					try (final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
						zis.transferTo(os);
					}
				}
				zis.closeEntry();
			}
		}
		zipFile.delete();
	}

	@Override
	public void run() {
		try {
			doUpdate();
		} catch (InvalidUserInputError e) {
			Output.error("Invalid input for addon " + addonInfo.getFilesUrl() + ": " + e.getLocalizedMessage());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}

package eu._4fh.WowAddonUpdater.data;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import eu._4fh.WowAddonUpdater.data.csv.Addon;
import eu._4fh.WowAddonUpdater.data.csv.CsvBean;
import eu._4fh.WowAddonUpdater.data.csv.Installed;

public class AddonInfoParser {

	private static final char separator = ';';

	private AddonInfoParser() {
	}

	public static @Nonnull Map<String, AddonInfo> readAddonInfos(final @Nonnull File addonsFile,
			final @Nonnull File installedFile) {
		final List<Addon> addons;
		final List<Installed> installed;
		try (final FileReader addonsReader = new FileReader(addonsFile);
				final FileReader installedReader = new FileReader(installedFile)) {
			addons = Collections.unmodifiableList(new CsvToBeanBuilder<Addon>(addonsReader).withSeparator(separator)
					.withType(Addon.class).build().parse());
			installed = Collections.unmodifiableList(new CsvToBeanBuilder<Installed>(installedReader)
					.withSeparator(separator).withType(Installed.class).withThrowExceptions(true).build().parse());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		final Map<String, AddonInfo> infos = new LinkedHashMap<>(addons.size());
		final Map<String, Installed> installedByUrl = new HashMap<>();
		installed.stream().forEach(addon -> installedByUrl.put(addon.getUrl(), addon));
		for (final Addon addon : addons) {
			infos.put(addon.getUrl(), new AddonInfo(addon, installedByUrl.get(addon.getUrl())));
		}

		return Collections.unmodifiableMap(infos);
	}

	public static void writeInstalledCsv(final @Nonnull File installedFile,
			final @Nonnull Collection<AddonInfo> infos) {
		writeCsvFile(installedFile, infos, info -> new Installed(info.getFilesUrl().toString(), info.getVersion()));
	}

	public static void writeAddonsCsv(final @Nonnull File addonsFile, final @Nonnull Collection<AddonInfo> infos) {
		writeCsvFile(addonsFile, infos, info -> new Addon(info.getFilesUrl().toString(), info.getReleaseType()));
	}

	private static <T extends CsvBean> void writeCsvFile(final @Nonnull File file,
			final @Nonnull Collection<AddonInfo> infos, final Function<AddonInfo, T> infoToBean) {
		final List<T> writeList = new ArrayList<>(infos.size());
		infos.stream().forEach(info -> writeList.add(infoToBean.apply(info)));
		Collections.sort(writeList, new CsvBean.CsvBeanComparator());
		try (final FileWriter writer = new FileWriter(file)) {
			new StatefulBeanToCsvBuilder<T>(writer).withSeparator(separator).withThrowExceptions(true)
					.withOrderedResults(true).build().write(writeList);
		} catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}

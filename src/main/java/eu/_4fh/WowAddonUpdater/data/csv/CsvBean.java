package eu._4fh.WowAddonUpdater.data.csv;

import java.util.Comparator;

import javax.annotation.Nonnull;

public interface CsvBean {
	public @Nonnull String getUrl();

	public static final class CsvBeanComparator implements Comparator<CsvBean> {
		@Override
		public int compare(final CsvBean o1, final CsvBean o2) {
			return o1.getUrl().compareTo(o2.getUrl());
		}
	}
}

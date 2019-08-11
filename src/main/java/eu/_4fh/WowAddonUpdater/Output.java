package eu._4fh.WowAddonUpdater;

import java.io.PrintStream;

import javax.annotation.Nonnull;

public class Output {
	private Output() {

	}

	public static enum OutputTypes {
		DEBUG(40, System.out), NORMAL(30, System.out), IMPORTANT(20, System.out), ERROR(10, System.err);
		private final int level;
		private final PrintStream stream;

		private OutputTypes(final int level, final PrintStream stream) {
			this.level = level;
			this.stream = stream;
		}
	}

	public static void debug(final @Nonnull String... messages) {
		out(OutputTypes.DEBUG, messages);
	}

	public static void normal(final @Nonnull String... messages) {
		out(OutputTypes.NORMAL, messages);
	}

	public static void important(final @Nonnull String... messages) {
		out(OutputTypes.IMPORTANT, messages);
	}

	public static void error(final @Nonnull String... messages) {
		out(OutputTypes.ERROR, messages);
	}

	private synchronized static void out(final @Nonnull OutputTypes outputType, final @Nonnull String... messages) {
		if (outputType.level <= Config.getInstance().getOutputLevel().level) {
			for (final String message : messages) {
				outputType.stream.println(message);
			}
		}
	}
}

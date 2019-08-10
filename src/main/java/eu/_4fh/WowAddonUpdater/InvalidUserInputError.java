package eu._4fh.WowAddonUpdater;

public class InvalidUserInputError extends Exception {
	private static final long serialVersionUID = -6729282460980317723L;

	public InvalidUserInputError(String s) {
		super(s);
	}

	public InvalidUserInputError(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidUserInputError(Throwable cause) {
		super(cause);
	}
}

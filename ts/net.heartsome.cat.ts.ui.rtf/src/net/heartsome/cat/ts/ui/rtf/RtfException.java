package net.heartsome.cat.ts.ui.rtf;

/**
 * Unchecked exception class for exceptions while building or writing the RTF document.
 */
public class RtfException extends RuntimeException {
	private static final long serialVersionUID = -3852613760579815760L;

	/**
	 * Default constructor.
	 */
	public RtfException() {
	}

	/**
	 * RTF exception with message and a reason.
	 * @param message
	 *            Error message.
	 * @param cause
	 *            Reason.
	 */
	public RtfException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * RTF exception with message.
	 * @param message
	 *            Error message.
	 */
	public RtfException(String message) {
		super(message);
	}

	/**
	 * RTF exception encapsulating the reason.
	 * @param cause
	 *            Reason.
	 */
	public RtfException(Throwable cause) {
		super(cause);
	}
}

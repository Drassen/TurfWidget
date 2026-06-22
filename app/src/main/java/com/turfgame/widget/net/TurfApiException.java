package com.turfgame.widget.net;

/**
 * Raised when a Turf API request cannot be completed. The {@link Reason}
 * lets the UI layer pick an appropriate message without inspecting strings.
 */
public class TurfApiException extends Exception {

    public enum Reason {
        /** No account email has been configured by the user. */
        NO_ACCOUNT,
        /** The request failed at the transport level (no connectivity, timeout, ...). */
        NETWORK,
        /** The server responded with an unexpected status code. */
        SERVER,
        /** The response body could not be parsed. */
        PARSE
    }

    private final Reason reason;

    public TurfApiException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public TurfApiException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}

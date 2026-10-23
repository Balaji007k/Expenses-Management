package javax.security.sasl;

import java.io.IOException;

/**
 * Standard Java SASL Exception required by MongoDB driver on Android.
 */
public class SaslException extends IOException {
    private static final long serialVersionUID = -8950787646149817779L;

    public SaslException() {
        super();
    }

    public SaslException(String detail) {
        super(detail);
    }

    public SaslException(String detail, Throwable ex) {
        super(detail);
        if (ex != null) {
            initCause(ex);
        }
    }
}

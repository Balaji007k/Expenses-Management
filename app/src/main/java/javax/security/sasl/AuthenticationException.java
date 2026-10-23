package javax.security.sasl;

/**
 * Standard Java SASL AuthenticationException required by MongoDB driver on Android.
 */
public class AuthenticationException extends SaslException {
    private static final long serialVersionUID = -3579708765071867955L;

    public AuthenticationException() {
        super();
    }

    public AuthenticationException(String detail) {
        super(detail);
    }

    public AuthenticationException(String detail, Throwable ex) {
        super(detail, ex);
    }
}

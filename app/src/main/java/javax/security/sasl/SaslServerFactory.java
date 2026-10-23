package javax.security.sasl;

import java.util.Map;
import javax.security.auth.callback.CallbackHandler;

public interface SaslServerFactory {
    SaslServer createSaslServer(
            String mechanism,
            String protocol,
            String serverName,
            Map<String, ?> props,
            CallbackHandler cbh) throws SaslException;

    String[] getMechanismNames(Map<String, ?> props);
}

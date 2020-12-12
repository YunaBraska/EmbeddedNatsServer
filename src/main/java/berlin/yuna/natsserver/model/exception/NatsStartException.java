package berlin.yuna.natsserver.model.exception;

public class NatsStartException extends RuntimeException {

    public NatsStartException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

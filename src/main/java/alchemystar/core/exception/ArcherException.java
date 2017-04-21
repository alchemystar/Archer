package alchemystar.core.exception;

/**
 * ArcherException
 *
 * @Author lizhuyang
 */
public class ArcherException extends RuntimeException {

    public ArcherException() {
        super();
    }

    public ArcherException(String message) {
        super(message);
    }

    public ArcherException(Throwable cause) {
        super(cause);
    }
}

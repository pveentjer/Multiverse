package org.multiverse.api.exceptions;

/**
 * An {@link IllegalTxnStateException} thrown when a write action is executed using
 * a readonly {@link org.multiverse.api.Txn}.
 *
 * @author Peter Veentjer.
 * @see org.multiverse.api.TxnFactoryBuilder#setReadonly(boolean)
 */
public class ReadonlyException extends IllegalTxnStateException {

    private static final long serialVersionUID = 0;

    /**
     * Creates a new ReadonlyException.
     *
     * @param message the message of the exception.
     */
    public ReadonlyException(String message) {
        super(message);
    }

    /**
     * Creates a new ReadonlyException.
     *
     * @param message the message of the exception.
     * @param cause   the cause of the exception.
     */
    public ReadonlyException(String message, Throwable cause) {
        super(message, cause);
    }

}

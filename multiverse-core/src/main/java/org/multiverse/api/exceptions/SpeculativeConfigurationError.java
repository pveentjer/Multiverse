package org.multiverse.api.exceptions;

/**
 * A {@link ControlFlowError} thrown for dealing with the speculative configuration mechanism.
 *
 * <p>Multiverse uses a speculative configuration mechanism if enabled makes certain optimizations possible. E.g. one of the optimizations
 * is to use different {@link org.multiverse.api.Txn} implementations that are optimized for certain transaction lengths. As
 * long as the speculation is not violated, you will get better performance than when a more heavy weight transaction/configuration.
 *
 * <p>So ControlFlowErrors are not something bad, but just a way for the STM to figure out what the cheapest settings are for
 * performance/scalability.
 *
 * <h3>Unexpected retries</h3>
 *
 * <p>Because a transaction can fail on a speculative failure more than once, it could be that the transaction is retried. Normally
 * this is not an issue, since the transaction will be retried, so is invisible. And once the {@link org.multiverse.api.TxnExecutor}
 * has learned, it will not make the same mistakes again, but if you do io (e.g. print to the System.out or do logging) you can expect
 * to see aborts, even though there is no other reason to.
 *
 * Speculative behavior can be turned of (either on the Txn or STM level) but you will not get the best out of performance. For
 * the speculative behavior to learn, it is important that the {@link org.multiverse.api.TxnExecutor} is reused.
 *
 * @author Peter Veentjer.
 * @see org.multiverse.api.TxnFactoryBuilder#setSpeculative(boolean)
 */
public class SpeculativeConfigurationError extends ControlFlowError {

    private static final long serialVersionUID = 0;

    public final static SpeculativeConfigurationError INSTANCE = new SpeculativeConfigurationError(false);

    /**
     * Creates a SpeculativeConfigurationError.
     *
     * @param fillStackTrace if the StackTrace should be filled.
     */
    public SpeculativeConfigurationError(boolean fillStackTrace) {
        super(fillStackTrace);
    }

    /**
     * Creates a SpeculativeConfigurationError with the provided message.
     *
     * @param message the message of the exception.
     */
    public SpeculativeConfigurationError(String message) {
        super(true, message);
    }

    /**
     * Creates a SpeculativeConfigurationError with the provided message and cause.
     *
     * @param message the message of the exception.
     * @param cause   the cause of the exception.
     */
    public SpeculativeConfigurationError(String message, Throwable cause) {
        super(true, message, cause);
    }
}

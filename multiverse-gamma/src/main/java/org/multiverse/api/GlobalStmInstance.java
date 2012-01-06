package org.multiverse.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.lang.reflect.Modifier.isStatic;

/**
 * A singleton that can be used for easy access to the {@link Stm} that is used globally. Once it has
 * been set, it should not be changed while running the system.
 *
 * <p>Using the GlobalStmInstance imposes some limitations (like 1 global Stm instance that is used by everything) but makes the
 * system a lot easier to use. But if the GlobalStmInstance should not be used, but a 'private' Stm, you need to carry around
 * the Stm reference yourself and just ignore this GlobalStmInstance.
 *
 * <h3>Initialization</h3>
 *
 * <p>The default implementation is the GammaStm for now. It can be configured through setting the System property:
 * 'org.multiverse.api.GlobalStmInstance.factoryMethod'. This method should be a no arg static method that returns a
 * {@link Stm} instance.
 *
 * @author Peter Veentjer
 */
public final class GlobalStmInstance {

    private static final String KEY = GlobalStmInstance.class.getName() + ".factoryMethod";

    private static final String DEFAULT_FACTORY_METHOD = "org.multiverse.stms.gamma.GammaStm.createFast";

    private static final Logger logger = Logger.getLogger(GlobalStmInstance.class.getName());

    private static final Stm instance;

    static {
        String factoryMethod = System.getProperty(KEY, DEFAULT_FACTORY_METHOD);
        logger.info(format("Initializing GlobalStmInstance using factoryMethod '%s'.", factoryMethod));
        try {
            Method method = getMethod(factoryMethod);
            instance = (Stm) method.invoke(null);
            logger.info(format("Successfully initialized GlobalStmInstance using factoryMethod '%s'.", factoryMethod));
        } catch (IllegalAccessException e) {
            String msg = format("Failed to initialize the GlobalStmInstance through System property '%s' with value '%s'." +
                    "Reason: factory method '%s' is not accessible (it should be public)').",
                    KEY, factoryMethod, factoryMethod);
            logger.severe(msg);
            throw new IllegalArgumentException(msg, e);
        } catch (ClassCastException e) {
            String msg = format("Failed to initialize GlobalStmInstance through System property '%s' with value '%s'." +
                    "Reason: factory method '%s' is not accessible (it should be public)').",
                    KEY, factoryMethod, factoryMethod);
            logger.severe(msg);
            throw new IllegalArgumentException(msg, e);
        } catch (InvocationTargetException e) {
            String msg = format("Failed to initialize the GlobalStmInstance through System property '%s' with value '%s'." +
                    "Reason: factory method '%s' failed to be invoked.",
                    KEY, factoryMethod, factoryMethod);
            logger.severe(msg);
            throw new IllegalArgumentException(msg, e);
        }
    }

    private static Method getMethod(String factoryMethod) {
        int indexOf = factoryMethod.lastIndexOf('.');
        if (indexOf == -1) {
            String msg = format(
                    "Failed to initialize the GlobalStmInstance through System property '%s' with value '%s'. " +
                            "Reason: It is not a valid factory method, it should be something like 'com.SomeStm.createSomeStm()').",
                    KEY, factoryMethod);
            logger.info(msg);
            throw new IllegalArgumentException();
        }

        String className = factoryMethod.substring(0, indexOf);
        Class clazz;
        try {
            clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            String msg = format("Failed to initialize the GlobalStmInstance through System property '%s' with value '%s'." +
                    "Reason: '%s' is not an existing class (it can't be found using the Thread.currentThread.getContextClassLoader).",
                    KEY, className, factoryMethod);
            logger.info(msg);
            throw new IllegalArgumentException(msg, e);
        }

        String methodName = factoryMethod.substring(indexOf + 1);
        if (methodName.length() == 0) {
            String msg = format("Failed to initialize the GlobalStmInstance through System property '%s' with value '%s'." +
                    "Reason: the factory method is does not exist, it should be something like %s.createSomeStm.",
                    KEY, className, factoryMethod);
            logger.info(msg);
            throw new IllegalArgumentException(msg);
        }

        Method method;
        try {
            method = clazz.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            String msg = format("Failed to initialize the GlobalStmInstance through System property '%s' with value '%s'." +
                    "Reason: the factory method does not exist. Remember that it should not have any arguments.",
                    KEY, factoryMethod);
            logger.info(msg);
            throw new IllegalArgumentException(msg, e);
        }

        if (!isStatic(method.getModifiers())) {
            String msg = format("Failed to initialize the GlobalStmInstance through System property '%s' with value '%s'." +
                    "Reason: the factory method is not static.",
                    KEY, factoryMethod);
            logger.info(msg);
            throw new IllegalArgumentException(msg);
        }

        return method;
    }

    /**
     * Gets the global {@link Stm} instance. The returned value will never be null.
     *
     * @return the global Stm instance.
     */
    public static Stm getGlobalStmInstance() {
        return instance;
    }

    //we don't want instances.

    private GlobalStmInstance() {
    }
}

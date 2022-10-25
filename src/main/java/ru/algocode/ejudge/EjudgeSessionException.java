package ru.algocode.ejudge;

/**
 * @author Perveev Mike
 */
public class EjudgeSessionException extends RuntimeException {
    public EjudgeSessionException(final String message) {
        super(String.format("Error happened while working with ejudge: \"%s\"", message));
    }

    public EjudgeSessionException(final String message, final Throwable cause) {
        super(String.format("Error happened while working with ejudge: \"%s\"", message), cause);
    }
}

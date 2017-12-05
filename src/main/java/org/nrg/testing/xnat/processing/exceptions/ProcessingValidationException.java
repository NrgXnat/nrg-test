package org.nrg.testing.xnat.processing.exceptions;

public abstract class ProcessingValidationException extends Exception {

    public ProcessingValidationException(String message) {
        super(message);
    }

    public ProcessingValidationException() {}

}

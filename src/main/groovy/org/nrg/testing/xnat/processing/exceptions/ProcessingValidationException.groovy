package org.nrg.testing.xnat.processing.exceptions

abstract class ProcessingValidationException extends Exception {

    ProcessingValidationException(String message) {
        super(message)
    }

    ProcessingValidationException() {}

}

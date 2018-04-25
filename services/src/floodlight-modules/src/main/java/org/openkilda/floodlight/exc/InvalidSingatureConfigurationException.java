package org.openkilda.floodlight.exc;

public class InvalidSingatureConfigurationException extends Exception {
    public InvalidSingatureConfigurationException(String details, Throwable throwable) {
        super(details, throwable);
    }
}

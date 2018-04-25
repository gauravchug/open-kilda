package org.openkilda.floodlight.exc;

public class CorruptedNetworkDataException extends Exception {
    public CorruptedNetworkDataException() {
        this("Corrupted network data");
    }

    public CorruptedNetworkDataException(String details) {
        super(details);
    }
}

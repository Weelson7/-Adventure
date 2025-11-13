package org.adventure.persistence;

import java.io.IOException;

/**
 * Exception thrown when file checksum validation fails.
 * Indicates file corruption or tampering.
 * 
 * @see SaveManager
 */
public class ChecksumMismatchException extends IOException {
    
    public ChecksumMismatchException(String message) {
        super(message);
    }
    
    public ChecksumMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}

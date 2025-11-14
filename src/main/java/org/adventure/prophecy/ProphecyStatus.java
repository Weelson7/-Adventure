package org.adventure.prophecy;

/**
 * Represents the current status of a prophecy.
 */
public enum ProphecyStatus {
    /** Prophecy is hidden/unknown */
    HIDDEN,
    
    /** Prophecy has been revealed but not yet triggered */
    REVEALED,
    
    /** Prophecy is in progress (trigger conditions being met) */
    IN_PROGRESS,
    
    /** Prophecy has been fulfilled */
    FULFILLED,
    
    /** Prophecy has been prevented/failed */
    FAILED
}

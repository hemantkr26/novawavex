package com.novawavex.novawavex.workflow;

public enum ExecutionStatus {

    QUEUED,

    RUNNING,

    COMPLETED,

    FAILED,

    CANCELLED;


    /*
     * =========================================
     * STATUS TRANSITION VALIDATION
     * =========================================
     */

    public boolean canTransitionTo(
            ExecutionStatus nextStatus) {

        if (nextStatus == null) {
            return false;
        }

        switch (this) {

            case QUEUED:
                return nextStatus == RUNNING
                        || nextStatus == CANCELLED;

            case RUNNING:
                return nextStatus == COMPLETED
                        || nextStatus == FAILED
                        || nextStatus == CANCELLED;

            case COMPLETED:
                return false;

            case FAILED:
                return false;

            case CANCELLED:
                return false;

            default:
                return false;
        }
    }
}
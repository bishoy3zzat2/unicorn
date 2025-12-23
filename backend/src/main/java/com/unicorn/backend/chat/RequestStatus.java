package com.unicorn.backend.chat;

/**
 * Enum representing the status of a chat request from Elite startups.
 */
public enum RequestStatus {
    /**
     * Request is pending investor response.
     */
    PENDING,

    /**
     * Request has been accepted by the investor.
     */
    ACCEPTED,

    /**
     * Request has been declined by the investor.
     */
    DECLINED
}

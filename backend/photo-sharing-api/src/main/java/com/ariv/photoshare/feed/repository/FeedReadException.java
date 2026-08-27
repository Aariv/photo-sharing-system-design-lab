package com.ariv.photoshare.feed.repository;

public class FeedReadException
        extends RuntimeException {

    public FeedReadException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}
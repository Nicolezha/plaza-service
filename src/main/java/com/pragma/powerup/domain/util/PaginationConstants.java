package com.pragma.powerup.domain.util;

public class PaginationConstants {
    private PaginationConstants() {
        /* This utility class should not be instantiated */
    }

    public static final int MIN_PAGE = 0;
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 50;

    public static final String PAGE_INVALID_MESSAGE =
            "Page number must be greater than or equal to 0";

    public static final String SIZE_INVALID_MESSAGE =
            "Page size must be between 1 and 50";
}

package com.pragma.powerup.domain.validation;

import com.pragma.powerup.domain.exception.InvalidPaginationException;
import org.junit.jupiter.api.Test;

import static com.pragma.powerup.domain.util.PaginationConstants.*;
import static org.junit.jupiter.api.Assertions.*;

class PaginationValidatorTest {

    private final PaginationValidator paginationValidator = new PaginationValidator();

    @Test
    void validatePaginationShouldPassWhenPageAndSizeAreValid() {
        assertDoesNotThrow(() -> paginationValidator.validatePagination(0, 10));
    }

    @Test
    void validatePaginationShouldPassWhenSizeIsAtTheLimits() {
        assertDoesNotThrow(() -> paginationValidator.validatePagination(MIN_PAGE, MIN_SIZE));
        assertDoesNotThrow(() -> paginationValidator.validatePagination(MIN_PAGE, MAX_SIZE));
    }

    @Test
    void validatePaginationShouldThrowWhenPageIsNegative() {
        InvalidPaginationException ex = assertThrows(InvalidPaginationException.class,
                () -> paginationValidator.validatePagination(-1, 10));

        assertEquals(PAGE_INVALID_MESSAGE, ex.getMessage());
    }

    @Test
    void validatePaginationShouldThrowWhenSizeIsBelowMinimum() {
        InvalidPaginationException ex = assertThrows(InvalidPaginationException.class,
                () -> paginationValidator.validatePagination(0, MIN_SIZE - 1));

        assertEquals(SIZE_INVALID_MESSAGE, ex.getMessage());
    }

    @Test
    void validatePaginationShouldThrowWhenSizeIsAboveMaximum() {
        InvalidPaginationException ex = assertThrows(InvalidPaginationException.class,
                () -> paginationValidator.validatePagination(0, MAX_SIZE + 1));

        assertEquals(SIZE_INVALID_MESSAGE, ex.getMessage());
    }
}

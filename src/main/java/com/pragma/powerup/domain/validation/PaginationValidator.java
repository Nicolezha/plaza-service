package com.pragma.powerup.domain.validation;

import com.pragma.powerup.domain.exception.InvalidPaginationException;

import static com.pragma.powerup.domain.util.PaginationConstants.*;

public class PaginationValidator {

    public void validatePagination(int page, int size){
        if(page<MIN_PAGE){
            throw new InvalidPaginationException(PAGE_INVALID_MESSAGE);
        }

        if(size<MIN_SIZE || size>MAX_SIZE){
            throw new InvalidPaginationException(SIZE_INVALID_MESSAGE);
        }
    }
}

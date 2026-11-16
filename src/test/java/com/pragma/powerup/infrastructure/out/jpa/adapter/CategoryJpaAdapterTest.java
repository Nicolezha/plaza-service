package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.infrastructure.out.jpa.repository.ICategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryJpaAdapterTest {

    @Mock
    private ICategoryRepository categoryRepository;

    @InjectMocks
    private CategoryJpaAdapter categoryJpaAdapter;

    @Test
    void existsByIdShouldReturnWhatTheRepositoryReturns() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertTrue(categoryJpaAdapter.existsById(1L));
        assertFalse(categoryJpaAdapter.existsById(99L));
    }
}

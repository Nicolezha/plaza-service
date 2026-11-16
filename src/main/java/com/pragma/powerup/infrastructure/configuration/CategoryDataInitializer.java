package com.pragma.powerup.infrastructure.configuration;

import com.pragma.powerup.infrastructure.out.jpa.entity.CategoryEntity;
import com.pragma.powerup.infrastructure.out.jpa.repository.ICategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryDataInitializer implements ApplicationRunner {

    private static final List<String> DEFAULT_CATEGORIES =
            List.of("ENTRADAS", "PLATOS FUERTES", "POSTRES", "BEBIDAS");

    private final ICategoryRepository categoryRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        for (String name : DEFAULT_CATEGORIES) {
            if (!categoryRepository.existsByName(name)) {
                categoryRepository.save(new CategoryEntity(null, name));
            }
        }
    }
}

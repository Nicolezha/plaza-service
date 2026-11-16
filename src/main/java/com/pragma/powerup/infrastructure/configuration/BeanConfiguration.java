package com.pragma.powerup.infrastructure.configuration;

import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.IUserClientPort;
import com.pragma.powerup.domain.usecase.RestaurantUseCase;
import com.pragma.powerup.domain.validation.RestaurantValidator;
import com.pragma.powerup.infrastructure.out.jpa.adapter.RestaurantJpaAdapter;
import com.pragma.powerup.infrastructure.out.feign.IUserFeignClient;
import com.pragma.powerup.infrastructure.out.feign.UserFeignAdapter;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IRestaurantEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IRestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BeanConfiguration {
    private final IRestaurantRepository restaurantRepository;
    private final IRestaurantEntityMapper restaurantEntityMapper;
    private final IUserFeignClient userFeignClient;

    @Bean
    public IRestaurantPersistencePort restaurantPersistencePort() {
        return new RestaurantJpaAdapter(restaurantRepository, restaurantEntityMapper);
    }

    @Bean
    public IRestaurantServicePort restaurantServicePort() {
        return new RestaurantUseCase(restaurantPersistencePort(), restaurantValidator(), userClientPort());
    }

    @Bean
    public RestaurantValidator restaurantValidator(){
        return new RestaurantValidator();
    }

    @Bean
    public IUserClientPort userClientPort(){
        return new UserFeignAdapter(userFeignClient);
    }
}
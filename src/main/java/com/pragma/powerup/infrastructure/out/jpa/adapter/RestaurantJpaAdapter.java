package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.infrastructure.out.jpa.entity.RestaurantEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IRestaurantEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IRestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class RestaurantJpaAdapter implements IRestaurantPersistencePort {

    private final IRestaurantRepository restaurantRepository;
    private final IRestaurantEntityMapper restaurantEntityMapper;

    @Override
    public PageModel<RestaurantModel> findAllOrderedByName(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<RestaurantEntity> result = restaurantRepository.findAll(pageable);

        List<RestaurantModel> content = result.getContent().stream()
                .map(restaurantEntityMapper::toRestaurantModel).toList();

        return new PageModel<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public RestaurantModel saveRestaurant(RestaurantModel restaurantModel) {
        RestaurantEntity restaurantEntity = restaurantRepository.save(restaurantEntityMapper.toEntity(restaurantModel));
        return restaurantEntityMapper.toRestaurantModel(restaurantEntity);
    }

    @Override
    public boolean existsByNit(String nit) {
        return restaurantRepository.existsByNit(nit);
    }

    @Override
    public Optional<RestaurantModel> findById(Long id) {
        return restaurantRepository.findById(id).map(restaurantEntityMapper::toRestaurantModel);
    }
}
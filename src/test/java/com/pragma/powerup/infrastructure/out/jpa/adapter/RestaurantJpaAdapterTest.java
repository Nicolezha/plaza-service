package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.infrastructure.out.jpa.entity.RestaurantEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IRestaurantEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IRestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantJpaAdapterTest {

    @Mock
    private IRestaurantRepository restaurantRepository;

    @Mock
    private IRestaurantEntityMapper restaurantEntityMapper;

    @InjectMocks
    private RestaurantJpaAdapter restaurantJpaAdapter;

    @Test
    void saveRestaurantShouldMapSaveAndMapBack(){
        RestaurantModel model = new RestaurantModel();
        RestaurantEntity entity = new RestaurantEntity();
        RestaurantModel savedModel = new RestaurantModel();
        RestaurantEntity savedEntity = new RestaurantEntity();

        when(restaurantEntityMapper.toEntity(model)).thenReturn(entity);
        when(restaurantRepository.save(entity)).thenReturn(savedEntity);
        when(restaurantEntityMapper.toRestaurantModel(savedEntity)).thenReturn(savedModel);

        RestaurantModel result = restaurantJpaAdapter.saveRestaurant(model);

        assertSame(savedModel, result);
    }

    @Test
    void existsByNitShouldReturnWhatTheRepositoryReturns(){
        when(restaurantRepository.existsByNit("900123456")).thenReturn(true);
        when(restaurantRepository.existsByNit("111")).thenReturn(false);

        assertTrue(restaurantJpaAdapter.existsByNit("900123456"));
        assertFalse(restaurantJpaAdapter.existsByNit("111"));
    }

    @Test
    void findByIdShouldReturnTheMappedRestaurantWhenItExists(){
        RestaurantEntity entity = new RestaurantEntity();
        RestaurantModel model = new RestaurantModel();

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(restaurantEntityMapper.toRestaurantModel(entity)).thenReturn(model);

        Optional<RestaurantModel> result = restaurantJpaAdapter.findById(1L);

        assertTrue(result.isPresent());
        assertSame(model, result.get());
    }

    @Test
    void findByIdShouldReturnEmptyWhenTheRestaurantDoesNotExist(){
        when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(restaurantJpaAdapter.findById(99L).isEmpty());
    }

    @Test
    void findAllOrderedByNameShouldRequestPageSortedByNameAndMapToPageModel() {
        RestaurantEntity entity = new RestaurantEntity();
        RestaurantModel model = new RestaurantModel();
        Page<RestaurantEntity> springPage =
                new PageImpl<>(List.of(entity), PageRequest.of(1, 2), 3);

        when(restaurantRepository.findAll(any(Pageable.class))).thenReturn(springPage);
        when(restaurantEntityMapper.toRestaurantModel(entity)).thenReturn(model);

        PageModel<RestaurantModel> result = restaurantJpaAdapter.findAllOrderedByName(1, 2);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(restaurantRepository).findAll(captor.capture());
        Pageable pageable = captor.getValue();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(2, pageable.getPageSize());
        assertEquals(Sort.by("name").ascending(), pageable.getSort());

        assertEquals(List.of(model), result.getContent());
        assertEquals(1, result.getPage());
        assertEquals(2, result.getSize());
        assertEquals(3L, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
    }

}
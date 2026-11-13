package com.pragma.powerup.infrastructure.out.jpa.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface IUserFeignClient {

    @GetMapping("/api/v1/user/{id}")
    UserRoleResponseDto getUserRole(@PathVariable("id") Long id);

}

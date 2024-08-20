package com.hotel.flint.dining.repository;

import com.hotel.flint.dining.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByMenuName(String name);
}
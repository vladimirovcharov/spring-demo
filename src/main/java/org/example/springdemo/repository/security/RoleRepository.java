package org.example.springdemo.repository.security;

import java.util.Optional;

import org.example.springdemo.model.security.Role;
import org.example.springdemo.model.security.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(Roles name);
}
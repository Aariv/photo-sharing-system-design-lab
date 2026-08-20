package com.ariv.photoshare.user.repository;

import com.ariv.photoshare.post.entity.PostEntity;
import com.ariv.photoshare.user.entity.UserEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<UserEntity, UUID> {
    
    // Equivalent to: SELECT * FROM users WHERE username = :username
    public boolean existsByUsername(String username) {
        return find("username", username).firstResultOptional().isPresent();
    }
}
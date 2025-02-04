package io.github.nunesdev.social.domain.repositories;

import io.github.nunesdev.social.domain.models.User;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, Long> {
}

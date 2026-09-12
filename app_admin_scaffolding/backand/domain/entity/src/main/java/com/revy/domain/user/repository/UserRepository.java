package com.revy.domain.user.repository;


import com.revy.domain.base.BaseRepository;
import com.revy.domain.user.User;

import java.util.UUID;

public interface UserRepository extends BaseRepository<User, UUID> {
}

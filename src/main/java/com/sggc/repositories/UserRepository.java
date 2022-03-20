package com.sggc.repositories;

import com.sggc.models.User;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for retrieving users
 */
@Repository
@EnableScan
public interface UserRepository extends CrudRepository<User ,String> {
}

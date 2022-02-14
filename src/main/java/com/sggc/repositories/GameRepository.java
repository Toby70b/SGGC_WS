package com.sggc.repositories;

import com.sggc.models.Game;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

@EnableScan
public interface GameRepository extends CrudRepository<Game, String> {
    Game findGameByAppid(String appId);
}

package com.sbeam.gameserver.repository;

import com.sbeam.gameserver.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> { //直接继承CURD，前面为类名，后面为主键数据类型

    Optional<Player> findByEmail(String email);

    Optional<Player> findByNickname(String nickname);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}
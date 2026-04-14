package com.sbeam.gameserver.repository;

import com.sbeam.gameserver.entity.Player;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

//直接在注解中加上属性
//@SpringBootTest(properties = {
//        "spring.datasource.url=jdbc:h2:mem:gameserver;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
//        "spring.datasource.driver-class-name=org.h2.Driver",
//        "spring.datasource.username=sa",
//        "spring.datasource.password=",
//        "spring.jpa.hibernate.ddl-auto=create-drop"
//})

//用properties方式可以灵活切换测试的数据库（真实MySQL/内存h2）。
@SpringBootTest
@ActiveProfiles("h2")
class PlayerRepositoryTest {

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    void playerRepositoryCanReadAndWriteToSqlDatabase() {
        Player player = new Player();
        player.setEmail("test@qq.com");
        player.setNickname("yukino01");
        player.setPasswordHash("hash");

        Player savedPlayer = playerRepository.save(player);
        Optional<Player> foundPlayer = playerRepository.findByEmail(savedPlayer.getEmail());

        assertThat(foundPlayer).isPresent();
        assertThat(foundPlayer.get().getNickname()).isEqualTo("yukino01");
        assertThat(playerRepository.count()).isEqualTo(1);
    }
}
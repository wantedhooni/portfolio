package com.revy.authapp.app.infra;

import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;


/**
 * H2 DB 웹으로 접근시 불편해서
 * 다른 DB 도구으로 포트를 뚫기 위한 설정
 */

@Configuration
public class H2TcpServerConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2TcpServer() throws SQLException {
        // -tcpAllowOthers: 외부에서 접근 허용 (로컬만이면 빼도 됨)
        // -ifNotExists: 없으면 생성
        return Server.createTcpServer(
                "-tcp", "-tcpPort", "13306",
                "-tcpAllowOthers",
                "-ifNotExists"
        );
    }
}
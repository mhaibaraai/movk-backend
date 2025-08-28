/*
 * @Author yixuanmiao
 * @Date 2025/08/28 10:34
 */

package com.movk.config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * SSH 隧道配置
 * 使用统一的 forwards 列表来声明需要建立的端口转发
 */
@Slf4j
@Data
@Component
@Configuration
@ConfigurationProperties(prefix = "ssh")
@ConditionalOnProperty(name = "ssh.enabled", havingValue = "true")
public class SshTunnelConfig {

    private boolean enabled;
    private String host;
    private int port = 22;
    private String username;
    private String password;
    private String privateKeyPath;
    private String passphrase;

    private List<Forward> forwards = new ArrayList<>();

    private Config config = new Config();

    private Session session;
    private final List<Integer> forwardedLocalPorts = new ArrayList<>();

    @Data
    public static class Forward {
        private String name;
        private Boolean enabled = false;
        private String remoteHost = "localhost";
        private Integer remotePort;
        private Integer localPort;
    }

    @Data
    public static class Config {
        private String strictHostKeyChecking = "no";
        private int connectionTimeout = 30000;
        private int keepAliveInterval = 60000;
    }

    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info("SSH隧道已禁用");
            return;
        }
        try {
            establishSshTunnel();
        } catch (Exception e) {
            throw new RuntimeException("无法建立SSH隧道连接", e);
        }
    }

    // 建立SSH隧道连接
    private void establishSshTunnel() throws Exception {
        JSch jsch = new JSch();

        // 认证
        if (privateKeyPath != null && !privateKeyPath.isEmpty()) {
            log.info("使用私钥认证: {}", privateKeyPath);
            if (passphrase != null && !passphrase.isEmpty()) {
                jsch.addIdentity(privateKeyPath, passphrase);
            } else {
                jsch.addIdentity(privateKeyPath);
            }
        }

        session = jsch.getSession(username, host, port);
        if (password != null && !password.isEmpty()) {
            session.setPassword(password);
        }

        // 会话配置
        Properties sessionConfig = new Properties();
        sessionConfig.put("StrictHostKeyChecking", config.strictHostKeyChecking);
        sessionConfig.put("PreferredAuthentications", "publickey,password");
        session.setConfig(sessionConfig);
        session.setServerAliveInterval(config.keepAliveInterval);
        session.setTimeout(config.connectionTimeout);

        // 连接
        log.info("正在连接SSH服务器: {}:{}", host, port);
        session.connect(config.connectionTimeout);

        // 建立端口转发
        if (forwards != null) {
            for (Forward f : forwards) {
                if (!Boolean.TRUE.equals(f.getEnabled())) {
                    continue;
                }
                int local = (f.getLocalPort() != null) ? f.getLocalPort() : 15432;
                int assignedPort = session.setPortForwardingL(local, f.getRemoteHost(), f.getRemotePort());
                forwardedLocalPorts.add(assignedPort);
                log.info("SSH隧道建立成功 [{}]: localhost:{} -> {}:{} (通过 {}:{})",
                        f.getName(),
                        assignedPort,
                        f.getRemoteHost(),
                        f.getRemotePort(),
                        host,
                        port
                );
            }
        }
    }

    @PreDestroy
    public void destroy() {
        if (session != null && session.isConnected()) {
            try {
                for (Integer port : forwardedLocalPorts) {
                    try {
                        session.delPortForwardingL(port);
                    } catch (Exception ignore) {
                        // 忽略单条删除异常
                    }
                }
                session.disconnect();
                log.info("SSH隧道已关闭");
            } catch (Exception e) {
                log.error("关闭SSH隧道时出错", e);
            }
        }
    }

    public boolean isConnected() {
        return session != null && session.isConnected();
    }
}

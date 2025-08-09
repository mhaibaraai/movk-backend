package com.movk.base.config;

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

import java.util.Properties;

/**
 * SSH隧道配置类
 * 用于建立SSH隧道连接到远程数据库
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

    private Database database = new Database();
    private Config config = new Config();

    private Session session;

    @Data
    public static class Database {
        private String remoteHost = "localhost";
        private int remotePort = 5432;
        private int localPort = 5432;
    }

    @Data
    public static class Config {
        private String strictHostKeyChecking = "no";
        private int connectionTimeout = 30000;
        private int keepAliveInterval = 60000;
        private int maxReconnectAttempts = 3;
        private int reconnectDelay = 5000;
    }

    /**
     * 初始化SSH隧道
     */
    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info("SSH隧道已禁用");
            return;
        }

        try {
            establishSshTunnel();
        } catch (Exception e) {
            log.error("建立SSH隧道失败", e);
            // 尝试重连
            for (int i = 1; i <= config.maxReconnectAttempts; i++) {
                log.info("尝试重新连接SSH隧道，第{}次尝试", i);
                try {
                    Thread.sleep(config.reconnectDelay);
                    establishSshTunnel();
                    break;
                } catch (Exception retryException) {
                    log.error("第{}次重连失败", i, retryException);
                    if (i == config.maxReconnectAttempts) {
                        throw new RuntimeException("无法建立SSH隧道连接", retryException);
                    }
                }
            }
        }
    }

    /**
     * 建立SSH隧道
     */
    private void establishSshTunnel() throws Exception {
        JSch jsch = new JSch();

        // 配置认证方式
        if (privateKeyPath != null && !privateKeyPath.isEmpty()) {
            // 使用私钥认证
            log.info("使用私钥认证: {}", privateKeyPath);
            if (passphrase != null && !passphrase.isEmpty()) {
                jsch.addIdentity(privateKeyPath, passphrase);
            } else {
                jsch.addIdentity(privateKeyPath);
            }
        }

        // 创建会话
        session = jsch.getSession(username, host, port);

        // 如果使用密码认证
        if (password != null && !password.isEmpty()) {
            session.setPassword(password);
        }

        // 配置会话属性
        Properties sessionConfig = new Properties();
        sessionConfig.put("StrictHostKeyChecking", config.strictHostKeyChecking);
        sessionConfig.put("PreferredAuthentications", "publickey,password");
        session.setConfig(sessionConfig);

        // 设置超时
        session.setServerAliveInterval(config.keepAliveInterval);
        session.setTimeout(config.connectionTimeout);

        // 连接
        log.info("正在连接SSH服务器: {}:{}", host, port);
        session.connect(config.connectionTimeout);

        // 设置端口转发
        int assignedPort = session.setPortForwardingL(
                database.localPort,
                database.remoteHost,
                database.remotePort
        );

        log.info("SSH隧道建立成功: localhost:{} -> {}:{} (通过 {}:{})",
                assignedPort,
                database.remoteHost,
                database.remotePort,
                host,
                port
        );
    }

    /**
     * 关闭SSH隧道
     */
    @PreDestroy
    public void destroy() {
        if (session != null && session.isConnected()) {
            try {
                session.delPortForwardingL(database.localPort);
                session.disconnect();
                log.info("SSH隧道已关闭");
            } catch (Exception e) {
                log.error("关闭SSH隧道时出错", e);
            }
        }
    }

    /**
     * 检查SSH隧道状态
     */
    public boolean isConnected() {
        return session != null && session.isConnected();
    }

    /**
     * 重新连接SSH隧道
     */
    public void reconnect() throws Exception {
        destroy();
        establishSshTunnel();
    }
}

/*
 * @Author yixuanmiao
 * @Date 2025/08/26 22:17
 */

package com.movk.common.config;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 确保数据源与 JPA 相关 Bean 在 SSH 隧道建立之后再初始化
 */
@Configuration
public class DataSourceDependsOnSshTunnel implements BeanFactoryPostProcessor {

    private static final String SSH_TUNNEL_BEAN_NAME = "sshTunnelConfig";
    private static final List<String> TARGET_BEAN_NAMES = Arrays.asList(
            "dataSource",
            "entityManagerFactory",
            "transactionManager"
    );

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        for (String beanName : TARGET_BEAN_NAMES) {
            if (!beanFactory.containsBeanDefinition(beanName)) {
                continue;
            }
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            String[] originalDependsOn = definition.getDependsOn();

            List<String> merged = new ArrayList<>();
            if (originalDependsOn != null && originalDependsOn.length > 0) {
                merged.addAll(Arrays.asList(originalDependsOn));
            }
            if (!merged.contains(SSH_TUNNEL_BEAN_NAME)) {
                merged.add(SSH_TUNNEL_BEAN_NAME);
            }
            definition.setDependsOn(merged.toArray(new String[0]));
        }
    }
}



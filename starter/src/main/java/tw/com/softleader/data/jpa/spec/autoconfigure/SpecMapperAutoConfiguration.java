/*
 * Copyright © 2022 SoftLeader
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package tw.com.softleader.data.jpa.spec.autoconfigure;

import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.support.RepositoryFactoryCustomizer;
import tw.com.softleader.data.jpa.spec.SpecMapper;
import tw.com.softleader.data.jpa.spec.SpecificationResolver;
import tw.com.softleader.data.jpa.spec.repository.support.JpaRepositoryFactoryBeanPostProcessor;
import tw.com.softleader.data.jpa.spec.repository.support.QueryBySpecExecutorImpl;

/**
 * @author Matt Ho
 */
@Slf4j
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SpecMapperProperties.class)
@ConditionalOnProperty(value = "spec.mapper.enabled", matchIfMissing = true)
public class SpecMapperAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  SpecMapper specMapper(List<SpecificationResolver> resolvers) {
    if (log.isTraceEnabled() && resolvers.isEmpty()) {
      log.trace("No SpecificationResolver declared");
    }
    if (log.isDebugEnabled()) {
      resolvers.forEach(resolver -> log.debug("Detected {}", resolver.getClass().getName()));
    }
    return SpecMapper.builder()
        .defaultResolvers()
        .resolvers(resolvers)
        .build();
  }

  @ConditionalOnBean(JpaRepositoryFactoryBean.class)
  static class RepositoryFactoryCustomizerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    JpaRepositoryFactoryBeanPostProcessor jpaRepositoryFactoryBeanPostProcessor(
        List<RepositoryFactoryCustomizer> customizers) {
      return new JpaRepositoryFactoryBeanPostProcessor(customizers);
    }

    @Bean
    RepositoryFactoryCustomizer repositoryBaseClassCustomizer(SpecMapperProperties properties) {
      log.debug("Configuring repository-base-class with '{}'",
          properties.getRepositoryBaseClass().getName());
      return factory -> factory.setRepositoryBaseClass(properties.getRepositoryBaseClass());
    }

    @Bean
    RepositoryFactoryCustomizer specMapperCustomizer(SpecMapper specMapper) {
      return factory -> factory.addRepositoryProxyPostProcessor(
          (proxyFactory, repositoryInformation) -> getQueryBySpecExecutorImpl(proxyFactory)
              .ifPresent(target -> target.setSpecMapper(specMapper)));
    }

    @SneakyThrows
    @SuppressWarnings({ "rawtypes" })
    private Optional<QueryBySpecExecutorImpl> getQueryBySpecExecutorImpl(ProxyFactory factory) {
      return ofNullable(factory.getTargetSource().getTarget())
          .filter(QueryBySpecExecutorImpl.class::isInstance)
          .map(QueryBySpecExecutorImpl.class::cast);
    }
  }
}

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
import static java.util.stream.Stream.concat;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
import tw.com.softleader.data.jpa.spec.SpecificationResolver.SpecificationResolverBuilder;
import tw.com.softleader.data.jpa.spec.SpecificationResolverCodecBuilder;
import tw.com.softleader.data.jpa.spec.repository.support.JpaRepositoryFactoryBeanPostProcessor;
import tw.com.softleader.data.jpa.spec.repository.support.QueryBySpecExecutorAdapter;

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
  SpecMapper specMapper(
      List<SpecificationResolver> resolvers,
      List<SpecificationResolverBuilder> builders,
      List<SpecificationResolverCodecBuilder> codecBuilders) {
    if (log.isTraceEnabled()) {
      if (resolvers.isEmpty()) {
        log.trace("No SpecificationResolver declared");
      }
      if (builders.isEmpty()) {
        log.trace("No SpecificationResolverBuilder declared");
      }
      if (codecBuilders.isEmpty()) {
        log.trace("No SpecificationResolverCodecBuilder declared");
      }
    }
    if (log.isDebugEnabled()) {
      concat(
          resolvers.stream(),
          concat(
              builders.stream(),
              codecBuilders.stream()))
                  .forEach(detected -> log.debug("Detected {}", detected.getClass().getName()));
    }
    val mapper = SpecMapper.builder()
        .defaultResolvers()
        .resolvers(resolvers);
    builders.forEach(builder -> mapper.resolver(builder::build));
    codecBuilders.forEach(mapper::resolver);
    return mapper.build();
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
          (proxyFactory, repositoryInformation) -> getQueryBySpecExecutorAdapter(proxyFactory)
              .ifPresent(target -> target.setSpecMapper(specMapper)));
    }

    @SneakyThrows
    @SuppressWarnings({ "rawtypes" })
    private Optional<QueryBySpecExecutorAdapter> getQueryBySpecExecutorAdapter(
        ProxyFactory factory) {
      return ofNullable(factory.getTargetSource().getTarget())
          .filter(QueryBySpecExecutorAdapter.class::isInstance)
          .map(QueryBySpecExecutorAdapter.class::cast);
    }
  }
}

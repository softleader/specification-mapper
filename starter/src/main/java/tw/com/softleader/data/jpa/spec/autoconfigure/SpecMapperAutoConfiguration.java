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
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;
import static org.springframework.util.Assert.notNull;
import static tw.com.softleader.data.jpa.spec.ASTWriterFactory.impersonation;
import static tw.com.softleader.data.jpa.spec.autoconfigure.SpecMapperProperties.PREFIX_SPEC_MAPPER;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.support.RepositoryFactoryCustomizer;
import tw.com.softleader.data.jpa.spec.*;
import tw.com.softleader.data.jpa.spec.SpecificationResolver.SpecificationResolverBuilder;
import tw.com.softleader.data.jpa.spec.repository.support.JpaRepositoryFactoryBeanPostProcessor;
import tw.com.softleader.data.jpa.spec.repository.support.QueryBySpecExecutorAdapter;

/**
 * @author Matt Ho
 */
@Slf4j
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SpecMapperProperties.class)
@ConditionalOnProperty(prefix = PREFIX_SPEC_MAPPER, value = "enabled", matchIfMissing = true)
public class SpecMapperAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  SpecMapper specMapper(
      ObjectProvider<SpecificationResolver> resolvers,
      ObjectProvider<SpecificationResolverBuilder> builders,
      ObjectProvider<SpecificationResolverCodecBuilder> codecBuilders,
      ObjectProvider<SkippingStrategy> skippingStrategy,
      ObjectProvider<ASTWriterFactory> astWriterFactory) {
    if (log.isTraceEnabled()) {
      if (!resolvers.iterator().hasNext()) {
        log.trace("No SpecificationResolver declared");
      }
      if (!builders.iterator().hasNext()) {
        log.trace("No SpecificationResolverBuilder declared");
      }
      if (!codecBuilders.iterator().hasNext()) {
        log.trace("No SpecificationResolverCodecBuilder declared");
      }
      if (!skippingStrategy.iterator().hasNext()) {
        log.trace("No SkippingStrategy declared");
      }
      if (!astWriterFactory.iterator().hasNext()) {
        log.trace("No WriterFactory declared");
      }
    }
    if (log.isDebugEnabled()) {
      concat(resolvers.orderedStream(), concat(builders.stream(), codecBuilders.stream()))
          .forEach(detected -> log.debug("Detected {}", detected.getClass().getName()));
    }
    var mapper = SpecMapper.builder().defaultResolvers();
    resolvers.orderedStream().forEach(mapper::resolver);
    builders.orderedStream().forEach(builder -> mapper.resolver(builder::build));
    codecBuilders.orderedStream().forEach(mapper::resolver);
    skippingStrategy.ifAvailable(mapper::skippingStrategy);
    astWriterFactory.ifAvailable(mapper::astWriterFactory);
    return mapper.build();
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(prefix = PREFIX_SPEC_MAPPER, value = "impersonate-logger")
  ASTWriterFactory impersonationASTWriterFactory() {
    return impersonation();
  }

  @Role(ROLE_INFRASTRUCTURE)
  @ConditionalOnBean(JpaRepositoryFactoryBean.class)
  static class RepositoryFactoryCustomizerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    JpaRepositoryFactoryBeanPostProcessor jpaRepositoryFactoryBeanPostProcessor(
        List<RepositoryFactoryCustomizer> customizers) {
      return new JpaRepositoryFactoryBeanPostProcessor(customizers);
    }

    @Bean
    @Role(ROLE_INFRASTRUCTURE)
    RepositoryFactoryCustomizer repositoryBaseClassCustomizer(
        ObjectProvider<SpecMapperProperties> propertiesProvider) {
      return factory -> {
        var properties = propertiesProvider.getIfAvailable();
        notNull(properties, "SpecMapperProperties must not be null");
        log.debug(
            "Configuring repository-base-class with '{}'",
            properties.getRepositoryBaseClass().getName());
        factory.setRepositoryBaseClass(properties.getRepositoryBaseClass());
      };
    }

    @Bean
    @Role(ROLE_INFRASTRUCTURE)
    RepositoryFactoryCustomizer specMapperCustomizer(
        ObjectProvider<SpecMapper> specMapperProvider) {
      return factory -> {
        var specMapper = specMapperProvider.getIfAvailable();
        notNull(specMapper, "SpecMapper must not be null");
        factory.addRepositoryProxyPostProcessor(
            (proxyFactory, repositoryInformation) ->
                getQueryBySpecExecutorAdapter(proxyFactory)
                    .ifPresent(target -> target.setSpecMapper(specMapper)));
      };
    }

    @SneakyThrows
    @SuppressWarnings({"rawtypes"})
    private Optional<QueryBySpecExecutorAdapter> getQueryBySpecExecutorAdapter(
        ProxyFactory factory) {
      return ofNullable(factory.getTargetSource().getTarget())
          .filter(QueryBySpecExecutorAdapter.class::isInstance)
          .map(QueryBySpecExecutorAdapter.class::cast);
    }
  }
}

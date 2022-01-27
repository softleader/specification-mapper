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
  SpecMapper specMapper() {
    return SpecMapper.builder().build();
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
      log.debug("Configuring repository-base-class to: {}",
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
    private Optional<QueryBySpecExecutorImpl> getQueryBySpecExecutorImpl(ProxyFactory factory) {
      return ofNullable(factory.getTargetSource().getTarget())
          .filter(QueryBySpecExecutorImpl.class::isInstance)
          .map(QueryBySpecExecutorImpl.class::cast);
    }
  }

}

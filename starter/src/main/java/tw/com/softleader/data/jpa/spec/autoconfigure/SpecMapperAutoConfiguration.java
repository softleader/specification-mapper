package tw.com.softleader.data.jpa.spec.autoconfigure;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.com.softleader.data.jpa.spec.SpecMapper;
import tw.com.softleader.data.jpa.spec.support.JpaRepositoryFactoryBeanPostProcessor;

/**
 * @author Matt Ho
 */
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

  @Bean
  @ConditionalOnMissingBean
  JpaRepositoryFactoryBeanPostProcessor jpaRepositoryFactoryBeanPostProcessor() {
    return new JpaRepositoryFactoryBeanPostProcessor();
  }
}

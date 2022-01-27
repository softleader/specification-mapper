package tw.com.softleader.data.jpa.spec.repository.support;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.support.RepositoryFactoryCustomizer;

/**
 * RepositoryFactoryCustomizer injector, which somehow Spring Data doesn't inject...
 *
 * @author Matt Ho
 * @see RepositoryFactoryCustomizer
 */
@RequiredArgsConstructor
public class JpaRepositoryFactoryBeanPostProcessor implements BeanPostProcessor {

  final List<RepositoryFactoryCustomizer> customizers;

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    if (bean instanceof JpaRepositoryFactoryBean) {
      var factoryBean = (JpaRepositoryFactoryBean) bean;
      customizers.forEach(factoryBean::addRepositoryFactoryCustomizer);
    }
    return bean;
  }
}

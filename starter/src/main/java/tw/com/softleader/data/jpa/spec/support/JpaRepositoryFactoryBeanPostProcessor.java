package tw.com.softleader.data.jpa.spec.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;

/**
 * @author Matt Ho
 */
public class JpaRepositoryFactoryBeanPostProcessor implements BeanPostProcessor {

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    if (bean instanceof JpaRepositoryFactoryBean) {
      // Spring somehow doesn't inject RepositoryFactoryCustomizer...
      ((JpaRepositoryFactoryBean) bean)
          .addRepositoryFactoryCustomizer(new QueryBySpecRepositoryFactoryCustomizer());
    }
    return bean;
  }
}

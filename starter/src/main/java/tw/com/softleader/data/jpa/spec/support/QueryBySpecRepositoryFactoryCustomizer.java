package tw.com.softleader.data.jpa.spec.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.core.support.RepositoryFactoryCustomizer;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import tw.com.softleader.data.jpa.spec.repository.support.QueryBySpecExecutorImpl;

/**
 * @author Matt Ho
 */
@Slf4j
public class QueryBySpecRepositoryFactoryCustomizer implements RepositoryFactoryCustomizer {

  @Override
  public void customize(RepositoryFactorySupport repositoryFactory) {
    log.debug("Configuring repository-base-class to [{}]",
        QueryBySpecExecutorImpl.class.getName());
    repositoryFactory.setRepositoryBaseClass(QueryBySpecExecutorImpl.class);
  }
}

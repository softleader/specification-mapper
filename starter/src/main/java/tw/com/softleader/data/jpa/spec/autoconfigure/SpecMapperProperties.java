package tw.com.softleader.data.jpa.spec.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import tw.com.softleader.data.jpa.spec.repository.support.QueryBySpecExecutorImpl;

/**
 * @author Matt Ho
 */
@Data
@ConfigurationProperties(prefix = "spec.mapper")
public class SpecMapperProperties {

  /**
   * Whether to enable the spec mapper
   */
  boolean enabled;

  /**
   * Configures the repository base class.
   */
  Class<? extends QueryBySpecExecutorImpl> repositoryBaseClass = QueryBySpecExecutorImpl.class;
}

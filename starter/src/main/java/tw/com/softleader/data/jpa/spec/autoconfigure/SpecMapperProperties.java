package tw.com.softleader.data.jpa.spec.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Matt Ho
 */
@Data
@ConfigurationProperties(prefix = "spec.mapper")
public class SpecMapperProperties {

  /**
   * Enabled
   */
  boolean enabled;
}

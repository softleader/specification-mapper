package tw.com.softleader.data.jpa.spec.domain;

import static java.util.Optional.ofNullable;

import lombok.Value;
import org.springframework.util.StringUtils;

/**
 * where-clause 的封裝, 讓 {@code SpecificationResolver} 的實作使用
 *
 * @author Matt Ho
 */
@Value
public class Expression {

  String path;
  Object value;

  public String competePath(String other) {
    return ofNullable(other).filter(StringUtils::hasText).orElse(path);
  }
}

package tw.com.softleader.data.jpa.spec.domain;

import static java.util.Arrays.stream;

import java.util.Objects;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import lombok.Builder;
import lombok.Value;

/**
 * 封裝多個欄位, 給 {@code Spec} 的實作使用
 *
 * @author Matt Ho
 */
@Value
@Builder
public class Metadata {

  String path;
  Object value;
  int order;
  Class<? extends Spec> specType;

  @SuppressWarnings("unchecked")
  <F> Path<F> path(Root root) {
    return (Path<F>) stream(path.split("\\."))
      .map(root::get)
      .filter(Objects::nonNull)
      .findFirst()
      .orElse(null);
  }
}

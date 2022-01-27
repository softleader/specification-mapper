package tw.com.softleader.data.jpa.spec.domain;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Metadata {

  String path;
  Object value;

  Path<?> path(Root<?> root) {
    Path<?> expr = null;
    for (String field : path.split("\\.")) {
      if (expr == null) {
        expr = root.get(field);
      } else {
        expr = expr.get(field);
      }
    }
    return expr;
  }
}

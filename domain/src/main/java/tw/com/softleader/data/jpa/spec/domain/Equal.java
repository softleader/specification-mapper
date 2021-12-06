package tw.com.softleader.data.jpa.spec.domain;

import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

/**
 * 實作 equal, e.g. {@code where name = "MyName"}
 *
 * @author Matt Ho
 */
public class Equal implements Spec {

  @Override
  public Specification<?> build(@NonNull Metadata metadata) {
    return (root, query, builder) -> builder.equal(metadata.path(root), metadata.getValue());
  }
}

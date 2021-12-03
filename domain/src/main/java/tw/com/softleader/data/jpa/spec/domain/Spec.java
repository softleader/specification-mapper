package tw.com.softleader.data.jpa.spec.domain;

import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author Matt Ho
 */
public interface Spec {

  Specification<?> build(@NonNull Metadata meta);
}

package tw.com.softleader.data.jpa.spec.domain;

import java.util.function.Function;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

/**
 * Share data between specifications
 *
 * @author Matt Ho
 */
public interface Context {

  Join<?, ?> getJoin(String key, Root<?> root);

  void putLazyJoin(String key, Function<Root<?>, Join<?, ?>> function);
}

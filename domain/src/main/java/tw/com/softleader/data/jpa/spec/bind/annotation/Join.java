package tw.com.softleader.data.jpa.spec.bind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.persistence.criteria.JoinType;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Join {

  /**
   * Specifies a collection property to join on, e.g. "addresses"
   */
  String path();

  /**
   * Specifies an alias for the joined part, e.g. "a"
   */
  String alias();

  /**
   * Whether the query should return distinct results or not
   */
  boolean distinct() default true;

  JoinType type() default JoinType.INNER;
}

package tw.com.softleader.data.jpa.spec.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.domain.Equals;
import tw.com.softleader.data.jpa.spec.domain.SimpleSpecification;

/**
 * @author Matt Ho
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Spec {

  /**
   * Name of the attribute to create {@code Path}
   *
   * @see Path
   */
  String path() default "";

  /**
   * {@code Specification} domain class
   */
  Class<? extends SimpleSpecification> value() default Equals.class;

  /**
   * Negates the {@code Specification}.
   *
   * @see Specification#not(Specification)
   */
  boolean not() default false;
}

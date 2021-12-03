package tw.com.softleader.data.jpa.spec.bind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import lombok.experimental.UtilityClass;

/**
 * @author Matt Ho
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Spec {

  String path() default "";

  Class<? extends tw.com.softleader.data.jpa.spec.domain.Spec> spec();

  int order() default Ordered.LOWEST_PRECEDENCE;

  @UtilityClass
  class Ordered {

    public static final int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;
    public static final int LOWEST_PRECEDENCE = Integer.MAX_VALUE;
  }
}

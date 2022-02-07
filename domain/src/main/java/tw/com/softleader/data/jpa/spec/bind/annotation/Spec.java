package tw.com.softleader.data.jpa.spec.bind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import tw.com.softleader.data.jpa.spec.bind.Compose;

/**
 * @author Matt Ho
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Spec {

  String path() default "";

  Class<? extends tw.com.softleader.data.jpa.spec.domain.Spec> spec();

  Compose compose() default Compose.AND;
}

package tw.com.softleader.data.jpa.spec.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import tw.com.softleader.data.jpa.spec.domain.Equal;
import tw.com.softleader.data.jpa.spec.domain.PathSpecification;
import tw.com.softleader.data.jpa.spec.domain.SimpleSpecification;

/**
 * @author Matt Ho
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Spec {

  String path() default "";

  Class<? extends SimpleSpecification> value() default Equal.class;
}

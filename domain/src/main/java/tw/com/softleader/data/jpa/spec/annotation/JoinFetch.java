package tw.com.softleader.data.jpa.spec.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.persistence.criteria.JoinType;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface JoinFetch {

  String[] paths();

  JoinType joinType() default JoinType.LEFT;

  boolean distinct() default true;

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.TYPE })
  @interface JoinFetches {

    JoinFetch[] value();
  }
}

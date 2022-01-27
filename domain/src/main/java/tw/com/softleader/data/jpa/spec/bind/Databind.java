package tw.com.softleader.data.jpa.spec.bind;

import static lombok.AccessLevel.PACKAGE;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;
import tw.com.softleader.data.jpa.spec.bind.annotation.Or;
import tw.com.softleader.data.jpa.spec.bind.annotation.Spec;
import tw.com.softleader.data.jpa.spec.bind.annotation.Spec.Ordered;
import tw.com.softleader.data.jpa.spec.domain.Equal;
import tw.com.softleader.data.jpa.spec.domain.Metadata;

/**
 * 這隻程式負責綁定 object field 跟 spec
 *
 * @author Matt Ho
 */
@Setter
@Accessors(chain = true)
@NoArgsConstructor(access = PACKAGE)
public class Databind implements Comparable<Databind> {

  @Getter
  int order;
  Metadata metadata;
  Class<? extends tw.com.softleader.data.jpa.spec.domain.Spec> specType;
  @Getter
  SpecificationResolver resolver;

  @SneakyThrows
  public static Databind of(@NonNull Object obj, @NonNull Field field) {
    var databind = new Databind();
    var spec = field.getAnnotation(Spec.class);
    if (spec == null) {
      databind
          .setOrder(Ordered.LOWEST_PRECEDENCE)
          .setMetadata(Metadata.builder()
              .path(field.getName())
              .value(
                  new PropertyDescriptor(field.getName(), obj.getClass()).getReadMethod().invoke(obj))
              .build())
          .setSpecType(Equal.class);
    } else {
      databind
          .setOrder(spec.order())
          .setMetadata(Metadata.builder()
              .path(Optional.of(spec.path()).filter(StringUtils::hasText).orElseGet(field::getName))
              .value(
                  new PropertyDescriptor(field.getName(), obj.getClass()).getReadMethod().invoke(obj))
              .build())
          .setSpecType(spec.spec());
    }
    if (field.isAnnotationPresent(Or.class)) {
      databind.setResolver(new OrSpecificationResolver(
          databind.metadata,
          databind.specType));
    } else {
      databind.setResolver(new AndSpecificationResolver(
          databind.metadata,
          databind.specType));
    }
    return databind;
  }

  @Override
  public int compareTo(Databind other) {
    return Comparator
        .comparing(Databind::getOrder)
        .compare(this, other);
  }
}

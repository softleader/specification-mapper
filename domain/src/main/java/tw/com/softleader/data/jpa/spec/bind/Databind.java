package tw.com.softleader.data.jpa.spec.bind;

import static lombok.AccessLevel.PRIVATE;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Comparator;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.bind.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Metadata;
import tw.com.softleader.data.jpa.spec.domain.Spec.Factory;

/**
 * 這隻程式負責綁定 object field 跟 spec
 *
 * @author Matt Ho
 */
@Value
@AllArgsConstructor(access = PRIVATE)
public class Databind implements Comparable<Databind> {

  final static Factory SPEC_FACTORY = new Factory();

  int order;
  Specification spec;

  @SneakyThrows
  public Databind(Object obj, Field field) {
    var spec = field.getAnnotation(Spec.class);
    this.order = spec.order();
    var metadata = Metadata.builder()
        .path(spec.path())
        .value(new PropertyDescriptor(field.getName(), obj.getClass()).getReadMethod().invoke(obj))
        .build();
    this.spec = SPEC_FACTORY.create(spec.spec()).build(metadata);
  }

  public Databind reduce(Databind other) {
    return new Databind(0, spec.and(other.spec));
  }

  @Override
  public int compareTo(Databind other) {
    return Comparator
        .comparing(Databind::getOrder)
        .compare(this, other);
  }
}

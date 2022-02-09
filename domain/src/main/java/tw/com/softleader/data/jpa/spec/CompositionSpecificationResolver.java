package tw.com.softleader.data.jpa.spec;

import static java.util.Optional.of;
import static java.util.function.Predicate.not;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import tw.com.softleader.data.jpa.spec.annotation.And;
import tw.com.softleader.data.jpa.spec.annotation.Or;
import tw.com.softleader.data.jpa.spec.domain.Conjunction;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.domain.Disjunction;
import tw.com.softleader.data.jpa.spec.util.FieldUtil;

/**
 * @author Matt Ho
 */
@Slf4j
@RequiredArgsConstructor
class CompositionSpecificationResolver implements SpecificationResolver {

  final SpecCodec codec;

  @Override
  public boolean supports(@NonNull Object obj, @NonNull Field field) {
    return field.isAnnotationPresent(Or.class)
        || field.isAnnotationPresent(And.class);
  }

  @Override
  public Specification<Object> buildSpecification(@NonNull Context context, @NonNull Object obj,
      @NonNull Field field) {
    var nested = FieldUtil.getValue(obj, field);
    if (nested == null) {
      return null;
    }
    log.debug(" -> Looping fields through nested object [{}.{}] ({})",
        obj.getClass().getSimpleName(),
        field.getName(),
        field.getType());
    var spec = of(codec.collectSpecs(context, nested))
        .filter(not(Collection::isEmpty))
        .map(newDomain(field)::apply)
        .orElse(null);
    log.debug(" <- Composed specification from [{}.{}]: {}",
        obj.getClass().getSimpleName(),
        field.getName(),
        spec);
    return spec;
  }

  <T> Function<Collection<Specification<T>>, Specification<T>> newDomain(Field field) {
    return field.isAnnotationPresent(Or.class) ? Disjunction::new : Conjunction::new;
  }
}

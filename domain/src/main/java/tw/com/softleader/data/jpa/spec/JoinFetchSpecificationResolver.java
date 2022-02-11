package tw.com.softleader.data.jpa.spec;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.util.Objects;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import tw.com.softleader.data.jpa.spec.annotation.JoinFetch;
import tw.com.softleader.data.jpa.spec.annotation.JoinFetch.JoinFetches;
import tw.com.softleader.data.jpa.spec.domain.Conjunction;
import tw.com.softleader.data.jpa.spec.domain.Context;

/**
 * @author Matt Ho
 */
@Slf4j
class JoinFetchSpecificationResolver implements SpecificationResolver {

  @Override
  public boolean supports(@NonNull Databind databind) {
    return databind.getTarget().getClass().isAnnotationPresent(JoinFetch.class)
        || databind.getTarget().getClass().isAnnotationPresent(JoinFetches.class);
  }

  @Override
  public Specification<Object> buildSpecification(@NonNull Context context,
      @NonNull Databind databind) {
    var specs = Stream.concat(
        joinFetchDef(context, databind.getTarget()),
        joinFetchesDef(context, databind.getTarget())).filter(Objects::nonNull)
        .collect(toList());
    if (specs.size() == 1) {
      return specs.get(0);
    }
    return new Conjunction<>(specs);
  }

  private Stream<Specification<Object>> joinFetchesDef(Context context, Object obj) {
    if (!obj.getClass().isAnnotationPresent(JoinFetches.class)) {
      return Stream.empty();
    }
    return stream(obj.getClass().getAnnotation(JoinFetches.class).value())
        .map(def -> newJoinFetch(context, def));
  }

  private Stream<Specification<Object>> joinFetchDef(Context context, Object obj) {
    if (!obj.getClass().isAnnotationPresent(JoinFetch.class)) {
      return Stream.empty();
    }
    return Stream.of(
        newJoinFetch(context, obj.getClass().getAnnotation(JoinFetch.class)));
  }

  Specification<Object> newJoinFetch(@NonNull Context context, @NonNull JoinFetch def) {
    return new tw.com.softleader.data.jpa.spec.domain.JoinFetch<>(
        context,
        def.paths(),
        def.joinType(),
        def.distinct());
  }
}

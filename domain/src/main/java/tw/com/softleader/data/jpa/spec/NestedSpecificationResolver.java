package tw.com.softleader.data.jpa.spec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import tw.com.softleader.data.jpa.spec.annotation.NestedSpec;
import tw.com.softleader.data.jpa.spec.domain.Context;

/**
 * @author Matt Ho
 */
@Slf4j
@RequiredArgsConstructor
class NestedSpecificationResolver implements SpecificationResolver {

  final SpecCodec codec;

  @Override
  public boolean supports(@NonNull Databind databind) {
    return databind.getField().isAnnotationPresent(NestedSpec.class);
  }

  @Override
  public Specification<Object> buildSpecification(@NonNull Context context,
      @NonNull Databind databind) {
    return databind.getFieldValue()
        .map(nested -> {
          log.debug(" -> Looping fields through nested object [{}.{}] ({})",
              databind.getTarget().getClass().getSimpleName(),
              databind.getField().getName(),
              databind.getField().getType());
          var spec = codec.toSpec(context, nested);
          log.debug(" <- Composed specification from [{}.{}]: {}",
              databind.getTarget().getClass().getSimpleName(),
              databind.getField().getName(),
              spec);
          var def = databind.getField().getAnnotation(NestedSpec.class);
          return def.combineType().apply(spec);
        })
        .orElse(null);
  }
}

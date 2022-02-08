package tw.com.softleader.data.jpa.spec;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.util.FieldUtil;

/**
 * @author Matt Ho
 */
@Slf4j
public class SpecMapper implements SpecCodec {

  final Collection<SpecificationResolver> resolvers; // 順序是重要的

  @Builder
  private SpecMapper(@Singular Collection<SpecificationResolver> resolvers) {
    if (resolvers.isEmpty()) {
      resolvers = List.of(
          new CompositionSpecificationResolver(this),
          new JoinFetchSpecificationResolver(),
          new JoinSpecificationResolver(),
          new SimpleSpecificationResolver());
    }
    this.resolvers = resolvers;
  }

  @Override
  public Collection<Specification<Object>> collectSpecs(@NonNull Context context,
      @NonNull Object rootObject) {
    return FieldUtil.fieldStream(rootObject)
        .flatMap(field -> resolveSpec(context, rootObject, field))
        .filter(Objects::nonNull)
        .collect(toList());
  }

  Stream<Specification<Object>> resolveSpec(@NonNull Context context, @NonNull Object obj,
      @NonNull Field field) {
    return resolvers.stream()
        .filter(resolver -> resolver.supports(obj, field))
        .map(resolver -> resolver.buildSpecification(context, obj, field));
  }
}

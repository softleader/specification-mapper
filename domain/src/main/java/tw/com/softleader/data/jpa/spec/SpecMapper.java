package tw.com.softleader.data.jpa.spec;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.util.FieldUtil;

/**
 * @author Matt Ho
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class SpecMapper implements SpecCodec {

  private Collection<SpecificationResolver> resolvers; // 順序是重要的

  public static SpecMapperBuilder builder() {
    return new SpecMapperBuilder();
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

  @NoArgsConstructor(access = AccessLevel.PACKAGE)
  public static class SpecMapperBuilder {

    private final Collection<Function<SpecCodec, SpecificationResolver>> resolvers = new LinkedList<>();

    public SpecMapperBuilder resolver(
        @NonNull Function<SpecCodec, SpecificationResolver> resolver) {
      this.resolvers.add(resolver);
      return this;
    }

    public SpecMapperBuilder resolver(@NonNull Supplier<SpecificationResolver> resolver) {
      return resolver(codec -> resolver.get());
    }

    public SpecMapperBuilder resolver(@NonNull SpecificationResolver resolver) {
      return resolver(codec -> resolver);
    }

    public SpecMapperBuilder defaultResolvers() {
      return resolver(CompositionSpecificationResolver::new)
          .resolver(JoinFetchSpecificationResolver::new)
          .resolver(JoinSpecificationResolver::new)
          .resolver(SimpleSpecificationResolver::new);
    }

    public SpecMapper build() {
      if (this.resolvers.isEmpty()) {
        throw new IllegalStateException("At least one resolver must be provided.");
      }
      var mapper = new SpecMapper();
      mapper.resolvers = this.resolvers.stream()
          .map(resolver -> resolver.apply(mapper))
          .collect(toUnmodifiableList());
      return mapper;
    }
  }
}

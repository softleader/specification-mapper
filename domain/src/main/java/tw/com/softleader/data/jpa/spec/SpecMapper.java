package tw.com.softleader.data.jpa.spec;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;
import static lombok.AccessLevel.PACKAGE;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import tw.com.softleader.data.jpa.spec.domain.Context;

/**
 * @author Matt Ho
 */
@Slf4j
@NoArgsConstructor(access = PACKAGE)
public class SpecMapper implements SpecCodec {

  private Collection<SpecificationResolver> resolvers; // Order matters

  public static SpecMapperBuilder builder() {
    return new SpecMapperBuilder();
  }

  @Override
  public Collection<Specification<Object>> collectSpecs(@NonNull Context context,
      @Nullable Object rootObject) {
    return ReflectionDatabind.of(rootObject)
        .stream()
        .flatMap(databind -> resolveSpec(context, databind))
        .filter(Objects::nonNull)
        .collect(toList());
  }

  Stream<Specification<Object>> resolveSpec(@NonNull Context context, @NonNull Databind databind) {
    return resolvers.stream()
        .filter(resolver -> resolver.supports(databind))
        .map(resolver -> resolver.buildSpecification(context, databind));
  }

  @NoArgsConstructor(access = PACKAGE)
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

    public SpecMapperBuilder defaultResolvers() { // 順序是重要的, ex: Join 需要比 Simple 還早
      return resolver(CompositionSpecificationResolver::new)
          .resolver(JoinFetchSpecificationResolver::new)
          .resolver(JoinSpecificationResolver::new)
          .resolver(SimpleSpecificationResolver::new);
    }

    public SpecMapper build() {
      if (this.resolvers.isEmpty()) {
        defaultResolvers();
      }
      var mapper = new SpecMapper();
      mapper.resolvers = this.resolvers.stream()
          .map(resolver -> resolver.apply(mapper))
          .collect(toUnmodifiableList());
      return mapper;
    }
  }
}

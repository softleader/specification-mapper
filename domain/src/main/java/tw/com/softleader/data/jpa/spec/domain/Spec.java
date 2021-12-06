package tw.com.softleader.data.jpa.spec.domain;

import static java.util.Optional.ofNullable;

import io.github.classgraph.ClassGraph;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author Matt Ho
 */
public interface Spec {

  Specification<?> build(@NonNull Metadata metadata);

  class Factory {

    final Map<Class<Spec>, Spec> specs;

    // 載入在找此 package 當下實作 Spec 的實作
    public Factory() {
      try (var scanned = new ClassGraph()
          .enableClassInfo()
          .acceptPackages(Spec.class.getPackageName())
          .scan()) {
        this.specs = scanned.getClassesImplementing(Spec.class).loadClasses(Spec.class)
            .stream()
            .collect(Collectors.toMap(
                Function.identity(),
                this::createInstance));
      }
    }

    public Spec create(@NonNull Class<? extends Spec> type) {
      return ofNullable(specs.get(type))
          .orElseThrow(
              () -> new UnsupportedOperationException(
                  "Unsupported Spec type: " + type)); // Should not reach here
    }

    @SneakyThrows
    Spec createInstance(@NonNull Class<? extends Spec> type) {
      return type.getDeclaredConstructor().newInstance();
    }
  }

}

package tw.com.softleader.data.jpa.spec;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import lombok.Synchronized;
import org.springframework.data.util.Pair;
import tw.com.softleader.data.jpa.spec.domain.Context;

class SpecContext implements Context {

  private final Map<Pair<String, Root<?>>, javax.persistence.criteria.Join<?, ?>> joins = new HashMap<>();
  private final Map<String, Function<Root<?>, Join<?, ?>>> lazyJoins = new HashMap<>();

  @Override
  @Synchronized
  public Join<?, ?> getJoin(String key, Root<?> root) {
    var lazyJoin = lazyJoins.get(key);
    if (lazyJoin == null) {
      return null;
    }
    Pair<String, Root<?>> rootKey = Pair.of(key, root);
    joins.computeIfAbsent(rootKey, k -> lazyJoin.apply(root));
    return joins.get(rootKey);
  }

  public void putLazyJoin(String key, Function<Root<?>, Join<?, ?>> lazyJoin) {
    lazyJoins.put(key, lazyJoin);
  }
}

package tw.com.softleader.data.jpa.spec.domain;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author Matt Ho
 */
@RequiredArgsConstructor
public class Join<T> implements Specification<T> {

  @NonNull
  private final Context context;
  @NonNull
  private final String pathToJoinOn;
  @NonNull
  private final String alias;
  @NonNull
  private final JoinType joinType;
  private final boolean distinct;

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
    query.distinct(distinct);
    join(root);
    return null;
  }

  private void join(Root<T> root) {
    if (!pathToJoinOn.contains("\\.")) {
      context.putLazyJoin(alias, r -> r.join(pathToJoinOn, joinType));
      return;
    }
    var byDot = pathToJoinOn.split("\\.");

    var alias = byDot[0];
    var join = context.getJoin(alias, root);

    if (join == null) {
      throw new IllegalArgumentException(
          "Join definition with alias: '" + alias + "' not found! " +
              "Make sure that join with the alias '" + alias
              + "' is defined before the join with path: '" + pathToJoinOn + "'");
    }

    var path = byDot[1];
    context.putLazyJoin(alias, r -> join.join(path, joinType));
  }
}

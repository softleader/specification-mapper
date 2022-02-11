package tw.com.softleader.data.jpa.spec.domain;

import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author Matt Ho
 */
public class JoinFetch<T> implements Specification<T> {

  private final List<String> pathsToFetch;
  private final JoinType joinType;
  private final boolean distinct;

  public JoinFetch(
      @NonNull String[] pathsToFetch,
      @NonNull JoinType joinType,
      boolean distinct) {
    this.pathsToFetch = List.of(pathsToFetch);
    this.joinType = joinType;
    this.distinct = distinct;

    if (pathsToFetch.length == 0) {
      throw new IllegalArgumentException("paths must not be empty");
    }
  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
    query.distinct(distinct);
    if (!Number.class.isAssignableFrom(query.getResultType())) { // do not join in count queries
      fetchJoin(root);
    }
    return null;
  }

  private void fetchJoin(Root<T> root) {
    if (pathsToFetch.size() > 1) {
      for (String path : pathsToFetch) {
        root.fetch(path, joinType);
      }
      return;
    }
    var pathToFetch = pathsToFetch.get(0);
    if (!pathToFetch.contains(".")) {
      root.fetch(pathToFetch, joinType);
      return;
    }
    var byDot = pathToFetch.split("\\.");
    var alias = byDot[0];
    var path = byDot[1];
    root.fetch(alias).fetch(path, joinType);
  }
}

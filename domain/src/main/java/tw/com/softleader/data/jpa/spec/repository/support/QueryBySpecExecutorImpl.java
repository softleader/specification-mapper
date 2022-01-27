package tw.com.softleader.data.jpa.spec.repository.support;

import java.io.Serializable;
import javax.persistence.EntityManager;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import tw.com.softleader.data.jpa.spec.SpecMapper;

/**
 * {@code QueryBySpecExecutor} 的預設實作
 *
 * @author Matt Ho
 */
public class QueryBySpecExecutorImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
    implements QueryBySpecExecutorAdapter<T> {

  @Getter
  final SpecMapper specMapper = SpecMapper.builder().build();

  public QueryBySpecExecutorImpl(
      @NonNull JpaEntityInformation<T, ?> entityInformation,
      @NonNull EntityManager entityManager) {
    super(entityInformation, entityManager);
  }

  @Override
  public Class<T> getGenericType() {
    return getDomainClass();
  }
}

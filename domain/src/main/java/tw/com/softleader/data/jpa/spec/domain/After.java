package tw.com.softleader.data.jpa.spec.domain;

import lombok.NonNull;

/**
 * {@code ... where x.age > ?}
 *
 * @author Matt Ho
 */
public class After<T> extends GreaterThan<T> {

  public After(@NonNull Context context, @NonNull String path,
      @NonNull Object value) {
    super(context, path, value);
  }
}

package tw.com.softleader.data.jpa.spec.domain;

import lombok.NonNull;

/**
 * {@code ... where x.firstname < ?}
 *
 * @author Matt Ho
 */
public class Before<T> extends LessThan<T> {

  public Before(@NonNull Context context, @NonNull String path,
      @NonNull Object value) {
    super(context, path, value);
  }
}

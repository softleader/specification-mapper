package tw.com.softleader.data.jpa.spec.domain;

import lombok.NonNull;

/**
 * @author Matt Ho
 */
public class EndingWith<T> extends Like<T> {

  public EndingWith(@NonNull Context context, @NonNull String path, @NonNull Object value) {
    super(context, path, value);
    this.value = "%" + value;
  }
}

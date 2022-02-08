package tw.com.softleader.data.jpa.spec.domain;

import lombok.NonNull;

/**
 * @author Matt Ho
 */
public class StartingWith<T> extends Like<T> {

  public StartingWith(@NonNull Context context, @NonNull String path, @NonNull Object value) {
    super(context, path, value);
    this.value = value + "%";
  }
}

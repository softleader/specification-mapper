package tw.com.softleader.data.jpa.spec;

import java.lang.reflect.Field;
import java.util.Optional;
import org.springframework.lang.NonNull;

/**
 * Databind 抽象了每個物件中的欄位, 讓怎麼取得欄位的方式有機會抽換成效能更好的實作
 *
 * @author Matt Ho
 */
public interface Databind {

  /**
   * 欄位所在的物件
   */
  @NonNull
  Object getTarget();

  /**
   * 欄位
   */
  @NonNull
  Field getField();

  /**
   * 欄位值
   */
  Optional<Object> getFieldValue();
}

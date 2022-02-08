package tw.com.softleader.data.jpa.spec;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import tw.com.softleader.data.jpa.spec.IntegrationTest.TestApplication;

/**
 * 會啟動 Spring Boot Data JPA 以及 H2 的整合測試
 *
 * @author Matt Ho
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureDataJpa
public @interface IntegrationTest {

  /**
   * Main entrypoint for run Spring-boot in test
   *
   * @author Matt Ho
   */
  @EnableJpaRepositories
  @SpringBootApplication
  class TestApplication {

    public static void main(String[] args) {
      SpringApplication.run(TestApplication.class, args);
    }
  }
}

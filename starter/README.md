# specification-mapper-starter

```xml
<dependency>
  <groupId>tw.com.softleader.data</groupId>
  <artifactId>specification-mapper-starter</artifactId>
</dependency>
```

specification-mapper-starter 整合了 [specification-mapper](../domain) 及 [Spring Data JPA](https://spring.io/projects/spring-data-jpa), 主要提供了 Query by Spec 的查詢方式

Query by Spec (QBS) 是一個  user-friendly 的查詢方式, 可以動態的建立查詢條件 ([Specifications](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications)), 並透過 QBS interface 就可以執行查詢語句!

## Getting Started

只要在 `pom.xml` 中加入 dependency, Spring Boot 在啟動時就會自動的配置, 透過 properties 中的 `spec.mapper.enabled` 可以控制開關, 預設是開啟的, 如要關閉則:

```yaml
spec:
  mapper:
    enabled: false
```

## Query by Spec using a Repository

QBS 提供了 [`QueryBySpecExecutor<T>`](./src/main/java/tw/com/softleader/data/jpa/spec/repository/QueryBySpecExecutor.java) 包含了許多查詢方法:

```java
public interface QueryBySpecExecutor<T> {

  List<T> findBySpec(Object spec);
  
  List<T> findBySpec(Object spec, Sort sort);
  
  Page<T> findBySpec(Object spec, Pageable pageable);
  
  // … more functionality omitted.
}
```

只要在原本的 repository interface 中去繼承 `QueryBySpecExecutor<T>` 就可以直接使用了:

```java
public interface PersonRepository extends
  JpaRepository<Person, Long>, QueryBySpecExecutor<Person> {
  ...
}

public class PersonService {

  @Autowired PersonRepository personRepository;

  public List<Person> findPeople(PersonCriteria criteria) {
    return personRepository.findBySpec(criteria);
  }
}
```

## Customize the QBS Base Repository

在配置的過程中, QBS 會自動配置 Spring Data JPA 的 [Base Repository](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.customize-base-repository), 預設的實作為 [`QueryBySpecExecutorImpl`](./src/main/java/tw/com/softleader/data/jpa/spec/repository/support/QueryBySpecExecutorImpl.java)

如果你的應用有自己的 Base Repository, 則該 Base Repository 必須實作 `QueryBySpecExecutorImpl`, 如:

```java
class MyRepositoryImpl<T, ID>
  extends QueryBySpecExecutorImpl<T, ID> {

  private final EntityManager entityManager;

  MyRepositoryImpl(JpaEntityInformation entityInformation,
                          EntityManager entityManager) {
    super(entityInformation, entityManager);

    // Keep the EntityManager around to used from the newly introduced methods.
    this.entityManager = entityManager;
  }

  @Transactional
  public <S extends T> S mySave(S entity) {
    // implementation goes here
  }
}
```

並且透過 properties 中的 `spec.mapper.repository-base-class` 設定成自定義的 base repository 的 fulll package name, 如:

```yaml
spec:
  mapper:
    repository-base-class: com.acme.example.MyRepositoryImpl
```

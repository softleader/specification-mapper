---
title: Query by Spec
weight: 10
---

Query by Spec (QBS) 提供了 `QueryBySpecExecutor<T>` 包含了許多查詢方法:

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
public interface PersonRepository 
  extends JpaRepository<Person, Long>, QueryBySpecExecutor<Person> {
  ...
}

@Service
public class PersonService {

  @Autowired PersonRepository personRepository;

  public List<Person> findPeople(PersonCriteria criteria) {
    return personRepository.findBySpec(criteria);
  }
}
```

## Customize Base Repository

在配置的過程中, QBS 會自動配置 Spring Data JPA 的 [Base Repository](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.customize-base-repository), 預設的實作為 `DefaultQueryBySpecExecutor`

由於 Java 只能單一繼承, 為了應用程式可以保留原有的 Parent Base Repository, QBS 還多提供了 `QueryBySpecExecutorAdapter` 擴展點

你的應用程式可以視情況選擇繼承 `DefaultQueryBySpecExecutor` 或實作 `QueryBySpecExecutorAdapter` 去客製化 Base Repository, 如:

```java
class MyJpaRepository<T, ID> extends SimpleJpaRepository<T, ID>
  implements QueryBySpecExecutorAdapter<T> {

  @Setter
  @Getter
  private SpecMapper specMapper;

  private final EntityManager entityManager;

  MyJpaRepository(JpaEntityInformation entityInformation,
                          EntityManager entityManager) {
    super(entityInformation, entityManager);

    // Keep the EntityManager around to used from the newly introduced methods.
    this.entityManager = entityManager;
  }

  @Override
  public Class<T> getDomainClass() {
    return super.getDomainClass();
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
    repository-base-class: com.example.MyJpaRepository
```

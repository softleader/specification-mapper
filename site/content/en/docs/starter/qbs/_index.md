---
title: Query by Spec
weight: 10
description: > 
  Introduction and usage of Query by Spec (QBS)
---

Query by Spec (QBS) provides the `QueryBySpecExecutor<T>` interface, which includes several query methods:

```java
public interface QueryBySpecExecutor<T> {

  List<T> findBySpec(Object spec);
  
  List<T> findBySpec(Object spec, Sort sort);
  
  Page<T> findBySpec(Object spec, Pageable pageable);
  
  // … more functionality omitted.
}
```

To use these methods, you simply need to extend `QueryBySpecExecutor<T>` in your existing repository interface:

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

By inheriting `QueryBySpecExecutor<T>`, you can directly use the query methods in your repository interface, making it easy to perform queries using specifications.

## Customize Base Repository

During the configuration process, QBS automatically configures the Spring Data JPA [Base Repository](https://docs.spring.io/spring-data/jpa/reference/repositories/custom-implementations.html#repositories.customize-base-repository). The default implementation is `DefaultQueryBySpecExecutor`.

However, since Java only supports single inheritance, and to allow your application to retain its original parent Base Repository, QBS provides an extension point called `QueryBySpecExecutorAdapter`.

Depending on your application's needs, you can choose to either extend `DefaultQueryBySpecExecutor` or implement `QueryBySpecExecutorAdapter` to customize the Base Repository. For example:

```java
class MyRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID>
  implements QueryBySpecExecutorAdapter<T> {

  @Setter
  @Getter
  private SpecMapper specMapper;

  private final EntityManager entityManager;

  MyRepositoryImpl(JpaEntityInformation entityInformation,
                          EntityManager entityManager) {
    super(entityInformation, entityManager);

    // Keep the EntityManager around to be used from the newly introduced methods.
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

You can configure your custom Base Repository by setting the `spec.mapper.repository-base-class` property in your application's properties file, specifying the full package name of your custom base repository, like this:

```yaml
spec:
  mapper:
    repository-base-class: com.example.MyRepositoryImpl
```

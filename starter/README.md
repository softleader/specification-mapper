[中文版](./README.tw-zh.md)

# specification-mapper-starter

```xml
<dependency>
  <groupId>tw.com.softleader.data.jakarta</groupId>
  <artifactId>specification-mapper-starter</artifactId>
  <version>last-release-version</version>
</dependency>
```

The `specification-mapper-starter` integrates [specification-mapper](../mapper) with [Spring Data JPA](https://spring.io/projects/spring-data-jpa) and provides a way to query by specifications.

Query by Spec (QBS) is a user-friendly querying approach that allows you to dynamically build query conditions using specifications. With the QBS interface, you can execute query statements easily.

## Getting Started

By adding the dependency in your `pom.xml` file, the `specification-mapper-starter` will automatically configure everything during the Spring Boot startup process, allowing you to start using it without any additional configuration. The starter includes the following features:

- Configuration for [Query By Spec](#query-by-spec)
- Registration of the default [`SpecMapper`](#default-specmapper)

The auto-configuration is enabled by default, and you can control it through the `spec.mapper.enabled` property in your application's properties file. To disable the auto-configuration, you can use the following configuration:

```yaml
spec:
  mapper:
    enabled: false
```

## Query by Spec

Query by Spec (QBS) provides the [`QueryBySpecExecutor<T>`](./src/main/java/tw/com/softleader/data/jpa/spec/repository/QueryBySpecExecutor.java) interface, which includes several query methods:

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

### Customize the QBS Base Repository

During the configuration process, QBS automatically configures the Spring Data JPA [Base Repository](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.customize-base-repository). The default implementation is [`QueryBySpecExecutorImpl`](./src/main/java/tw/com/softleader/data/jpa/spec/repository/support/QueryBySpecExecutorImpl.java).

However, since Java only supports single inheritance, and to allow your application to retain its original parent Base Repository, QBS provides an extension point called [`QueryBySpecExecutorAdapter`](./src/main/java/tw/com/softleader/data/jpa/spec/repository/support/QueryBySpecExecutorAdapter.java).

Depending on your application's needs, you can choose to either extend `QueryBySpecExecutorImpl` or implement `QueryBySpecExecutorAdapter` to customize the Base Repository. For example:

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
    repository-base-class: com.acme.example.MyRepositoryImpl
```

## Default SpecMapper

This starter automatically configures a *Default SpecMapper* during the application startup process and registers it as a Spring `@Bean`. You can obtain an instance of the SpecMapper through dependency injection (`@Autowired`) in your application.

For example, if you want to enhance the specifications before performing the query, you can use the SpecMapper as follows:

```java
class PersonService {

  @Autowired SpecMapper specMapper;
  @Autowired PersonRepository personRepository;

  List<Person> getPersonByCriteria(PersonCriteria criteria) {
    var spec = specMapper.toSpec(criteria);
    
    // Perform additional operations on the spec
    
    return personRepository.findAll(spec);
  }
}
```

In the above example, the SpecMapper is injected into the `PersonService`, allowing you to convert the criteria into a specification using `specMapper.toSpec()`. You can then modify the spec as needed before passing it to the `personRepository` for querying.

### Customize SpecificationResolver

By registering your custom `SpecificationResolver` as a Spring `@Bean`, it will be automatically detected and added to the *Default SpecMapper* during the application startup process.

For example, if you want to add your custom spec annotation as mentioned in [Customize Spec Annotation](../mapper#customize-spec-annotation), you can configure it as follows:

```java
@Configuration
class MyConfig {

  @Bean
  SpecificationResolver myResolver() {
    return ...
  }
}
```

If your `SpecificationResolver` needs access to the `SpecMapper` itself, you can wrap it in a `SpecificationResolverCodecBuilder`. This way, the `SpecCodec`, which is the interface of `SpecMapper`, will be passed in when constructing the resolver. Here's an example:

```java
@Configuration
class MyConfig {

  @Bean
  SpecificationResolverCodecBuilder myResolver() {
    return MySpecificationResolver::new;
  }
}

class MySpecificationResolver implements SpecificationResolver {
  
  private final SpecCodec codec;
  
  MySpecificationResolver(SpecCodec codec) {
    // Keep the SpecCodec around to be used.
    this.codec = codec;
  }
  
  // implementation goes here
}
```

In the above example, the `MySpecificationResolver` is constructed with the `SpecCodec` provided by the `SpecMapper`. This allows you to access and utilize the `SpecMapper` functionality within your custom resolver.

### Customize Default SpecMapper

Certainly, you can fully customize the `SpecMapper`. Just register your `SpecMapper` as a Spring `@Bean`, and during the application startup process, the configuration of the **Default SpecMapper** will be **skipped** in favor of the one you registered.

Here's an example configuration:

```java
@Configuration
class MyConfig {

  @Bean
  SpecMapper mySpecMapper() {
    return SpecMapper.builder()
      . ...
      .build();
  }
}
```

In the above example, you can customize the `SpecMapper` according to your needs by providing the desired configuration options within the `mySpecMapper` method. This way, the application will use the `SpecMapper` instance that you registered as a `@Bean`, overriding the default configuration of the `SpecMapper`.

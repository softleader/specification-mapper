# specification-mapper-starter

```xml
<dependency>
  <groupId>tw.com.softleader.data</groupId>
  <artifactId>specification-mapper-starter</artifactId>
  <version>last-release-version</version>
</dependency>
```

specification-mapper-starter 整合了 [specification-mapper](../mapper) 及 [Spring Data JPA](https://spring.io/projects/spring-data-jpa), 並提供了 Query by Spec 的查詢方式等

Query by Spec (QBS) 是一個  user-friendly 的查詢方式, 可以動態的建立查詢條件 ([Specifications](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications)), 透過 QBS interface 就可以執行查詢語句!

## Getting Started

只要在 `pom.xml` 中加入 dependency, 此 Starter 在 Spring Boot 啟動過程就會自動的配置一切, 讓你可以零配置的就開始使用, 包含了:

- [Query By Spec](#query-by-spec) 的設定
- 註冊預設的 [`SpecMapper`](#default-specmapper)


自動配置預設是啟用的, 你可以透過 properties 中的 `spec.mapper.enabled` 控制, 如要關閉則:

```yaml
spec:
  mapper:
    enabled: false
```

## Query by Spec

Query by Spec (QBS) 提供了 [`QueryBySpecExecutor<T>`](./src/main/java/tw/com/softleader/data/jpa/spec/repository/QueryBySpecExecutor.java) 包含了許多查詢方法:

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

### Customize the QBS Base Repository

在配置的過程中, QBS 會自動配置 Spring Data JPA 的 [Base Repository](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.customize-base-repository), 預設的實作為 [`QueryBySpecExecutorImpl`](./src/main/java/tw/com/softleader/data/jpa/spec/repository/support/QueryBySpecExecutorImpl.java)

由於 Java 只能單一繼承, 為了方便應用程式可以保留原有的 Parent Base Repository, QBS 還多提供了 [`QueryBySpecExecutorAdapter`](./src/main/java/tw/com/softleader/data/jpa/spec/repository/support/QueryBySpecExecutorAdapter.java) 擴展點

你的應用程式可以視情況選擇繼承 `QueryBySpecExecutorImpl` 或實作 `QueryBySpecExecutorAdapter` 去客製化 Base Repository, 如:

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
    repository-base-class: com.acme.example.MyRepositoryImpl
```

## Default SpecMapper

此 Starter 會在 App 啟動的過程中自動的配置一個 *Default  SpecMapper* 並註冊到 *Spring @Bean* 中, 你可以透過 *Autowired* 的方式跟 Spring 取得.

例如, 我想要在轉換成 Specification 後, 先做一些加強再去查詢, 則範例如下:

```java
class PersonService {

  @Autowired SpecMapper specMapper;
  @Autowired PersonRepository personRepository;

  List<Person> getPersonByCriteria(PersonCriteria criteria) {
    val spec = specMapper.toSpec(criteria);
    
    // do more to spec
    
    return personRepository.findAll(spec);
  }
}
```

### Customize SpecificationResolver

只要將你自定義的 `SpecificationResolver` 註冊成 *Spring @Bean*, 在 App 啟動的過程中就會自動的偵測並加入到 *Default SpecMapper* 中!

例如, 我想要[增加自定義的 Spec Annotation](../mapper#customize-spec-annotation), 配置範例如下:

```java
@Configuration
class MyConfig {

  @Bean
  SpecificationResolver myResolver() {
    return ...
  }
}
```

如果你的 `SpecificationResolver` 需要用到 `SpecMapper` 本身, 則你可以包裝成 `SpecificationResolverCodecBuilder`, 在建構 resolver 時就會把 `SpecCodec`, 即 `SpecMapper` 的 interface, 傳進去, 例如:

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
    // Keep the SpecCodec around to used.
    this.codec = codec;
  }
  
  // implementation goes here
}
```

### Customize Default SpecMapper

當然, 你也可以完全的客製化 `SpecMapper`, 只要將你的 `SpecMapper` 註冊成 *Spring @Bean*,  App 啟動的過程中就會**略過 *Default SpecMapper* 的配置**而優先採用的你所註冊的那個! 

配置範例如下:

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


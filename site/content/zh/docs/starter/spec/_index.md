---
title: Default SpecMapper
weight: 20
---

此 Starter 會在 App 啟動的過程中自動的配置一個 *Default  SpecMapper* 並註冊到 *Spring @Bean* 中, 你可以透過 *Autowired* 的方式跟 Spring 取得.

例如, 我想要在轉換成 Specification 後, 先做一些加強再去查詢, 則範例如下:

```java
class PersonService {

  @Autowired SpecMapper specMapper;
  @Autowired PersonRepository personRepository;

  List<Person> getPersonByCriteria(PersonCriteria criteria) {
    var spec = specMapper.toSpec(criteria);
    
    // Perform additional operations on the spec, ex:
    // spec = spec.and((root, query, criteriaBuilder) ->  {
    //     ...
    // });
    
    return personRepository.findAll(spec);
  }
}
```

## Customize SpecificationResolver

只要將你自定義的 `SpecificationResolver` 註冊成 *Spring @Bean*, 在 App 啟動的過程中就會自動的偵測並加入到 *Default SpecMapper* 中!

例如, 我想要[增加自定義的 Spec Annotation](/docs/mapper/customize/), 配置範例如下:

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

## Customize SkippingStrategy

只要將你自定義的 [`SkippingStrategy`](/docs/mapper/spec/#skipping-strategy) 註冊成 *Spring @Bean*, 在 App 啟動的過程中就會自動的偵測並加入到 *Default SpecMapper* 中!

配置範例如下:

```java
@Configuration
class MyConfig {

  @Bean
  SkippingStrategy mySkippingStrategy() {
    return ...
  }
}
```

## Customize ASTWriterFactory

透過 properties 中的 `spec.mapper.impersonate-logger`, 可以設定 [Logging](/docs/mapper/logging/) 過程中, 是否要偽裝成實際處理的 object logger, 預設是關閉的, 若要開啟範例如下:

```yaml
spec:
  mapper:
    # 是否要偽裝成實際處理的 object logger, 預設關閉
    impersonate-logger: true
```

若你需要完整的客製化, 只要將你自定義的 `ASTWriterFactory` 註冊成 *Spring @Bean*, 在 App 啟動的過程中就會自動的偵測並加入到 *Default SpecMapper* 中!

配置範例如下:

```java
@Configuration
class MyConfig {

  @Bean
  ASTWriterFactory myASTWriterFactory() {
    return ...
  }
}
```

## Customize Default SpecMapper

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

---
title: Config SpecMapper
weight: 20
description: > 
  自動配置 SpecMapper
---

Starter 會在 App 啟動的過程中自動的配置一個 [*Default SpecMapper*](/docs/mapper/spec/), 並註冊成 *Spring @Bean* 中, 你可以透過 *Autowired* 的方式跟 Spring 取得.

```java
@Autowired SpecMapper specMapper;
```

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

在上述範例中, `SpecMapper` 被注入到了 `PersonService`, 開發人員就可以使用 `specMapper.toSpec()` 將 criteria 物件轉換成 specification, 接著就可以對 specification 做更多的調整, 最後才傳入 `personRepository` 查詢

## Configuration 

Starter 提供了許多方式讓開發人員調整 _Default SpecMapper_ 的配置

### SpecificationResolver

[`SpecificationResolver`](/docs/mapper/customize/) 是用來增加自定義的 Spec Annotation, 只要將你要增加的客製化實作註冊成 *Spring @Bean*, 在 App 啟動的過程中就會自動的偵測及配置

範例如下:

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

### SkippingStrategy

[`SkippingStrategy`](/docs/mapper/spec/#skipping-strategy) 用來提供欄位跳脫的策略, 只要將你自定義的實作註冊成 *Spring @Bean*, 在 App 啟動的過程中就會自動的偵測並加入到 *Default SpecMapper* 中!

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

### ASTWriterFactory

在 [Logging](/docs/mapper/logging/) 中提供了不同的 Logger Name 策略, 透過 properties 中的 `spec.mapper.impersonate-logger`, 可以設定是否要偽裝成實際處理的 object logger, 預設是關閉的, 若要開啟範例如下:

```yaml
spec:
  mapper:
    # 是否要偽裝成實際處理的 object logger, 預設關閉
    impersonate-logger: true
```

若你需要完整的客製化, 只要將你自定義的 `ASTWriterFactory` 實作註冊成 *Spring @Bean*, 在 App 啟動的過程中就會自動的偵測並加入到 *Default SpecMapper* 中!

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

## Customize SpecMapper

當然, 你也可以完全的客製化 `SpecMapper`, 只要將你的 `SpecMapper` 註冊成 *Spring @Bean*, 就會最優先的使用!

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

{{% alert title="Important" color="warning" %}}
採此模式客製化, App 啟動的過程中就會略過此章節所有配置內容
{{% /alert %}}

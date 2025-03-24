---
title: Config SpecMapper
weight: 20
description: > 
  Auto-configuration of SpecMapper
---

During application startup, the starter automatically configures a [*Default SpecMapper*](/docs/mapper/spec/) and registers it as a *Spring @Bean*, allowing you to retrieve it via Autowired.

```java
@Autowired SpecMapper specMapper;
```

For example, if you want to enhance the specifications before performing the query, you can use the SpecMapper as follows:

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

In the above example, the `SpecMapper` is injected into the `PersonService`, allowing you to convert the criteria into a specification using `specMapper.toSpec()`. You can then modify the spec as needed before passing it to the `personRepository` for querying.

## Configuration

The starter provides multiple ways to adjust the configuration of the *Default SpecMapper*.

### SpecificationResolver

SpecificationResolver allows you to add custom Spec annotations. Simply register your custom implementation as a Spring @Bean, and it will be automatically detected and configured during application startup.

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

### SkippingStrategy

[`SkippingStrategy`](/mapper/spec/#skipping-strategy) defines rules for skipping specific fields. By registering your custom implementation as a Spring @Bean, it will be automatically detected and added to the *Default SpecMapper* during application startup.

Example:

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

As described in [Logging](/docs/mapper/logging/), different Logger Name strategies are available. You can enable impersonation mode using the `spec.mapper.impersonate-logger` property. By default, this setting is disabled. To enable it:

```yaml
spec:
  mapper:
    # Whether to impersonate the logger of the actual object being processed, off by default
    impersonate-logger: true
```

For full customization, register your own ASTWriterFactory implementation as a *Spring @Bean*, and it will be automatically detected and added to the *Default SpecMapper*.

Example:

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

You can also fully customize `SpecMapper` by registering your own implementation as a *Spring @Bean*, which will take precedence over the default configuration.

Example:

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
If you customize SpecMapper in this way, all configurations mentioned in this section will be skipped during application startup.
{{% /alert %}}

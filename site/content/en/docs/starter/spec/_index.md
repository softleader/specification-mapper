---
title: Default SpecMapper
weight: 20
---

This starter automatically configures a *Default SpecMapper* during the application startup process and registers it as a Spring `@Bean`. You can obtain an instance of the SpecMapper through dependency injection (`@Autowired`) in your application.

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

In the above example, the SpecMapper is injected into the `PersonService`, allowing you to convert the criteria into a specification using `specMapper.toSpec()`. You can then modify the spec as needed before passing it to the `personRepository` for querying.

## Customize SpecificationResolver

By registering your custom `SpecificationResolver` as a Spring `@Bean`, it will be automatically detected and added to the *Default SpecMapper* during the application startup process.

For example, if you want to add your custom spec annotation as mentioned in [Customize Spec](/docs/mapper/customize/), you can configure it as follows:

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

## Customize SkippingStrategy

As long as you register your custom [`SkippingStrategy`](/mapper/spec/#skipping-strategy) as a *Spring @Bean*, it will automatically be detected and added to the *Default SpecMapper* during the application startup process!

Here's a configuration example:

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

Through the `spec.mapper.impersonate-logger` property, you can set whether the [Logging](/docs/mapper/logging/) process should impersonate the logger of the actual object being processed. It is turned off by default. To enable it, see the example below:

```yaml
spec:
  mapper:
    # Whether to impersonate the logger of the actual object being processed, off by default
    impersonate-logger: true
```

If you need full customization, simply register your custom `ASTWriterFactory` as a *Spring @Bean*. It will automatically be detected and added to the *Default SpecMapper* during the app startup!

Here's how to configure it:

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

[![version](https://img.shields.io/github/v/release/softleader/specification-mapper?color=brightgreen&sort=semver)](https://github.com/softleader/specification-mapper/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/tw.com.softleader.data.jakarta/specification-mapper-parent?color=orange)](https://central.sonatype.com/search?q=g%3Atw.com.softleader.data.jakarta&smo=true&namespace=tw.com.softleader.data.jakarta)
![GitHub tag checks state](https://img.shields.io/github/checks-status/softleader/specification-mapper/jakarta)
![GitHub issues](https://img.shields.io/github/issues-raw/softleader/specification-mapper)

# specification-mapper

An alternative Specification API for Spring Data JPA, Jakarta EE 9. 

## What is Specification Mapper

Specification Mapper is a tool that facilitates the construction of [Specifications](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications). It reads the fields of a POJO and, through simple and memorable annotations, converts the values of these fields into Specifications. Specification Mapper also provides many extension points, allowing you to easily expand and implement your own logic.

It is influenced by [tkaczmarzyk/specification-arg-resolver](https://github.com/tkaczmarzyk/specification-arg-resolver). We used specification-arg-resolver extensively for a period of time. However, in specification-arg-resolver, we had to expose the entity directly to the API. This became a conflict when we started implementing the [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) and became more concerned about layer dependencies. This conflict led to the development of Specification Mapper.

In the Clean Architecture, Specification Mapper provides a convenient way to convert and use any POJO in any layer. For example, in the infrastructure layer, Specification mapper can be used to convert domain objects into specifications. By using Specification Mapper, we can effectively manage dependencies between layers and achieve a clear and structured code architecture, enhancing code readability and maintainability.

The current version is implemented using Jakarta. If you are using Javax, please refer to [this link](https://github.com/softleader/specification-mapper/tree/javax).

## Getting Started

- **Spec Mapper API:** See documentation for [specification-mapper](./mapper).
- **Spring Starter:** See documentation for [specification-mapper-starter](./starter).

## Compatibility

### Java

- 17
- 21

### Spring

The following shows versions with compatible [Spring Boot](https://spring.io/projects/spring-boot) releases.

- 3.0.x
- 3.1.x
- 3.2.x

## License

[Apache 2.0 License](./LICENSE).

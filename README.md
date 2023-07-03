# specification-mapper

An alternative Specification API for Spring Data JPA.

## What is Specification Mapper

Specification Mapper is a tool that facilitates the construction of [Specifications](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications). It reads the fields of a POJO and, through simple and memorable annotations, converts the values of these fields into Specifications. Specification Mapper also provides many extension points, allowing you to easily expand and implement your own logic.

It is influenced by [tkaczmarzyk/specification-arg-resolver](https://github.com/tkaczmarzyk/specification-arg-resolver). We used specification-arg-resolver extensively for a period of time. However, in specification-arg-resolver, we had to expose the entity directly to the API. This became a conflict when we started implementing the [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) and became more concerned about layer dependencies. This conflict led to the development of Specification Mapper.

In the Clean Architecture, Specification Mapper provides a convenient way to convert and use any POJO in any layer. By using Specification Mapper, we can effectively manage dependencies between layers and achieve a clear and structured code architecture. With Specification Mapper, you can define and manipulate specifications more effortlessly in your application, improving code readability and maintainability.

The current version is implemented using Javax. Versions for Javax implementations typically start with _1.x_. If you are using Jakarta, please refer to [this link](https://github.com/softleader/specification-mapper/tree/jakarta).

## Getting Started

- **Spec Mapper API:** See documentation for [specification-mapper](./mapper).
- **Spring Starter:** See documentation for [specification-mapper-starter](./starter).

## Compatibility

### Java

- 8
- 11
- 17

### Spring

The following shows versions with compatible [Spring Boot](https://spring.io/projects/spring-boot) releases.

- 2.4.x
- 2.5.x
- 2.6.x
- 2.7.x

## License

[Apache 2.0 License](./LICENSE).

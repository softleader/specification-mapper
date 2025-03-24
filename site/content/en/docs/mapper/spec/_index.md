---
title: SpecMapper
weight: 10
description: > 
  The entry point for specification operations
---

`SpecMapper` is the most important class and serves as the API entry point for all specification operations:

```java
var mapepr = SpecMapper.builder().build();
```

Next, we define a POJO (Plain Old Java Object) that encapsulates the query conditions, such as:

```java
@Data
public class CustomerCriteria {

  @Spec(Like.class)
  String firstname;
}
```

With this, we can perform the conversion to a `Specification`. Once we have the `Specification`, we can query the database using the original approach, for example, through the repository of Spring Data JPA:

```java
var criteria = new CustomerCriteria();
criteria.setFirstname("Hello")

var mapper = SpecMapper.builder().build();
var specification = mapper.toSpec(criteria);

customerRepository.findAll(specification);
```

The executed SQL will be like:

```
... where x.firstname like '%Hello%'
```

## Skipping Strategy

In the fields of the POJO, if any of the following conditions are met, they will be ignored during the conversion process:

- No Spec Annotation is attached.
- The value is *null*.
- If the type is *Iterable* and the value is *empty*.
- If the type is *Optional* and the value is *empty*.
- If the type is *CharSequence* and the length of value is *0*.
- If the type is *Array* and the length of value is *0*.
- If the type is *Map* and the value is *empty*.

For example, after constructing the following POJO, if no values are set and it is directly converted into a `Specification` for querying:

```java
@Data
public class CustomerCriteria {

  @Spec(Like.class)
  String firstname;
  
  String lastname = "Hello";
   
  @Spec
  String nickname = "";
  
  @Spec(GreaterThat.class)
  Optional<Integer> age = Optional.empty();
  
  @Spec(In.class)
  Collection<String> addresses = Arrays.asList();
}

var mapper = SpecMapper.builder().build();
customerRepository.findAll(mapper.toSpec(new CustomerCriteria()));
```

The executed SQL in the above example will not have any filtering conditions.

> If you are using the Builder Pattern (e.g., Lombok's *@Builder*), please pay special attention to the default values set in the builder.

If you want to customize the logic for skipping, you can implement a `SkippingStrategy` and pass it when constructing a `SpecMapper`:

```java
var mapper = SpecMapper.builder()
      .defaultResolvers()
      .skippingStrategy(fieldValue -> {
        // Determine whether to skip the field value and return a boolean
      })
      .build();
```

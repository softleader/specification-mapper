---
title: Combine Specs
weight: 30
---

You can use `@And` or `@Or` to combine multiple specifications within an object. The default combination is `@And`.

## Combine on Classes

`@And` or `@Or` can be used at the class level , For example, if you want to change it to `@Or`, the code would be as follows:

```java
@Or // Default is @And if not specified
@Data
public class CustomerCriteria {

  @Spec(Like.class)
  String firstname;
  
  @Spec(Like.class)
  String lastname;
}
```

The executed SQL will be like:

```
... where x.firstname like %?% or x.lastname like %?% 
```

## Combine on Fields

You can also use `@And` or `@Or` annotations on fields to control how an individual field is combined with other fields. Here's an example:

```java
@Data
public class CustomerCriteria {

  @Spec(Like.class)
  String firstname;
  
  @Spec(Like.class)
  String lastname;

  @Or
  @Spec(value = After.class, not = true)
  LocalDate birthday;
}
```

The executed SQL will be like:

```
... where (x.firstname like ?) and (x.lastname like ?) or x.birthday<=?
```

**Note that the fields are combined in the order they are declared, and SQL has operator precedence. Please ensure that the combination and the result align with your expectations.**

For example, if we adjust the field order in the above example:

```java
@Data
public class CustomerCriteria {

  @Spec(Like.class)
  String firstname;
  
  @Or
  @Spec(value = After.class, not = true)
  LocalDate birthday;
  
  @Spec(Like.class)
  String lastname;
}
```

The executed SQL will be like:

```
... where (x.firstname like ? or x.birthday<=?) and (x.lastname like ?)
```

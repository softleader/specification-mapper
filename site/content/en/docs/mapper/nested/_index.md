---
title: Nested Specs
weight: 40
description: > 
  Handle query conditions for nested objects
---

You can use `@NestedSpec` on a field to instruct `SpecMapper` to combine specifications with the nested object. There is no level limitation, so you can keep going deeper!

For example, let's say we have a shared `AddressCriteria` POJO, and we want to include it in other POJOs. The code would look like this:

```java
@Data
public class CustomerCriteria {

  @Spec(Like.class)
  String firstname;
  
  @NestedSpec
  AddressCriteria address;
}

@Or
@Data
public class AddressCriteria {

  @Spec
  String county;
  
  @Spec
  String city;
}
```

The executed SQL will be like:

```
... where x.firstname like %?% and ( x.county=? or x.city=? )
```

### Combine Nested Specs

You can also declare `@And` or `@Or` on fields within the nested object to control how the result is combined with other fields. For detailed information, please refer to [Specify Combining Type on Field](/docs/mapper/combine/#combine-on-fields).

Here's an example:

```java
@Data
public class CustomerCriteria {

  @Spec(Like.class)
  String firstname;
  
  @Or
  @NestedSpec
  AddressCriteria address;
}

@Data
public class AddressCriteria {

  @Spec
  String county;
  
  @Spec
  String city;
}
```

The executed SQL will be like:

```
... where (x.firstname like ?) or x.county=? and x.city=?
```

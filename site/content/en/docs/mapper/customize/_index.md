---
title: Customize Specs
weight: 70
description: > 
  Extend and customize query conditions
---

Continuing from the example in the section [Extending @Spec](/mapper/simple/#extending-spec), let's take it a step further. Now, we want to make the entity class configurable so that it can be used for entities other than `Customer`. 

To fulfill this requirement, we need to define additional parameters in the annotation. As a result, the Simple `@Spec` approach is no longer suitable. Instead, we need to define a new annotation. Here's the complete code:

First, we define the annotation:

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface MaxCreatedTime {

  Class<?> from();
}
```

Next, we need to implement the logic responsible for handling `@MaxCreatedTime`. We can extend it by implementing the `SpecificationResolver` interface:

```java
public class MaxCreatedTimeSpecificationResolver implements SpecificationResolver {

  @Override
  public boolean supports(Databind databind) { 
    // Here, we tell the SpecMapper when to use this resolver
    return databind.getField().isAnnotationPresent(MaxCreatedTime.class);
  }

  @Override
  public Specification<Object> buildSpecification(Context context, Databind databind) {
    var def = databind.getField().getAnnotation(MaxCreatedTime.class);
    return databind.getFieldValue()
        .map(value -> subquery(def.from(), value.toString()))
        .orElse(null);
  }

  Specification<Object> subquery(Class<?> entityClass, String by) {
    // Here, we provide an example implementation for the subquery
    return (root, query, builder) -> {
      var subquery = query.subquery(LocalDateTime.class);
      var subroot = subquery.from(entityClass);
      subquery.select(
        builder.greatest(subroot.get("createdTime").as(LocalDateTime.class))
      ).where(builder.equal(root.get(by), subroot.get(by)));
      return builder.equal(root.get("createdTime"), subquery);
    };
  }
}
```

Next, we add this resolver to the `SpecMapper` during its construction:

```java
var mapper = SpecMapper.builder()
      .defaultResolvers()
      .resolver(new MaxCreatedTimeSpecificationResolver())
      .build();
```

Finally, we define the POJO and convert it to a `Specification`:

```java
@Data
public class CustomerCriteria {

  @MaxCreatedTime(from = Customer.class)
  String maxBy;
}

var criteria = new CustomerCriteria();
criteria.setMaxBy("firstname");

var spec = mapper.toSpec(criteria, Customer.class);
repository.findAll(spec);
```

The executed SQL will be like:

```
... where customer0_.created_time=(
  select max(customer1_.created_time) from customer customer1_ 
  where customer0_.firstname=customer1_.firstname
)
```

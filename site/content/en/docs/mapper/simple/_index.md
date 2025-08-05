---
title: Simple Specs
weight: 20
description: > 
  Basic query conditions
---

You can use `@Spec` on fields to define the implementation of the `Specification`, `Equals` spec is the default:

```java
@Spec // Equivalent to @Spec(Equals.class)
String firstname;
```

The corresponding entity path will default to the field name, but you can also set `@Spec#path` to change it:

```java
@Spec(path = "...") // Takes precedence if defined
String firstname; // Defaults to the field name
```

## Built-in @Spec

Here is a list of the built-in types for `@Spec`:

| Spec | Supported field type | Sample | JPQL snippet |
|---|---|---|---|
| `Equals` | *Any*  | `@Spec(Equals.class) String firstname;` | `... where x.firstname = ?` |
| `NotEquals` | *Any*| `@Spec(NotEquals.class) String firstname;` | `... where x.firstname <> ?` |
| `Between` | *Iterable of Comparable* <br> (Expected exact **2 elements** in *Iterable*) | `@Spec(Between.class) List<Integer> age;` | `... where x.age between ? and ?` |
| `LessThan` | *Comparable* | `@Spec(LessThan.class) Integer age;` | `... where x.age < ?` |
| `LessThanEqual` | *Comparable* | `@Spec(LessThanEqual.class) Integer age;` | `... where x.age <= ?` |
| `GreaterThan` | *Comparable* | `@Spec(GreaterThan.class) Integer age;` | `... where x.age > ?` |
| `GreaterThanEqual` | *Comparable* | `@Spec(GreaterThanEqual.class) Integer age;` | `... where x.age >= ?` |
| `After` | *Comparable* | `@Spec(After.class) LocalDate startDate;` | `... where x.startDate > ?` |
| `Before` | *Comparable* | `@Spec(Before.class) LocalDate startDate;` | `... where x.startDate < ?` |
| `IsNull` | *Boolean* | `@Spec(IsNull.class) Boolean age;` | `... where x.age is null` *(if true)* <br> `... where x.age not null` *(if false)* |
| `NotNull` | *Boolean* | `@Spec(NotNull .class) Boolean age;` | `... where x.age not null` *(if true)* <br> `... where x.age is null` *(if false)* |
| `Like` | *String* | `@Spec(Like.class) String firstname;` | `... where x.firstname like %?%` |
| `NotLike` | *String* | `@Spec(NotLike.class) String firstname;` | `... where x.firstname not like %?%` |
| `StartingWith` | *String* | `@Spec(StartingWith.class) String firstname;` | `... where x.firstname like ?%` |
| `EndingWith` | *String*| `@Spec(EndingWith.class) String firstname;` | `... where x.firstname like %?` |
| `In` | *Iterable of Any* | `@Spec(In.class) Set<String> firstname;` | `... where x.firstname in (?, ?, ...)` |
| `NotIn` | *Iterable of Any* | `@Spec(NotIn.class) Set<String> firstname;` | `... where x.firstname not in (?, ?, ...)` |
| `True` | *Boolean* | `@Spec(True.class) Boolean active;` | `... where x.active = true` *(if true)* <br> `... where x.active = false` *(if false)* |
| `False` | *Boolean* | `@Spec(False.class) Boolean active;` | `... where x.active = false` *(if true)* <br> `... where x.active = true` *(if false)* |
| `HasLength` | *Boolean* | `@Spec(HasLength.class) Boolean firstname;` | `... where x.firstname is not null and character_length(x.firstname)>0` *(if true)* <br> `... where not(x.firstname is not null and character_length(x.firstname)>0)` *(if false)* |
| `HasText` | *Boolean* | `@Spec(HasText.class) Boolean firstname;` | `... where x.firstname is not null and character_length(trim(BOTH from x.firstname))>0` *(if true)* <br> `... where not(where x.firstname is not null and character_length(trim(BOTH from x.firstname))>0)` *(if false)* |

> In order to facilitate the usage for those who are already familiar with Spring Data JPA, the specs are named as closely as possible with [Query Methods](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html#jpa.query-methods.query-creation)


## Negates @Spec

You can use `@Spec#not` to indicate the inverse condition. By default, it is set to `false`, but if you set it to `true`, the result will be inverted.

For example, if you want to use `Between` to find data **outside** of a certain range, the example would be as follows:

```java
@Spec(value = Between.class, not = true)
Collection<Integer> age;
```

The executed SQL will be like:

```sql
... where x.age not between ? and ?
```

## Extending @Spec

`@Spec` can be easily extended by implementing `SimpleSpecification<T>` and providing the required constructor. These classes can then be used in `@Spec` annotations.

For example, let's say we have a `Customer` entity with the following fields:
- `firstname` (String): Name of the person (can be duplicated)
- `createdTime` (LocalDateTime): Creation time (unique)

We want to retrieve the data for each unique name with the latest creation time. To achieve this, we plan to write a subquery. The complete example is as follows:

First, we implement `SimpleSpecification<T>` and provide the required constructor:

```java
public class MaxCustomerCreatedTime extends SimpleSpecification<Customer> {

  // This is the required constructor, the modifier can be public, protected, default, or private
  protected MaxCustomerCreatedTime(Context context, String path, Object value) {
    super(context, path, value);
  }

  @Override
  public Predicate toPredicate(Root<Customer> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
    // The following provides an example implementation for the subquery
    var subquery = query.subquery(LocalDateTime.class);
    var subroot = subquery.from(Customer.class);
    subquery.select(builder.greatest(subroot.get("createdTime").as(LocalDateTime.class)))
            .where(builder.equal(root.get((String) value), subroot.get((String) value)));
    return builder.equal(root.get("createdTime"), subquery);
  }
}
```

The `MaxCustomerCreatedTime` class we implemented above can now be used in `@Spec`. Next, we define the POJO and convert it to a `Specification`:

```java
@Data
public class CustomerCriteria {

  @Spec(MaxCustomerCreatedTime.class)
  String maxBy;
}

var criteria = new CustomerCriteria();
criteria.setMaxBy("firstname");

var spec = mapper.toSpec(criteria, Customer.class);
repository.findAll(spec);
```

The executed SQL will be like:

```sql
... where customer0_.created_time=(
  select max(customer1_.created_time) from customer customer1_ 
  where customer0_.firstname=customer1_.firstname
)
```

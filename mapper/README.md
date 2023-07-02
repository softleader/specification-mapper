[中文版](./README.tw-zh.md)

# specification-mapper

```xml
<dependency>
  <groupId>tw.com.softleader.data.jakarta</groupId>
  <artifactId>specification-mapper</artifactId>
  <version>last-release-version</version>
</dependency>
```

specification-mapper is a generator for [Specifications](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications). It reads the fields from an object and dynamically creates query conditions based on the definitions of the fields' annotations.

In addition, [specification-mapper-starter](../starter) provides integration with Spring Boot, allowing you to use it effortlessly in Spring apps without any configuration. We highly recommend checking it out if you are using a Spring Boot application!

## Getting Started

What we need is to create an instance of `SpecMapper`, which is the most important class and serves as the API entry point for all specification operations:

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

The executed SQL will be:

```
... where x.firstname like '%Hello%'
```

### Skipping Strategy

In the fields of the POJO, if any of the following conditions are met, they will be ignored during the conversion process:

- No Spec Annotation is attached.
- If the type is *Iterable* and the value is *empty*.
- If the type is *Optional* and the value is *empty*.
- The value is *null*.

For example, after constructing the following POJO, if no values are set and it is directly converted into a `Specification` for querying:

```java
@Data
public class CustomerCriteria {

  @Spec(Like.class)
  String firstname;
  
  String lastname = "Hello";
  
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

## Simple Specifications

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

### Built-in Simple @Spec

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

> In order to facilitate the usage for those who are already familiar with Spring Data JPA, the specs are named as closely as possible with [Query Methods](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation)

### Negates the @Spec

You can use `@Spec#not` to indicate the inverse condition. By default, it is set to `false`, but if you set it to `true`, the result will be inverted.

For example, if you want to use `Between` to find data **outside** of a certain range, the example would be as follows:

```java
@Spec(value = Between.class, not = true)
Collection<Integer> age;
```

The executed SQL will be:

```
... where x.age not between ? and ?
```

### Extending Simple @Spec

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

The executed SQL will be:

```
... where customer0_.created_time=(
  select max(customer1_.created_time) from customer customer1_ 
  where customer0_.firstname=customer1_.firstname
)
```

## Combining Specs

You can use `@And` or `@Or` at the class level to combine multiple specifications within an object. The default combination is `@And`.

For example, if you want to change it to `@Or`, the code would be as follows:

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

The executed SQL will be:

```
... where x.firstname like %?% or x.lastname like %?% 
```

### Specify Combining Type on Field

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

The executed SQL will be:

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

The executed SQL will be:

```
... where (x.firstname like ? or x.birthday<=?) and (x.lastname like ?)
```

## Nested Specs

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

The executed SQL will be:

```
... where x.firstname like %?% and ( x.county=? or x.city=? )
```

### Specify Combining Type on Nested Object

You can also declare `@And` or `@Or` on fields within the nested object to control how the result is combined with other fields. For detailed information, please refer to [Specify Combining Type on Field](#specify-combining-type-on-field).

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

The executed SQL will be:

```
... where (x.firstname like ?) or x.county=? and x.city=?
```

## Join

You can use `@Join` on fields to filter associated entities. It is important that the relationships between entities are properly defined beforehand. For example:

```java
@Entity
class Customer {

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private Collection<Order> orders;
}

@Entity
class Order {

  private String itemName;
}
```

If you want to query customers who have purchased specific items, you can define the POJO as follows:

```java
@Data
public class CustomerOrderCriteria {

  @Join(path = "orders", alias = "o")
  @Spec(path = "o.itemName", value = In.class)
  Collection<String> items;
}
```

The executed SQL will be:

```
select distinct ... from customer customer0_ 
inner join orders orders1_ on customer0_.id=orders1_.order_id 
where orders1_.item_name in (?, ?)
```

To align with most use cases, the default join type is `INNER`, and the result is made distinct. You can modify this behavior by setting `@Join#joinType` or `@Join#distinct`. For example:

```java
@Join(joinType = JoinType.RIGHT, distinct = false)
```

### Multi Level Joins

You can use `@Joins` to define multi-level joins. For example:

```java
@Entity
class Customer {

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private Set<Order> orders;
}

@Entity
class Order {
    
  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<Tag> tags;
}

@Entity
class Tag {

  private String name;
}
```

If you want to query customers who have purchased items with specific tags, you can define the POJO as follows:

```java
@Data
class CustomerOrderTagCriteria {

  @Joins({
    @Join(path = "orders", alias = "o"),
    @Join(path = "o.tags", alias = "t")
  })
  @Spec(path = "t.name", value = In.class)
  Collection<String> tags;
}
```

The executed SQL will be:

```
select distinct ... from customer customer0_ 
inner join orders orders1_ on customer0_.id=orders1_.order_id 
inner join orders_tags tags2_ on orders1_.id=tags2_.order_id 
inner join tag tag3_ on tags2_.tags_id=tag3_.id 
where tag3_.name in (?)
```

**Note that the processing of annotations is sequential, so the order of `@Joins` must match the order of joins.**

For example, in the given scenario, the following definition order is incorrect:

```java
@Data
class CustomerOrderTagCriteria {

  @Joins({
    @Join(path = "o.tags", alias = "t"), // "o" alias will not exist during the processing of this @Join
    @Join(path = "orders", alias = "o")
  })
  @Spec(path = "t.name", value = In.class)
  Collection<String> tagNames;
}
```

## Join Fetch

You can use `@JoinFetch` at the class level to fetch all lazy associated data at once. For example:

```java
@Entity
class Customer {

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "order_id")
  private Collection<Order> orders;
}

@Entity
class Order {
  
  private String itemName;
}
```

If you want to fetch the `Order` entities along with the `Customer` entity, you can use:

```java
@JoinFetch(paths = "orders")
@Data
class CustomerOrderCriteria {

  @Spec
  String name;
}
```

The executed SQL will be:

```
select distinct 
  customer0_.* ...,
  orders1_.* ...
from customer customer0_ 
left outer join orders orders1_ on customer0_.id=orders1_.order_id 
where customer0_.name=?
```

To align with most use cases, the default join type is `LEFT`, and the result is made distinct. You can modify this behavior by setting `@FetchJoin#joinType` or `@FetchJoin#distinct`. For example:

```java
@FetchJoin(joinType = JoinType.RIGHT, distinct = false)
```

### Multi Level Fetch Joins

You can use `@FetchJoins` to define multi-level fetch joins. For example:

```java
@Entity
class Customer {

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private Set<Order> orders;
}

@Entity
class Order {
    
  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<Tag> tags;
}

@Entity
class Tag {

  private String name;
}
```

If you want to fetch the `Order` and `Tag` entities along with the `Customer` entity, you can use:

```java
@JoinFetches({
  @JoinFetch(paths = "orders"),
  @JoinFetch(paths = "orders.tags")
})
@Data
class CustomerOrderTagCriteria {

  @Spec
  String name;
}
```

The executed SQL will be:

```
select distinct 
  customer0_.* ...,
  orders1_.* ...,
  tags3_.* ...
from customer customer0_ 
left outer join orders orders1_ on customer0_.id=orders1_.order_id 
inner join orders orders2_ on customer0_.id=orders2_.order_id 
left outer join orders_tags tags3_ on orders2_.id=tags3_.order_id 
left outer join tag tag4_ on tags3_.tags_id=tag4_.id 
where customer0_.name=?
```

## Customize Spec Annotation

Continuing from the example in the section [Extending Simple @Spec](#extending-simple-spec), let's take it a step further. Now, we want to make the entity class configurable so that it can be used for entities other than `Customer`. 

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

The executed SQL will be:

```
... where customer0_.created_time=(
  select max(customer1_.created_time) from customer customer1_ 
  where customer0_.firstname=customer1_.firstname
)
```

## Logging 

To set the package `tw.com.softleader.data.jpa.spec` to logging level `debug`, which prints more information during the object-to-spec conversion process:

```
DEBUG 20297 --- [           main] t.c.softleader.data.jpa.spec.SpecMapper  : --- Spec AST ---
+-[CustomerCriteria]: my.package.CustomerCriteria
|  +-[CustomerCriteria.firstname]: @Spec(value=Equals, path=, not=false) -> Equals[path=name, value=matt]
|  +-[CustomerCriteria.address]: my.package.AddressCriteria (NestedSpecificationResolver)
|  |  +-[AddressCriteria.county]: @Spec(value=Equals, path=, not=false) -> null
|  |  +-[AddressCriteria.city]: @Spec(value=Equals, path=, not=false) -> Equals[path=name, value=Taipei]
|  \-[CustomerCriteria.address]: Conjunction[specs=[Equals[path=city, value=Taipei]]]
\-[CustomerCriteria]: Conjunction[specs=[Equals[path=name, value=matt], Conjunction[specs=[Equals[path=city, value=Taipei]]]]]
```

## Limitation

When `SpecMapper` searches for fields in a POJO, it only looks for local fields within the current class and does not traverse the hierarchy of classes to find fields. If you have shared fields that you want to use in multiple POJOs, consider using the [Nested Specs](#nested-specs) approach.

---
title: Join Fetch
weight: 60
---

In a POJO, you can use `@JoinFetch` on a field or class to filter associated entities. The difference from [`@Join`](/docs/mapper/join) is that this allows fetching all lazy-related data at once.

## Single Level Fetch

For example, consider a Customer entity that has a one-to-many relationship with an Order entity:

```java
@Entity
class Customer {

  @OneToMany(fetch = LAZY, cascade = ALL)
  @JoinColumn(name = "order_id")
  private Collection<Order> orders;
}

@Entity
class Order {
  
  private String itemName;
}
```

If you want to fetch Order data when retrieving Customer, you can do:

```java
@Data
@JoinFetch(paths = "orders")
class CustomerOrderCriteria {

  @Spec
  String name;
}
```

The executed SQL will be:

```sql
select distinct 
  customer0_.* ...,
  orders1_.* ...
from customer customer0_ 
inner outer join orders orders1_ on customer0_.id=orders1_.order_id 
where customer0_.name=?
```

> You can see that `orders1_.*` is also included in the select fields. 

### Join Type

To fit most use cases, the default join type is `INNER`, and duplicate results are eliminated using `DISTINCT`. You can change this behavior using `@FetchJoin#joinType` or `@FetchJoin#distinct`, for example:

```java
@FetchJoin(joinType = JoinType.RIGHT, distinct = false)
```

### With Clause

`@JoinFetch` also allows filtering data. For example, if you want to filter both customer names and order names, define a POJO as follows:

```java
@Data
@JoinFetch(path = "orders", alias = "o")
public class CustomerOrderCriteria {

  @Spec
  String name;

  @Spec(path = "o.itemName", value = In.class)
  Collection<String> items;
}
```

The executed SQL will be:

```sql
select distinct 
  customer0_.* ...,
  orders1_.* ...
from customer customer0_ 
inner outer join orders orders1_ on customer0_.id=orders1_.order_id 
where customer0_.name=? and orders1_.item_name in (?)
```

`@JoinFetch` can also be used at the field level, for example:

```java
@Data
public class CustomerOrderCriteria {

  @Spec
  String name;

  @JoinFetch(path = "orders", alias = "o")
  @Spec(path = "o.itemName", value = In.class)
  Collection<String> items;
}
```

## Multi Level Fetches

You can use `@JoinFetches` to define multi-level fetches. For example, if the Order entity has a many-to-many relationship with a Tag entity:

```java
@Entity
class Customer {

  @OneToMany(cascade = ALL, fetch = LAZY)
  @JoinColumn(name = "order_id")
  private Set<Order> orders;
}

@Entity
class Order {
    
  @ManyToMany(cascade = ALL, fetch = LAZY)
  private Set<Tag> tags;
}

@Entity
class Tag {

  private String name;
}
```

If you want to query customers who purchased items belonging to specific categories, define a POJO as follows:

```java
@Data
class CustomerOrderTagCriteria {

  @JoinFetches({
    @JoinFetch(path = "orders", alias = "o"),
    @JoinFetch(path = "o.tags", alias = "t")
  })
  @Spec(path = "t.name", value = In.class)
  Collection<String> tags;
}
```

The executed SQL will be:

```sql
select distinct 
  customer0_.* ...,
  orders1_.* ...,
  tag3_.* ...,
from customer customer0_ 
inner join orders orders1_ on customer0_.id=orders1_.order_id 
inner join orders_tags tags2_ on orders1_.id=tags2_.order_id 
inner join tag tag3_ on tags2_.tags_id=tag3_.id 
where tag3_.name in (?)
```

`@JoinFetches` can also be used at the class level, making the `aliases` available across fields in the same object. For example:

```java
@Data
@JoinFetches({
  @JoinFetch(path = "orders", alias = "o"),
  @JoinFetch(path = "o.tags", alias = "t")
})
public class CustomerOrderCriteria {

  @Spec(path = "o.itemName", value = In.class)
  Collection<String> items;

  @Spec(path = "t.name", value = In.class)
  Collection<String> tags;
}
```

### Fetches Order

Annotations are processed in order, so `@JoinFetches` must be defined in the correct sequence.

For example, in the previous scenario, the following order is incorrect:

```java
@Data
class CustomerOrderTagCriteria {

  @JoinFetches({
    @JoinFetch(path = "o.tags", alias = "t"), // "o" alias will be not exist during processing this @Join
    @JoinFetch(path = "orders", alias = "o")
  })
  @Spec(path = "t.name", value = In.class)
  Collection<String> tagNames;
}
```

## Alias

The usage rules for `@JoinFetch#alias` are as follows:

- It is shared within the same POJO
- It cannot be declared multiple times within the same POJO
- If not provided, the default alias is `@JoinFetch#path`
- If it contains `.`, it will be replaced with `_`

For example:

```java
@JoinFetches({
  @JoinFetch(path = "orders"), // default alias is "orders"
  @JoinFetch(path = "orders.tags") // default alias is "orders_tags"
})
@Spec(path = "orders_tags.name", value = In.class)
```

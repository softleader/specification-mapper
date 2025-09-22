---
title: Join
weight: 50
description: > 
  Filter related entities
---

In a POJO, you can use `@Join` on a field or class to filter related entities.

## Single Join

To use `@Join`, you first need to define the relationship in your entity. For example, a `Customer` entity has a one-to-many relationship with an `Order` entity:

```java
@Entity
class Customer {

  @OneToMany(cascade = ALL, fetch = LAZY)
  @JoinColumn(name = "order_id")
  private Collection<Order> orders;
}

@Entity
class Order {

  private String itemName;
}
```

If you want to query customers who bought specific items, you can define a POJO like this:

```java
@Data
public class CustomerOrderCriteria {

  @Join(path = "orders", alias = "o")
  @Spec(path = "o.itemName", value = In.class)
  Collection<String> items;
}
```

The executed SQL will be like:

```sql
select distinct customer0_.* from customer customer0_ 
inner join orders orders1_ on customer0_.id=orders1_.order_id 
where orders1_.item_name in (? , ?)
```

`@Join` can also be used at the class level. In this case, all fields within the same object can use the alias to apply additional conditions to the joined entity. For example:

```java
@Data
@Join(path = "orders", alias = "o")
public class CustomerOrderCriteria {

  @Spec(path = "o.itemName", value = In.class)
  Collection<String> items;

  @Spec(path = "o.orderNo", value = StartingWith.class)
  String orderNo;
}
```

### Join Behavior

To better align with most usage scenarios, the default behavior is as follows:

- The default join type is `INNER`.
- Duplicate results are removed (`distinct`).

You can modify the default behavior by configuring `@Join#joinType` or `@Join#distinct`, for example:

```java
@Join(joinType = JoinType.RIGHT, distinct = false)
```

## Multi Joins

You can use `@Joins` to define multi-level joins. For example, if the Order entity has a many-to-many relationship with a Tag entity:

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

If you want to query customers who bought items belonging to specific categories, you can define a POJO like this:

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

The executed SQL will be like:

```sql
select distinct customer0_.* from customer customer0_ 
inner join orders orders1_ on customer0_.id=orders1_.order_id 
inner join orders_tags tags2_ on orders1_.id=tags2_.order_id 
inner join tag tag3_ on tags2_.tags_id=tag3_.id 
where tag3_.name in (?)
```

`@Joins` can also be used at the class level, allowing all fields within the same object to use the [`alias`](#alias) for additional conditions on the joined entity, for example:

```java
@Data
@Joins({
  @Join(path = "orders", alias = "o"),
  @Join(path = "o.tags", alias = "t")
})
public class CustomerOrderCriteria {

  @Spec(path = "o.itemName", value = In.class)
  Collection<String> items;

  @Spec(path = "t.name", value = In.class)
  Collection<String> tags;
}
```

### Repeatable

`@Join` supports repeatable declarations and can be used multiple times directly instead of using `@Joins`:

```java
@Joins({
  @Join(path = "orders", alias = "o"),
  @Join(path = "o.tags", alias = "t")
})
public class CustomerOrderCriteria {}
```

This is equivalent to the following:

```java
@Join(path = "orders", alias = "o")
@Join(path = "o.tags", alias = "t")
public class CustomerOrderCriteria {}
```

### Joins Order

Annotations are processed in order, so you must define `@Joins` in the correct sequence.

For example, the following definition is incorrect:

```java
@Data
class CustomerOrderTagCriteria {

  @Join(path = "o.tags", alias = "t") // "o" alias will be not exist during processing this @Join
  @Join(path = "orders", alias = "o")
  @Spec(path = "t.name", value = In.class)
  Collection<String> tagNames;
}
```

## Alias

The usage rules for `@Join#alias` are as follows:

- If not provided, the default alias is `@Join#path`
- If it contains `.`, it will be replaced with `_`

For example:

```java
@Join(path = "orders") // default alias is "orders"
@Join(path = "orders.tags") // default alias is "orders_tags"
@Spec(path = "orders_tags.name", value = In.class)
```

### Reusing the Same Alias

If multiple fields declare `@Join` with the same alias,  
they will **share one join** in the SQL. All conditions are applied to the **same joined row**.


For example:

```java
@Data
public class CustomerOrderCriteria {

  @Join(path = "orders", alias = "o") // same alias
  @Spec(path = "o.id")
  Long orderId;

  @Join(path = "orders", alias = "o") // same alias
  @Spec(path = "o.itemName", value = Like.class)
  String itemName;
}
```

The executed SQL will be like:

```sql
select distinct customer0_.*
from customer customer0_
inner join orders o1_ on customer0_.id = o1_.order_id -- single join reused
where o1_.id = ?
  and o1_.item_name like ? -- both conditions apply on same row
```

### Same Path with Different Aliases

If the same path is declared with **different aliases**,  
multiple joins will be generated in the SQL. Each join may match **different rows** of the association.

For example:

```java
@Data
public class CustomerOrderCriteria {

  @Join(path = "orders", alias = "o1") // alias = o1
  @Spec(path = "o1.id")
  Long orderId;

  @Join(path = "orders", alias = "o2") // alias = o2
  @Spec(path = "o2.itemName", value = Like.class)
  String itemName;
}
```

The executed SQL will be like:

```sql
select distinct customer0_.*
from customer customer0_
inner join orders o1_ on customer0_.id = o1_.order_id -- first join
inner join orders o2_ on customer0_.id = o2_.order_id -- second join
where o1_.id = ? -- condition on o1
  and o2_.item_name like ? -- condition on o2
```

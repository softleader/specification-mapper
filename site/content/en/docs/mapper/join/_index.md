---
title: Join
weight: 50
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

### Joins Order

Annotations are processed in order, so you must define `@Joins` in the correct sequence.

For example, the following definition is incorrect:

```java
@Data
class CustomerOrderTagCriteria {

  @Joins({
    @Join(path = "o.tags", alias = "t"), // "o" alias will be not exist during processing this @Join
    @Join(path = "orders", alias = "o")
  })
  @Spec(path = "t.name", value = In.class)
  Collection<String> tagNames;
}
```

## Alias

The usage rules for `@Join#alias` are as follows:

- It is shared within the same POJO
- It cannot be declared multiple times within the same POJO
- If not provided, the default alias is `@Join#path`
- If it contains `.`, it will be replaced with `_`

For example:

```java
@Joins({
  @Join(path = "orders"), // default alias is "orders"
  @Join(path = "orders.tags") // default alias is "orders_tags"
})
@Spec(path = "orders_tags.name", value = In.class)
```

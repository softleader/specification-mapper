---
title: Join
weight: 50
description: > 
  關聯查詢的過濾條件
---

在 POJO 中, 你可以在 Field 或 Class 上使用 `@Join` 來過濾關聯的 Entity

## Single Join

要使用 `@Join`, 在 Entity 中需要先定義好關聯, 例如, 有個客戶 Entity, 會一對多的關聯訂單 Entity:

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

如果你想要查詢買了指定東西的客戶, 則可以定義 POJO 如下:

```java
@Data
public class CustomerOrderCriteria {

  @Join(path = "orders", alias = "o")
  @Spec(path = "o.itemName", value = In.class)
  Collection<String> items;
}
```

執行的 SQL 會類似:

```sql
select distinct customer0_.* from customer customer0_ 
inner join orders orders1_ on customer0_.id=orders1_.order_id 
where orders1_.item_name in (? , ?)
```

`@Join` 也可以用在 class 層級, 在同一個物件內的欄位就都可以使用 [`alias`](#alias)  來對 Join 的對象增加條件, 例如:

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

為了比較符合大部分的使用情境, 以下是預設的行為:

- Join type 預設為 `INNER`
- 將結果排除重複 (`distinct`)

透過設定 `@Join#joinType` 或 `@Join#distinct` 可以改變預設行為, 如:

```java
@Join(joinType = JoinType.RIGHT, distinct = false)
```

## Multi Joins

你可以使用 `@Joins` 來定義多層級的 Join, 例如, 在剛剛的訂單 Entity 中, 還會多對多的關聯到類別 Entity:

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

如果你想要查詢買了指定所屬類別的東西的客戶, 則可以定義 POJO 如下:

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

執行的 SQL 會類似:

```sql
select distinct customer0_.* from customer customer0_ 
inner join orders orders1_ on customer0_.id=orders1_.order_id 
inner join orders_tags tags2_ on orders1_.id=tags2_.order_id 
inner join tag tag3_ on tags2_.tags_id=tag3_.id 
where tag3_.name in (?)
```

`@Joins` 也是可以用在 class 層級, 在同一個物件內的欄位就都可以使用 [`alias`](#alias) 來對 Join 的對象增加條件, 例如:

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

`@Join` 支援重複宣告, 可用直接用多個 `@Join` 代替 `@Joins`:

```java
@Joins({
  @Join(path = "orders", alias = "o"),
  @Join(path = "o.tags", alias = "t")
})
public class CustomerOrderCriteria {}
```

與以下寫法一樣:

```java
@Join(path = "orders", alias = "o")
@Join(path = "o.tags", alias = "t")
public class CustomerOrderCriteria {}
```

### Joins Order

Annotation 的處理是有順序性的, 因此必須依照 Join 的順序去定義 `@Joins`

例如依照上面的情境, 下列的定義順序是錯誤的:

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

`@Join#alias` 的使用規則如下:

- 在同個 POJO 中是共用的
- 在同的 POJO 中不可重複宣告
- 若沒提供, 預設使用 `@Join#path`
- 若包含了 `.` 會以 `_` 取代之

例如:

```java
@Join(path = "orders") // alias 預設為 orders
@Join(path = "orders.tags") // alias 預設為 orders_tags
@Spec(path = "orders_tags.name", value = In.class)
```

---
title: Join Fetch
weight: 60
---

在 POJO 中, 你可以在 Field 或 Class 上使用 `@JoinFetch` 來過濾關聯的 Entity, 跟 [`@Join`](/docs/mapper/join) 的差別是, 這可以一次撈出所有 Lazy 的關聯資料

## Single Level Fetch

例如, 有個客戶 Entity, 會一對多的關聯訂單 Entity:

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

如果你想在取得 Customer 時就順便取得 Order, 則:

```java
@Data
@JoinFetch(paths = "orders")
class CustomerOrderCriteria {

  @Spec
  String name;
}
```

執行的 SQL 會類似:

```sql
select distinct 
  customer0_.* ...,
  orders1_.* ...
from customer customer0_ 
inner outer join orders orders1_ on customer0_.id=orders1_.order_id 
where customer0_.name=?
```

> 你可以看到 `orders1_.*` 也被放入了 select 項目內

### Join Type

為了比較符合大部分的使用情境, 預設的 Join type 是 `INNER`, 也會將結果排除重複 (*distinct*), 你可以設定 `@FetchJoin#joinType` 或 `@FetchJoin#distinct` 來改變, 如:

```java
@FetchJoin(joinType = JoinType.RIGHT, distinct = false)
```

### With Clause

`@JoinFetch` 也可以讓你過濾資料, 例如, 我想要過濾客戶名稱及訂單名稱, 則可以定義 POJO 如下:

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

執行的 SQL 會類似:

```sql
select distinct 
  customer0_.* ...,
  orders1_.* ...
from customer customer0_ 
inner outer join orders orders1_ on customer0_.id=orders1_.order_id 
where customer0_.name=? and orders1_.item_name in (?)
```

`@JoinFetch` 也可以用在 Field 層級, 例如:

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

你可以使用 `@JoinFetches` 來定義多層級的 Fetch, 例如, 在剛剛的訂單 Entity 中, 還會多對多的關聯到類別 Entity:

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

  @JoinFetches({
    @JoinFetch(path = "orders", alias = "o"),
    @JoinFetch(path = "o.tags", alias = "t")
  })
  @Spec(path = "t.name", value = In.class)
  Collection<String> tags;
}
```

執行的 SQL 會類似:

```sql
select distinct 
  customer0_.* ...,
  orders1_.* ...,
  tag3_.* ...
from customer customer0_ 
inner join orders orders1_ on customer0_.id=orders1_.order_id 
inner join orders_tags tags2_ on orders1_.id=tags2_.order_id 
inner join tag tag3_ on tags2_.tags_id=tag3_.id 
where tag3_.name in (?)
```

`@JoinFetches` 也是可以用在 class 層級, 在同一個物件內的欄位就都可以使用 `alias` 來對 Join 的對象增加條件, 例如:

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

Annotation 的處理是有順序性的, 因此必須依照 Join 的順序去定義 `@JoinFetches`

例如依照上面的情境, 下列的定義順序是錯誤的:

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

`@JoinFetch#alias` 的使用規則如下:

- 在同個 POJO 中是共用的
- 在同的 POJO 中不可重複宣告
- 若沒提供, 預設使用 `@JoinFetch#path`
- 若包含了 `.` 會以 `_` 取代之

例如:

```java
@JoinFetches({
  @JoinFetch(path = "orders"), // alias 預設為 orders
  @JoinFetch(path = "orders.tags") // alias 預設為 orders_tags
})
@Spec(path = "orders_tags.name", value = In.class)
```

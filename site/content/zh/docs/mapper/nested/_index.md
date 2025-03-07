---
title: Nested Specs
weight: 40
---

在 POJO 中, 你可以在 Field 上使用 `@NestedSpec` 來告知 `SpecMapper` 要往下一層物件 (Nested Object) 去組合 Specification,  這是沒有層級限制的, 可以一直往下找!

例如我有一個共用的 `AddressCriteria` POJO, 我就可以將它掛載到其他的 POJO 中, 程式碼範例如下:

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

執行的 SQL 會類似:

```
... where x.firstname like %?% and ( x.county=? or x.city=? )
```

## Combine Nested Specs

你也可以在 Nested Object 的欄位上宣告 `@And` 或 `@Or` 來控制結果要怎麼跟其他的欄位組合, 詳細的說明請參考 [Combining on Fields](/docs/mapper/combine/#combine-on-fields).

舉個例子如下:

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

執行的 SQL 會類似:

```
... where (x.firstname like ?) or x.county=? and x.city=?
```

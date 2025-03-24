---
title: Limitation
weight: 90
description: > 
  限制與考量
---

`SpecMapper` 在找 POJO 的欄位時, 只會找當前 Class 的 Local Field, 而不去往上找 Hierarchy Classes 的 Field, 如果你共用的欄位想要用在多個 POJO, 請考慮使用 [Nested Specs](/docs/mapper/nested) 方式

---
title: Limitation
weight: 90
description: > 
  Constraints and considerations
---

When `SpecMapper` searches for fields in a POJO, it only looks for local fields within the current class and does not traverse the hierarchy of classes to find fields. If you have shared fields that you want to use in multiple POJOs, consider using the [Nested Specs](/docs/mapper/nested/) approach.

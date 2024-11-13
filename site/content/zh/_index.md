---
title: Specification Mapper
---

{{% blocks/lead color="primary" %}}
```
   ____             _ ____          __  _                
  / __/__  ___ ____(_) _(_)______ _/ /_(_)__  ___        
 _\ \/ _ \/ -_) __/ / _/ / __/ _ `/ __/ / _ \/ _ \       
/___/ .__/\__/\__/_/_//_/\__/\_,_/\__/_/\___/_//_/       
   /_/                     /  |/  /__ ____  ___  ___ ____
                          / /|_/ / _ `/ _ \/ _ \/ -_) __/
                         /_/  /_/\_,_/ .__/ .__/\__/_/   
                                    /_/  /_/             
```
<div>
{{% /blocks/lead %}}

{{% blocks/lead color="dark" %}}
Specification binding API for Spring Data JPA
{{% /blocks/lead %}}

{{< blocks/section color="white" type="row" >}}

{{% blocks/feature icon="fa fa-magic" title="Dynamic Query Generation" %}}
自動讀取物件欄位並基於註解生成查詢條件, 節省時間並減少客製查詢程式碼的需求
{{% /blocks/feature %}}

{{% blocks/feature icon="fa-solid fa-leaf" title="Integration with Spring Boot" %}}
通過 specification-mapper-starter 與 Spring Boot 無需配置即可整合, 輕鬆在 Spring 應用程式中設置和使用
{{% /blocks/feature %}}

{{% blocks/feature icon="fa-solid fa-filter" title="Flexible Skipping Strategy" %}}
在轉換過程中會忽略為 `null`、空值或缺乏註解的欄位, 有效管理查詢條件並減少冗餘條件
{{% /blocks/feature %}}

{{% blocks/feature icon="fa-solid fa-tags" title="Customizable Annotations" %}}
透過 `@Spec` 註解自訂查詢行為, 包括 Equals、Like、Between 等, 使其適應各種查詢需求
{{% /blocks/feature %}}

{{% blocks/feature icon="fa-solid fa-code" title="Support for Custom Specifications" %}}
允許使用自訂 `@Spec` 實作複雜查詢, 例如子查詢, 滿足應用程式的特定需求
{{% /blocks/feature %}}

{{% blocks/feature icon="fa-solid fa-layer-group" title="Combining Specifications" %}}
透過 `@And` 和 `@Or` 註解在類別和欄位層級組合規格, 靈活控制查詢邏輯, 以滿足複雜篩選需求
{{% /blocks/feature %}}

{{< /blocks/section >}}

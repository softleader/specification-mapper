---
title: Specification Mapper
llms_summary: "Specification Mapper 是一套協助建構 Spring Data JPA Specifications 的工具，提供注解式、可擴充且可讀性高的查詢條件組合能力。"
---

{{% blocks/lead color="primary" %}}
<pre tabindex="0"><code>
 __                                                    
/ _\_ __   ___  ___  /\/\   __ _ _ __  _ __   ___ _ __ 
\ \| '_ \ / _ \/ __|/    \ / _` | '_ \| '_ \ / _ \ '__|
_\ \ |_) |  __/ (__/ /\/\ \ (_| | |_) | |_) |  __/ |   
\__/ .__/ \___|\___\/    \/\__,_| .__/| .__/ \___|_|   
   |_|                          |_|   |_|              
</code></pre>
<div>
{{% /blocks/lead %}}

{{% blocks/lead color="dark" %}}
Specification binding API for Spring Data JPA
{{% /blocks/lead %}}

<div class="container">
  <section>
    <p class="lead mt-5"><h2>What is Specification Mapper?</h2></p>
    <p>Specification Mapper 是一個協助構建 <a href='https://docs.spring.io/spring-data/jpa/reference/jpa/specifications.html'>Specifications</a> 的工具, 它會讀取 POJO 的欄位, 並透過簡單且易記的註解, 將這些欄位的值轉換成 Specifications, 此外, Specification Mapper 提供多個擴展點, 以可以輕鬆擴展並實現自定義邏輯</p>
    <p>在 Clean Architecture 架構, Specification Mapper 提供了一種方便的方式, 可以在任一 Layer 中轉換 POJO, 例如, 在 infrastructure layer 中, 可以使用 Specification Mapper 將 Domain Object 轉換為 Specifications, 透過使用 Specification Mapper, 可以有效地管理層之間的依賴性, 實現清晰且有條理的程式架構, 增強代碼的可讀性和可維護性</p>
  </section>
</div>

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

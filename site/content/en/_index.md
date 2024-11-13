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
Automatically reads object fields and generates query conditions based on annotations, saving time and reducing the need for custom query code.
{{% /blocks/feature %}}

{{% blocks/feature icon="fa-solid fa-leaf" title="Integration with Spring Boot" %}}
With specification-mapper-starter, integration with Spring Boot requires no configuration, making it easy to set up and start using in Spring applications.
{{% /blocks/feature %}}

{{% blocks/feature icon="fa-solid fa-filter" title="Flexible Skipping Strategy" %}}
Fields that are `null`, empty, or lack annotations are ignored during conversion, allowing for efficient query condition management and reducing redundant criteria.
{{% /blocks/feature %}}

{{% blocks/feature icon="fa-solid fa-tags" title="Customizable Annotations" %}}
Define specific query behavior with various `@Spec` annotations like Equals, Like, and Between, making it highly adaptable to different querying needs.
{{% /blocks/feature %}}

{{% blocks/feature icon="fa-solid fa-code" title="Support for Custom Specifications" %}}
Enables extending `@Spec` with custom implementations, allowing users to handle complex queries, such as subqueries, tailored to specific application requirements.
{{% /blocks/feature %}}

{{% blocks/feature icon="fa-solid fa-layer-group" title="Combining Specifications" %}}
With `@And` and `@Or` annotations, specifications can be combined at both class and field levels, offering precise control over query logic for complex filtering needs.
{{% /blocks/feature %}}

{{< /blocks/section >}}

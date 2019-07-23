---
title: "API"
---

## Concepts
The core of Casquatch is the object based API. Full documentation can be seen at [CasquatchDao]({{% api "CasquatchDao" %}}). The concept is that objects are generated from database metadata via the [Code Generator]({{< ref "codegenerator.md" >}}) then they can be passed generically to apis such as [getById(class,object)]({{% api "CasquatchDao" "getById-java.lang.Class-T-" %}}) or [save(class,object)]({{% api "CasquatchDao" "save-java.lang.Class-T-" %}})

## Query Options
APIs are all overloaded to additionally take [QueryOptions]({{% api "QueryOptions" %}}) object to specify items such as consistency or limit. See [Query Options]({{< ref "queryoptions.md" >}}) for more information

## Example
### GetById
{{< highlight java >}}
TableName object = db.getById(TableName.class, new TableName(1,2););
{{< /highlight >}}

### GetAllById
{{< highlight java >}}
List<TableName> objectList = db.getAllById(TableName.class, new TableName(1););
{{< /highlight >}}

### Save
{{< highlight java >}}
TableName object = new TableName(1,2);
db.save(TableName.class, object);
{{< /highlight >}}

### Delete
{{< highlight java >}}
TableName object = new TableName(1,2);
db.delete(TableName.class, object);
{{< /highlight >}}
  
---
title: "Rest API"
---

## Concepts
A spring rest controller can be generated automatically tieing in to core Casquatch APIs

## Implementation
Implementation requires a boolean flag set on [CasquatchSpring]({{% api "annotations/CasquatchSpring" %}}) as follows
{{< highlight java >}}
@CasquatchSpring(generateRestApi=true)
{{< /highlight >}}

## Example
See [Spring Rest]({{< ref "examples/springrest.md" >}}) for a working implementation
# EC2 Schema #

Schema to query EC2 objects as
[doctest](https://github.com/tkawachi/sbt-doctest).

```scala
scala> import social.midas.discovery.common.SchemaFinder
scala> import social.midas.discovery.schema.Ec2Schema
scala> val root = SchemaFinder.makeRoot(List(Ec2Schema))
root: sangria.schema.Schema[social.midas.discovery.common.AbstractContext,Unit] = ...

scala> import sangria.renderer.SchemaRenderer
scala> SchemaRenderer.renderSchema(root)
res0: String =
schema {
  query: RootObject
}
<BLANKLINE>
input Ec2Filter {
  name: String!
  values: [String!]!
}
<BLANKLINE>
"Describes an EC2 instance."
type Ec2Instance {
  privateIpAddress: String
  id: String!
}
<BLANKLINE>
type RootObject {
  "The list of EC2 instances."
  ec2Instances(
    "Provide filters on a EC2 query."
    filters: [Ec2Filter!]): [Ec2Instance!]!
}
```

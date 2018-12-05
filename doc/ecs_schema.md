# ECS Schema #

Schema to query ECS objects as
[doctest](https://github.com/tkawachi/sbt-doctest).

```scala
scala> import social.midas.discovery.common.SchemaFinder
scala> import social.midas.discovery.schema.EcsSchema
scala> val root = SchemaFinder.makeRoot(List(EcsSchema))
root: sangria.schema.Schema[social.midas.discovery.common.AbstractContext,Unit] = ...

scala> import sangria.renderer.SchemaRenderer
scala> SchemaRenderer.renderSchema(root)
res0: String =
schema {
  query: RootObject
}
<BLANKLINE>
"An AWS ECS cluster."
type EcsCluster {
  arn: String!
<BLANKLINE>
  "The list of services of this cluster matching filterArn if specified."
  services(
    "Filter ARNs by matching to a regular expression."
    filterArn: String): [EcsServiceArn!]!
<BLANKLINE>
  "The list of tasks of this cluster."
  tasks(
    "Filter ARNs by matching to a regular expression."
    filterArn: String,
<BLANKLINE>
    "The name of the family with which to filter the tasks results."
    family: String): [EcsTask!]!
}
<BLANKLINE>
"An ECS container"
type EcsContainer {
  arn: String!
  clusterArn: String!
  networkBindings: [EcsNetworkBinding!]!
}
<BLANKLINE>
"Details on the network bindings between a container and its host container instance."
type EcsNetworkBinding {
  protocol: String!
  bindIp: String!
  containerPort: Int!
  hostPort: Int!
}
<BLANKLINE>
"An ECS service"
type EcsServiceArn {
  arn: String!
  clusterArn: String!
}
<BLANKLINE>
"An ECS task"
type EcsTask {
  arn: String!
  clusterArn: String!
<BLANKLINE>
  "The containers associated with the task."
  containers: [EcsContainer!]!
<BLANKLINE>
  "The name of the task group associated with the task."
  group: String!
}
<BLANKLINE>
type RootObject {
  "The list of available ECS clusters."
  ecsClusters(
    "Filter ARNs by matching to a regular expression."
    filterArn: String): [EcsCluster!]!
}
```

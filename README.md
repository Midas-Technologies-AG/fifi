[![Maven Central](https://maven-badges.herokuapp.com/maven-central/social.midas/discovery-common_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/social.midas/discovery-common_2.12) 

# Service Discovery Library #

Well, the title says it all: this library is about service discovery
in general. At the moment it only supports discovering IP addresses
for AWS EC2 and ECS, but expect this to expand in the near future.

Discovery statement are [GraphQL](https://graphql.org/) queries which
can be defined in your [configuration
file](https://github.com/lightbend/config). Here is an example to
fetch the private IP addresses of all your EC2 instances:

```graphql
query {
    ec2Instances {
        privateIpAddress
    }
}
```

There are filters on almost every level. See the
[EC2](doc/ec2_schema.md) and [ECS schema doctest](doc/ecs_schema.md).

Use extractors (e.g. the
[Ip4Extractor](https://oss.sonatype.org/service/local/repositories/releases/archive/social/midas/discovery-common_2.12/0.3.3/discovery-common_2.12-0.3.3-javadoc.jar/!/social/midas/discovery/common/Ip4Extractor$.html))
to get back the bits that interest you from the query. Define both in
your configuration file like this:

```
discovery.aws.region = "eu-central-1"
discovery.extractors = [ "social.midas.discovery.common.Ip4Extractor$" ]
discovery.query = """
query {
  ecsClusters {
    tasks {
      containerInstance {
        ec2Instance {
          privateIpAddress
        }
      }
    }
  }
}
"""
```

Then you can simply use `discoverFromConfig`
([doc](https://oss.sonatype.org/service/local/repositories/releases/archive/social/midas/discovery-common_2.12/0.3.3/discovery-common_2.12-0.3.3-javadoc.jar/!/social/midas/discovery/common/index.html))
to get back all IP addresses of your ECS machines.

## Library Structure ##

The library is split into several parts:
  * `discovery-common` -- common functionality and main interface
    ([doc](https://oss.sonatype.org/service/local/repositories/releases/archive/social/midas/discovery-common_2.12/0.3.3/discovery-common_2.12-0.3.3-javadoc.jar/!/social/midas/discovery/common/index.html)),
  * `discovery-common-aws` -- functionality specific for AWS
    ([doc](https://oss.sonatype.org/service/local/repositories/releases/archive/social/midas/discovery-common-aws_2.12/0.3.3/discovery-common-aws_2.12-0.3.3-javadoc.jar/!/social/midas/discovery/common/aws/index.html)),
  * `discovery-aws-ec2` -- defines the [EC2 schema](doc/ec2_schema.md)
    ([doc](https://oss.sonatype.org/service/local/repositories/releases/archive/social/midas/discovery-aws-ec2_2.12/0.3.3/discovery-aws-ec2_2.12-0.3.3-javadoc.jar/!/social/midas/discovery/index.html)),
  * `discovery-aws-ecs` -- defines the [ECS schema](doc/ecs_schema.md)
    ([doc](https://oss.sonatype.org/service/local/repositories/releases/archive/social/midas/discovery-aws-ecs_2.12/0.3.3/discovery-aws-ecs_2.12-0.3.3-javadoc.jar/!/social/midas/discovery/index.html)).

As ECS runs on EC2 you currently get the whole package by depending on
`discovery-aws-ecs`. The schemata get discovered automatically when
they are in the classpath.

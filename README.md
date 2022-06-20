<p align="center">
    <a href="https://github.com/yaml-path/YamlPath/graphs/contributors" alt="Contributors">
        <img src="https://img.shields.io/github/contributors/yaml-path/YamlPath"/></a>
    <a href="https://github.com/yaml-path/YamlPath/pulse" alt="Activity">
        <img src="https://img.shields.io/github/commit-activity/m/yaml-path/YamlPath"/></a>
    <a href="https://github.com/yaml-path/YamlPath/actions/workflows/push.yaml" alt="Build Status">
        <img src="https://github.com/yaml-path/YamlPath/actions/workflows/push.yaml/badge.svg"></a>
    <a href="https://github.com/yaml-path/YamlPath" alt="Coverage">
        <img src=".github/badges/jacoco.svg"></a>
</p>

# YAML-Path Expression Language Parser

**A Java DSL for reading YAML documents and replacing values.**

YamlPath is available at [the Maven Central Repository](https://search.maven.org/search?q=a:yaml-path%20AND%20g:org.yamlpath). To use it, simply declare the following dependency part of your pom file:

```xml
<dependency>
    <groupId>org.yamlpath</groupId>
    <artifactId>yaml-path</artifactId>
    <version>${latest version in Maven Central</version>
</dependency>
```

## Usage

The simplest most straight forward way to use YamlPath is via the static API.

```java
String yaml = "apiVersion: v1\n" 
            + "kind: Service\n" 
            + "metadata:\n" 
            + "  name: helm-on-kubernetes-example\n";

String name = YamlPath.from(yaml).readSingle("metadata.name");
```

## Replacements 

Using the YamlPath parser, apart from reading values, we can also overwrite values and then dump the updated resources as string.

For example, given the following yaml file:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: example
```

If we write the following code:

```java
String newYamlContent = YamlPath.from(file)
        .write("metadata.name", "another-name!")
        .dumpAsString();
// Output of newYamlContent is:
//        ---
//        apiVersion: apps/v1
//        kind: Deployment
//        metadata:
//          name: another-name!
```

## Wildcard: map properties at any level

If we want to map a property that is placed at a very depth level, for example, the `containerPort` property:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: example
spec:
  replicas: 3
  selector:
    matchLabels:
      app.kubernetes.io/name: example
  template:
    metadata:
        app.kubernetes.io/name: example
    spec:
      containers:
        - env:
            - name: KUBERNETES_NAMESPACE
              valueFrom:
                fieldRef:
                  fieldPath: metadata.namespace
          name: example
          ports:
            - containerPort: 8080 // we want to map this property!
              name: http
              protocol: TCP
```

If we want to map the property `containerPort`, we would need to write all the parent properties by using the path expression "spec.template.spec.containers.ports.containerPort".

And what about if the `containerPort` property is at one place in the Deployment resources, but at another place in the DeploymentConfig resources? We would need to provide two expressions.

To ease up this use case, we can use wildcards. For example, using the expression "*.ports.containerPort", it would map all the container ports at any level.

## Escape characters

If you want to select properties which key contains special characters like '.', you need to escape them using `'`, for example, using the path expression "".

```java
YamlPath.from(yaml).read("spec.selector.matchLabels.'app.kubernetes.io/name'");
```

## Filter

If you want to only map properties of certain resource type, you can add as many conditions you need in the path such as: "(kind == Service).metadata.name".

Additionally, we can write the filter including the "and" operator using "&&" or the "or" operator using "||":

```java
// To map the property only for Service resources AND resources that has an annotation 'key' with value 'some' 
YamlPath.from(yaml).read("(kind == Service && metadata.annotations.'key' == 'some.text').metadata.name");

// To map the property only for either Deployment OR DeploymentConfig resources 
YamlPath.from(yaml).read("(kind == Deployment || kind == DeploymentConfig).metadata.name");
```

Also, filters can be placed at any place in the path expression and also at multiple times. Let's see an example of this: we want to map the container port of containers with name `example` and only for Deployment resources:

```
## To map the property only for Deployment resource AND containers with a concrete name 
YamlPath.from(yaml).read("(kind == Deployment).spec.template.spec.containers.(name == example).ports.containerPort");
```

## What is not supported using path expressions?

- We can't use regular expressions.
- We can't write complex filters that involves AND/OR conditions. For example: the filter `(kind == Deployment && kind == DeploymentConfig || name == example)` is not supported.
- We can't select elements by index. For example, if we want to map the second container, we can't do: `spec.template.spec.containers.2.ports.containerPort`.

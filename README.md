# memjc

When a java file is compiled, memjc outputs a class file on a memory file system.

## Requirements

Java >= 1.7

## Build

gradle is installed
```sh
$ gradle build
```
gradle is not installed
```sh
$ gradlew build
```

## Usage

```sh
$ memjc Test.java

// output class file
$ memjc -memjc-out Test.java
```

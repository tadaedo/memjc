# memjc

memjc outputs a class file on a memory file system.

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

## Usage(ver1.1)

```
Usage: memjc <memjc options> <javac options> <source files>
Version: 1.1
memjc Options:
  -Mhelp Show usage
  -Mout Output class file
  -Mmain:<classname>[:<arg1>:<arg2>...] main class
  -Mcp:<classpath>[;<classpath1>;<classpath2>...] java class path (Windows)
  -Mcp:<classpath>[:<classpath1>:<classpath2>...] java class path (Other)
```

```sh
# compile
$ memjc Test.java

# compile and output class file
$ memjc -Mout Test.java

# compile and run
$ memjc -Mmain:Test Test.java

# args
$ memjc -Mmain:Test:arg1:arg2:arg3 Test.java

# classpath
$ memjc -Mcp:./packages/ -Mmain:org.example.Test ./packages/org/example/Test.java

# classpath(unix user directory)
# not work $ memjc -Mcp:~/packages/ -Mmain:org.example.Test ~/packages/org/example/Test.java
$ memjc -Mcp:`echo ~/packages/` -Mmain:org.example.Test ~/packages/org/example/Test.java
```

## Usage(ver1.0)

```sh
$ memjc Test.java

// output class file
$ memjc -memjc-out Test.java
```

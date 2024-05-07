jobj
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.jobj/com.io7m.jobj.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.io7m.jobj%22)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/com.io7m.jobj/com.io7m.jobj?server=https%3A%2F%2Fs01.oss.sonatype.org&style=flat-square)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/io7m/jobj/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m-com/jobj.svg?style=flat-square)](https://codecov.io/gh/io7m-com/jobj)
![Java Version](https://img.shields.io/badge/21-java?label=java&color=e6c35c)

![com.io7m.jobj](./src/site/resources/jobj.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/jobj/main.linux.temurin.current.yml)](https://www.github.com/io7m-com/jobj/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/jobj/main.linux.temurin.lts.yml)](https://www.github.com/io7m-com/jobj/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/jobj/main.windows.temurin.current.yml)](https://www.github.com/io7m-com/jobj/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/jobj/main.windows.temurin.lts.yml)](https://www.github.com/io7m-com/jobj/actions?query=workflow%3Amain.windows.temurin.lts)|

## jobj

A parser for the useful subset of the Wavefront OBJ file format.

## Features

* Hand-written event-based recovering parser: Efficiently parse, accumulating
  errors along the way, without being tied to any particular AST types.
* High coverage test suite.
* [OSGi-ready](https://www.osgi.org/)
* [JPMS-ready](https://en.wikipedia.org/wiki/Java_Platform_Module_System)
* ISC license.

## Usage

Provide an implementation of the `JOParserEventListenerType` interface
to a `JOParserType`:

```
Path file;
JOParserEventListenerType listener;
InputStream stream;

final JOParserType p =
  JOParser.newParserFromStream(
    Optional.of(file),
    stream,
    listener
  );

p.run();
```

The `listener` will receive parse events encountered during parsing of the
file.

## Coverage

The only specification for the OBJ file format is an
[unofficial specification](src/site/resources/obj.txt) that appears to have
been handed around for decades.

Most of the OBJ file format has no relevance to anything used in modern
computer graphics in 2024, but the format itself is often used as a
bare-minimum portable text format for distributing mesh data. This parser
attempts to capture the useful subset of data and makes no attempt to parse
the entirety of the OBJ format. It's practically guaranteed that the parser
will be missing the once piece of data you actually wanted to extract from the
`.obj` file you're parsing. Patches to increase format coverage are welcome.


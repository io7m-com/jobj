jobj
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.jobj/com.io7m.jobj.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.io7m.jobj%22)
[![Maven Central (snapshot)](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fcom%2Fio7m%2Fjobj%2Fcom.io7m.jobj%2Fmaven-metadata.xml&style=flat-square)](https://central.sonatype.com/repository/maven-snapshots/com/io7m/jobj/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m-com/jobj.svg?style=flat-square)](https://codecov.io/gh/io7m-com/jobj)
![Java Version](https://img.shields.io/badge/17-java?label=java&color=e65cc3)

![com.io7m.jobj](./src/site/resources/jobj.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/jobj/main.linux.temurin.current.yml)](https://www.github.com/io7m-com/jobj/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/jobj/main.linux.temurin.lts.yml)](https://www.github.com/io7m-com/jobj/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/jobj/main.windows.temurin.current.yml)](https://www.github.com/io7m-com/jobj/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/jobj/main.windows.temurin.lts.yml)](https://www.github.com/io7m-com/jobj/actions?query=workflow%3Amain.windows.temurin.lts)|

## Repository Relocation

Development of this project has moved to an
[open-source but not open-contribution](https://sqlite.org/copyright.html#notopencontrib)
model.

Source code and commits will remain publicly available perpetually, but issues
and/or pull requests will be rejected and/or ignored. Additionally, this project
will now only be available via a read-only mirror at:

  https://codeberg.org/io7m-com/jobj


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


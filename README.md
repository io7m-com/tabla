tabla
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.tabla/com.io7m.tabla.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.io7m.tabla%22)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/com.io7m.tabla/com.io7m.tabla?server=https%3A%2F%2Fs01.oss.sonatype.org&style=flat-square)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/io7m/tabla/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m-com/tabla.svg?style=flat-square)](https://codecov.io/gh/io7m-com/tabla)
![Java Version](https://img.shields.io/badge/21-java?label=java&color=e6c35c)

![com.io7m.tabla](./src/site/resources/tabla.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/tabla/main.linux.temurin.current.yml)](https://www.github.com/io7m-com/tabla/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/tabla/main.linux.temurin.lts.yml)](https://www.github.com/io7m-com/tabla/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/tabla/main.windows.temurin.current.yml)](https://www.github.com/io7m-com/tabla/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/tabla/main.windows.temurin.lts.yml)](https://www.github.com/io7m-com/tabla/actions?query=workflow%3Amain.windows.temurin.lts)|

## tabla

### Features

  * Uses a generic constraint solver for table layouts.
  * Written in pure Java 21.
  * [OSGi](https://www.osgi.org/) ready.
  * [JPMS](https://en.wikipedia.org/wiki/Java_Platform_Module_System) ready.
  * ISC license.
  * High-coverage automated test suite.

### Building

```
$ mvn clean verify
```

### Usage

```
var builder =
  Table.builder()
    .declareColumn("Item")
    .declareColumn("Description");

builder.addRow()
  .addCell("ca146625-5b90-4478-8ca3-51bce50f4d07")
  .addCell("Structural documentation.")

...
// Add more rows here.
...

var table =
  builder.build();

var lines =
  Tabla.framedUnicodeRenderer()
    .renderLines(table);

for (var line : lines) {
  System.out.println(line);
}
```

```
┌──────────────────────────────────────┬────────────────────────────────────────────┐
│ Item                                 │ Description                                │
├──────────────────────────────────────┼────────────────────────────────────────────┤
│ ca146625-5b90-4478-8ca3-51bce50f4d07 │ Structural documentation.                  │
├──────────────────────────────────────┼────────────────────────────────────────────┤
│ 8df7d45d-a6cd-4bd9-8e40-daa1f38d2835 │ A small jar containing a pickled beetroot. │
├──────────────────────────────────────┼────────────────────────────────────────────┤
│ 6253ab7f-2f0f-4961-9b9d-164b462da266 │ Assorted coat hangers.                     │
├──────────────────────────────────────┼────────────────────────────────────────────┤
│ 577cfd36-7d2d-41db-a9b9-cf4d4ced9160 │ A silver, decorative clock.                │
└──────────────────────────────────────┴────────────────────────────────────────────┘
```



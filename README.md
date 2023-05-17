[![License](https://img.shields.io/github/license/i-net-software/sass-compiler.svg)](https://github.com/i-net-software/sass-compiler/blob/master/LICENSE.txt)
[![JitPack](https://jitpack.io/v/i-net-software/sass-compiler.svg)](https://jitpack.io/#i-net-software/sass-compiler/master-SNAPSHOT)

A pure Java implementation of the http://sass-lang.com compiler with the target to compile the [Bootstrap 5 framework](https://github.com/twbs/bootstrap).

Tests
=====
There is an extensive test suite consisting of source scss input and expected 
css output imported from the sass-lang project. Today the features required by
the tests in src/test/resources/sasslang are fully implemented while the
tests in src/test/resources/sasslangbroken are currently expected to fail.

Contributing
====
Your contributions are more than welcome. For example, if you want 
to compile sources other than Bootstrap. Every PR should also contain tests.

Testing Snapshot
====
If you want test the latest snapshot then you can checkout or use [JitPack](https://jitpack.io/#i-net-software/sass-compiler).

For example with Gradle:
```
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.i-net-software:sass-compiler:master-SNAPSHOT'
}
```

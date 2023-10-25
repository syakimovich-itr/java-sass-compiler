[![Build with Java 8](https://github.com/i-net-software/sass-compiler/actions/workflows/build8.yml/badge.svg)](https://github.com/i-net-software/sass-compiler/actions/workflows/build8.yml)
[![Build with Java 11](https://github.com/i-net-software/sass-compiler/actions/workflows/build11.yml/badge.svg)](https://github.com/i-net-software/sass-compiler/actions/workflows/build11.yml)
[![License](https://img.shields.io/github/license/i-net-software/sass-compiler.svg)](https://github.com/i-net-software/sass-compiler/blob/master/LICENSE.txt)
[![Maven](https://img.shields.io/maven-central/v/de.inetsoftware/sass-compiler.svg)](https://mvnrepository.com/artifact/de.inetsoftware/sass-compiler)
[![JitPack](https://jitpack.io/v/i-net-software/sass-compiler.svg)](https://jitpack.io/#i-net-software/sass-compiler/master-SNAPSHOT)

A pure Java implementation of the http://sass-lang.com compiler with the target to compile the scss sources of the [Bootstrap 5 framework](https://github.com/twbs/bootstrap) framework. 

You can find the project's [homepage](https://github.com/i-net-software/sass-compiler) on GitHub. 

Dependencies
====
No dependencies to other libraries are needed.

```
repositories {
    mavenCentral()
}

dependencies {
    implementation 'de.inetsoftware:sass-compiler:+'
}
```


Using
====
```
import com.inet.sass.ScssStylesheet;
import com.inet.sass.handler.SCSSErrorHandler;
import com.inet.sass.resolver.ScssStylesheetResolver;
...
ScssStylesheetResolver resolver = new FilesystemResolver( StandardCharsets.UTF_8 );
SCSSErrorHandler errorHandler = new SCSSErrorHandler() {
...
};
String scssFileName = ...;
ScssStylesheet scss = ScssStylesheet.get( scssFileName, errorHandler, resolver );
scss.compile( ScssContext.UrlMode.ABSOLUTE );
String css = scss.printState();
...
```

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


How to install:

You need to copy the gradle.properties-template to gradle.properties and edit it. Else Gradle will complain
about missing properties.

You need to install gradle 0.9

http://www.gradle.org

And execute the following command

gradle install

The Multiverse jar has no external dependencies. It uses velocity (pre) compile-time to generate Java sources, but
there is no runtime dependency on Velocity or any other library.

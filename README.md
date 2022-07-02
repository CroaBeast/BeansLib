![Cocoa Beans](https://static.wikia.nocookie.net/minecraft_gamepedia/images/8/85/Cocoa_Beans_JE4_BE3.png/revision/latest?cb=20200127083719)
# BeansLIB
An essential library for your SpigotMC plugin. Features range from advanced gradient text formatting to time parsing.

Please note that this is **not a plugin!**

Requires Java 8. Works on MC Versions 1.8 - 1.18.

## Setup
1. Add BeansLib into your project via Maven or Gradle (look below).

2. Create a class to initialize the BeansLib integration. See this [example BeansLib class](https://github.com/CroaBeast/BeansLib/blob/master/example/MyTextClass.java) for more info.
   > You can also override almost any default BeansLib method in this class.

3. Initialize your BeansLib class (the one you create) in your main class. See this [example main class](https://github.com/CroaBeast/BeansLib/blob/master/example/MyPlugin.java) for more info.

4. Finally, you can call the methods in every class you want. See this [example class](https://github.com/CroaBeast/BeansLib/blob/master/example/ExampleClass.java) for more info.

And you're all set!

## Maven and Gradle Integration
Maven - add to pom.xml
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
```xml
<dependencies>
    <dependency>
        <groupId>com.github.CroaBeast</groupId>
        <artifactId>BeansLib</artifactId>
        <!--Replace version with the latest release version-->
        <version>RELEASE_TAG</version>
    </dependency>
</dependencies>
```

Gradle - add to build.gradle
```
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```
```
dependencies {
    implementation 'com.github.CroaBeast:BeansLib:RELEASE_TAG'
}
```

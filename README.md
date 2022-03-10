![Cocoa Beans](https://static.wikia.nocookie.net/minecraft_gamepedia/images/8/85/Cocoa_Beans_JE4_BE3.png/revision/latest?cb=20200127083719)
# BeansLIB
An essential library for your SpigotMC plugin. Features range from advanced gradient text formatting to time parsing.

Please note that this is **not a plugin!**

Requires Java 8. Works on MC Versions 1.8 - 1.18.

## Setup
1. Add BeansLib into your project via Maven or Gradle (look below).

2. In your onEnable or onLoad method include the following line:
``BeansLib.init(yourPluginInstance);``

3. Optional: Set all the text placeholder variables calling the setters of the TextUtils class also in your onEnable or onLoad method. Example:
``TextUtils.setPlayerKey("[playerName]");``

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
        <version>1.1</version>
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
    implementation 'com.github.CroaBeast:BeansLib:1.1'
}
```

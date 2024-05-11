# YASI Maven Plugin
Yet Another Sort Imports Maven Plugin

---
YASI Maven Plugin is a Maven plugin designed to automate the process of sorting imports within Java projects. It ensures consistent and organized import statements according to specified configurations, enhancing code readability and maintainability.

## Installation

YASI Maven Plugin can be easily integrated into your Maven project by adding the following configuration to your `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.oopsmash</groupId>
            <artifactId>yasi-maven-plugin</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <executions>
                <execution>
                    <goals>
                        <goal>sort</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Usage

Once the plugin is configured in your `pom.xml`, it will automatically run during the Maven build process. You can also execute it manually using the following command:

```bash
mvn com.oopsmash:yasi-maven-plugin:sort
```

## Contributing

Contributions to YASI Maven Plugin are welcome! Whether you want to report a bug, suggest a feature, or submit a pull request.

## License

YASI Maven Plugin is licensed under the [MIT License](LICENSE). Feel free to use, modify, and distribute this plugin according to the terms of the license.

<img width="656" alt="CleanShot 2022-06-14 at 21 40 58@2x" src="https://user-images.githubusercontent.com/87706822/173674995-e9edc956-33e7-44ce-a205-5bafbd253887.png">
Racoon is a wrapper for the MySQL connector.
It makes communicating with the database easier by providing a bunch of functionalities:

- Mapping query results to classes.
- Query parameter support (prepared statements)
  - Custom types
  - Indexed parameter
  - Named parameter
  - Basic types
  - Smart casting
  - List types

Racoon is developed with freedom of use in mind from the beginning.
This means that there are multiple ways to achieve the same result, so that it can be used in different contexts.
## Installation

To build the library, run the following command:

```
$ ./gradlew clean build -x test
```

To publish the library to the local Maven repository, run the following command:

```
$ ./gradlew publishToMavenLocal
```

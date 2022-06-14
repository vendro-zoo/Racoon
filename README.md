<img width="656" alt="CleanShot 2022-06-14 at 21 40 58@2x" src="https://user-images.githubusercontent.com/87706822/173674995-e9edc956-33e7-44ce-a205-5bafbd253887.png">
Racoon is a wrapper for the MySQL connector.
It makes communicating with the database easier by providing a bunch of functionalities:

- Mapping query results to classes.
- Query parameter support (prepared statements)
  - Custom types support
  - Indexed parameter support
  - Named parameter support
  - Basic types support
  - Smart cast query result to match the class field type

Racoon is developed with freedom of use in mind from the beginning.
This property of Racoon can be seen in the mapping operations that allow the use of reified generic types and runtime types.
## Installation

To build the library, run the following command:

```
$ ./gradlew clean build -x test
```

To publish the library to the local Maven repository, run the following command:

```
$ ./gradlew publishToMavenLocal
```

## Roadmap

- Basics:
  - Table column name mapping (ANNOTATIONS?) (ADDED MAPPING FUNCTIONALITY, BUT APPLIES TO ALL)
  - Get query info (number of rows, number of columns, column names, column types, ecc...)?
- Insert query support
  - Custom ID annotation
  - Update the ID field upon insertion
  - Detect ID field by name or annotation
- Update query support
- Basic cache support?
- Custom cache support?
- Pooling:
  - Possibility to disable pooling without limiting the number of connections (TO BE TESTED)
  - Remove expired connections in the pool even if are being used
  - Possibility to set the minimum number of connections in the pool???

# Racoon

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
## Roadmap

- Basics:
  - Table column name mapping (ANNOTATIONS?)
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
  - Possibility to set the minimum number of connections in the pool??? (TO BE TESTED)

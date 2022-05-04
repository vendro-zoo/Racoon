# Racoon

Racoon is a wrapper for the MySQL connector.
It makes communicating with the database easier by providing functionalities like:
- Mapping query results to classes.

Racoon is developed with freedom of use in mind from the beginning.
This property of Racoon can be seen in the mapping operations that allow the use of reified generic types and runtime types.
## Roadmap

- Query parameter support
    - Add interface for custom implementations
    - Add indexed parameter support
    - Add named parameter support
    - Add basic types support
- Insert query support
    - Custom ID annotation
    - Update the ID field upon insertion
    - Detect ID field by name or annotation
- Update query support
- Basic cache support
- Custom cache support
- Pooling?
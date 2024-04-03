# PayMyBuddy App
## UML Diagram Visual
![Image Description](src/main/resources/doc/UMLdiagram.png)

## Guide
This guide will walk you through the steps to get the PayMyBuddy project running on your local machine for development and testing purposes.

## Prerequisites

Before you begin, ensure you have the following installed on your system:

- [Git](https://git-scm.com/)
- [Podman](https://podman.io/)
- [Java JDK](https://openjdk.java.net/)
- [Maven](https://maven.apache.org/) or use the provided Maven wrapper scripts (`mvnw` or `mvnw.cmd`)

To install Podman, please refer to the official Podman installation guide.

## Database
After installing Podman (See the official [installation guide](https://podman.io/getting-started/installation)), you can run a PostgreSQL container using the following command:

```sh
podman run --name postgres-container -e POSTGRES_PASSWORD=sergtsop -d -p 5432:5432 postgres
```
This command will start a new container named postgres-container running the latest version of PostgreSQL, set the default password for the postgres user to sergtsop, and map port 5432 on the host to port 5432 on the container.

To access the database, use:

```sh
podman exec -it postgres-container psql -U postgres
```
## Running the Project
To run the project locally, you can execute:
```sh
./mvnw spring-boot:run
```

## Useful Commands

to list all running containers:
```sh
podman ps
```

to list all databases:
```sh
\list
```

The "\c" command followed by a database name within the psql command line interface is used to connect to a different database. So when you issue the following command, you're instructing psql to connect to the paymybuddy_db database.
```sh
\c paymybuddy_db
```
To list tables in db:
```sh
\dt
```
### Common SQL Commands
```sh
SELECT * FROM transaction_table;
SELECT * FROM user_table;
SELECT * FROM user_connections;
```
to exit the database:
```sh
\q
```
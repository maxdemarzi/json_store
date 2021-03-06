# json_store
Store large properties on a node as a string of json.

This project uses maven, to build a jar-file with the procedure in this
project, simply package the project with maven:

    mvn clean package

This will produce a jar-file, `target/json-store-1.0-SNAPSHOT.jar`,
that can be copied to the `plugin` directory of your Neo4j instance.

    cp target/json-store-1.0-SNAPSHOT.jar neo4j-enterprise-3.5.1/plugins/.
    

Restart your Neo4j Server. Your new Stored Procedures are available:

    com.maxdemarzi.json.node.set(Node node, Map properties)
    com.maxdemarzi.json.node.get(Node node)

### Try it:

Create an index:

    CREATE INDEX ON :User(username);

Create some data:

    CREATE (n1:User { username:'User-1' })
    CREATE (n2:User { username:'User-2' })
    CREATE (n3:User { username:'User-3' })
    CREATE (n1)-[:FRIENDS]->(n2)
    CREATE (n1)-[:FRIENDS]->(n3)
    CREATE (n2)-[:FRIENDS]->(n3)

Set additional properties:
    
    MATCH (n:User) WHERE n.username = "User-1"
    WITH n
    CALL com.maxdemarzi.json.node.set(n, {prop1:"String Property", prop2:"Second String Property"}) 
    YIELD value RETURN value
    
Get additional properties:

    MATCH (n:User) WHERE n.username = "User-1"
    WITH n 
    CALL com.maxdemarzi.json.node.get(n) YIELD value RETURN value    
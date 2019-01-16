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
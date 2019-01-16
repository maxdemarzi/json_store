package com.maxdemarzi;

import org.junit.*;
import org.neo4j.driver.v1.*;
import org.neo4j.harness.junit.Neo4jRule;

import java.util.*;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.neo4j.driver.v1.Values.parameters;

public class NodeSetGetTest {
    @Rule
    public final Neo4jRule neo4j = new Neo4jRule()
            .withProcedure(Procedures.class)
            .withFixture(MODEL_STATEMENT);

    @Test
    public void shouldNodeSet()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.build().withoutEncryption().toConfig() ) )
        {

            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            session.run( "MATCH (n:User) WHERE n.username = $username " +
                            "WITH n " +
                            "CALL com.maxdemarzi.json.node.set(n, $properties) " +
                            "YIELD value RETURN value",
                    parameters( "username", "User-1", "properties", properties) );

            StatementResult result = session.run( "MATCH (n:User) WHERE n.username = $username WITH n " +
                            "CALL com.maxdemarzi.json.node.get(n) YIELD value RETURN value",
                    parameters( "username", "User-1") );

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(properties));
        }
    }

    private static HashMap<String, Object> properties = new HashMap<String, Object>(){{
        put("prop1", "String Property");
        put("prop2", "Second String Property");
        put("prop3", 1L);
        put("prop4", 12345678901L);
        put("prop5", true);
        put("prop6", new HashMap<String, Object>() {{
            put("nested1", 2L);
            put("nested2", "Nested String Property");
            put("nested3", "Nested String Property two");
        }});
    }};

    private static final String MODEL_STATEMENT =
            "CREATE (n1:User { username:'User-1' })" +
            "CREATE (n2:User { username:'User-2' })" +
            "CREATE (n3:User { username:'User-3' })" +
            "CREATE (n1)-[:FRIENDS]->(n2)" +
            "CREATE (n1)-[:FRIENDS]->(n3)" +
            "CREATE (n2)-[:FRIENDS]->(n3)" ;
}

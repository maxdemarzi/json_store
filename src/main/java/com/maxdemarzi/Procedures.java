package com.maxdemarzi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxdemarzi.result.MapResult;
import net.openhft.chronicle.core.values.LongValue;
import net.openhft.chronicle.values.Values;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;
import net.openhft.chronicle.map.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Procedures {

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final File nodeFile = new File("./nodeProperties.dat");
    private static final File relationshipFile = new File("./relationshipProperties.dat");

    private static ChronicleMap<LongValue, CharSequence> nodeProperties;
    private static ChronicleMap<LongValue, CharSequence> relProperties;


    @Procedure(name = "com.maxdemarzi.json.node.set", mode = Mode.READ)
    @Description("CALL com.maxdemarzi.json.node.set(Node node, Map properties)")
    public Stream<MapResult> nodeSet(@Name("node") Node node, @Name("properties") Map<String, Object> properties) throws JsonProcessingException {
        initializeNodeProperties();
        LongValue key = Values.newHeapInstance(LongValue.class);
        key.setValue(node.getId());
        nodeProperties.put(key, objectMapper.writeValueAsString(properties));
        return Stream.empty();
    }

    @Procedure(name = "com.maxdemarzi.json.node.get", mode = Mode.READ)
    @Description("CALL com.maxdemarzi.json.node.get(Node node)")
    public Stream<MapResult> nodeGet(@Name("node") Node node) throws IOException {
        initializeNodeProperties();
        LongValue key = Values.newHeapInstance(LongValue.class);
        key.setValue(node.getId());
        HashMap<String, Object> properties = objectMapper.readValue(nodeProperties.get(key).toString(),
                new TypeReference<HashMap<String, Object>>() {});
        return Stream.of(new MapResult(properties));
    }

    private void initializeNodeProperties() {
        if (nodeProperties == null) {
            try {
                nodeProperties = ChronicleMap
                        .of(LongValue.class, CharSequence.class)
                        .name("json-properties-map")
                        .entries(1_000_000)
                        .averageValue(objectMapper.writeValueAsString(averageProperties))
                        .createPersistedTo(nodeFile);
            } catch (IOException e) {
                e.printStackTrace();
                log.error("Node Properties IO Exception: " + nodeFile.getAbsolutePath());
            }
        }
    }

    private static final HashMap<String, Object> averageProperties = new HashMap<String, Object>(){{
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
}

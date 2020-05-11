package savvy.core;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.io.fs.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

/**
 * Embeds a Neo4j instance
 * and exposes some basic functionality
 */
public class EmbeddedNeo4j {

  private static final File databaseDirectory = new File("target/savvy-db");
  private static final Label ENTITY_LABEL = Label.label("Entity");
  private static final String NAME = "name";
  private static final String CYPHER_MERGE =
    String.format("MERGE (n:%1$s {%2$s: $%2$s}) RETURN n", ENTITY_LABEL, NAME);

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private GraphDatabaseService _db;
  private DatabaseManagementService _dbms;
  private IndexDefinition _index;


  private static void registerShutdownHook(final DatabaseManagementService managementService,
    boolean persist) {

    // registers shutdown hook for Neo4j
    // -> shuts down nicely on JVM exit (even for Ctrl-C)
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if (!persist) {
        try {
          FileUtils.deleteRecursively(databaseDirectory);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      managementService.shutdown();
    }));
  }

  /**
   * creates an instance of the database
   *
   * @param persistent whether the database is persistent
   * @throws IOException when the database isn't found (shouldn't happen)
   */
  public void createDb(boolean persistent) throws IOException {
    if (!persistent) {
      FileUtils.deleteRecursively(databaseDirectory);
    }
    _dbms = new DatabaseManagementServiceBuilder(databaseDirectory).build();
    _db = _dbms.database(DEFAULT_DATABASE_NAME);

    // create constraint & index
    createConstraint();

    registerShutdownHook(_dbms, persistent);
  }

  /**
   * create constraint for unique entities (by name)
   * create index for lookup/edit of entities in db
   * (neo4j automatically creates the index with the constraint)
   */
  public void createConstraint() {

    // create index if needed
    try (var tx = _db.beginTx()) {
      tx.schema().getConstraintByName(NAME);
      _index = tx.schema().getIndexByName(NAME);

    } catch (IllegalArgumentException e) {
      try (var tx = _db.beginTx()) {
        // entity (names) must be unique
        tx.schema().constraintFor(ENTITY_LABEL).assertPropertyIsUnique(NAME).withName(NAME)
          .create();
        _index = tx.schema().getIndexByName(NAME);

        tx.commit();

      }
    }
  }

  public boolean isIndexed() {
    try (Transaction tx = _db.beginTx()) {
      return tx.schema().getIndexPopulationProgress(_index).getCompletedPercentage() == 100F;
    }
  }

  /**
   * add a fact to the database
   *
   * @param subject      the subject of the fact
   * @param relationship the relationship between the subject and object
   * @param object       the object of the fact
   */
  public void addData(String subject, String relationship, String object) {
    addEntity(subject);
    addEntity(object);

    try (Transaction tx = _db.beginTx()) {
      var subNode = tx.findNode(ENTITY_LABEL, NAME, subject);
      var objNode = tx.findNode(ENTITY_LABEL, NAME, object);

      var rel = subNode.createRelationshipTo(objNode, RelTypes.KNOWS);
      rel.setProperty(NAME, relationship);

      tx.commit();
    }

  }

  public Node addEntity(String name) {
    try (var tx = _db.beginTx()) {
      Map<String, Object> params = Map.of(NAME, name);
      Node result = tx.execute(CYPHER_MERGE, params).<Node>columnAs("n").next();
      tx.commit();
      return result;
    }
  }

  public String readData(String entityName) {
    var list = List.of();
    try (var tx = _db.beginTx()) {
      var found = tx.findNodes(ENTITY_LABEL, NAME, entityName).stream().findFirst();

      // todo use a traversal to get matching facts and populate the list view
      return found.map(node -> (node.getProperty(NAME)) + " → ???").orElse("NOT FOUND");
    }
  }


  /**
   * remove a fact from the database
   */
  public void removeData(String subject, String relationship, String object) {
    try (Transaction tx = _db.beginTx()) {

      // find & delete the relationship between subject and object
      var subNode = tx.findNode(ENTITY_LABEL, NAME, subject);
      var objNode = tx.findNode(ENTITY_LABEL, NAME, object);
      subNode.getRelationships(Direction.OUTGOING, RelTypes.KNOWS).forEach(rel -> {
        if (rel.getEndNode().equals(objNode) && rel.getProperty(NAME).equals(relationship)) {
          rel.delete();
        }
      });

      // delete subject if now unused
      if (!subNode.hasRelationship()) {
        subNode.delete();
      }

      // delete object if now unused
      if (!objNode.hasRelationship()) {
        objNode.delete();
      }

      tx.commit();
    }
  }

  /**
   * manually shutdown the database
   * (usually we rely on the shutdown hook)
   */
  public void shutDown() {
    System.out.println("Shutting down database ...");
    _dbms.shutdown();

  }

  // describes the types of relationships within the database
  private enum RelTypes implements RelationshipType {
    KNOWS
  }
}

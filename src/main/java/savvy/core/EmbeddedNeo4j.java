package savvy.core;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.io.fs.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

/**
 * Embeds a Neo4j instance
 * and exposes some basic functionality
 */
public class EmbeddedNeo4j {

  private static final File databaseDirectory = new File("target/savvy-db");
  private static final Label ENTITY_LABEL = Label.label("Entity");
  private static final String NAME = "name";
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  public String result;

  private GraphDatabaseService _db;
  private Node _subject;
  private Node _object;
  private Relationship _relationship;
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

    // create entities index
    createIndex();

    registerShutdownHook(_dbms, persistent);
  }

  /**
   * setup the index for lookup/edit of entities in db
   */
  public void createIndex() {
    if (indexAlreadyExists()) {
      return;
    }
    try (var tx = _db.beginTx()) {
      var schema = tx.schema();

      _index = schema.indexFor(ENTITY_LABEL).on(NAME).withName(NAME).create();

      tx.commit();

    }
  }

  private boolean indexAlreadyExists() {
    try (var tx = _db.beginTx()) {
      var indexes = tx.schema().getIndexes();
      for (var index : indexes) {
        for (var key : index.getPropertyKeys()) {
          if (key.equals(NAME)) {
            return true;
          }
        }
      }
    }
    return false;
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
    try (Transaction tx = _db.beginTx()) {
      _subject = tx.createNode(ENTITY_LABEL);
      _subject.setProperty(NAME, subject);

      _object = tx.createNode();
      _object.setProperty(NAME, object);

      _relationship = _subject.createRelationshipTo(_object, RelTypes.KNOWS);
      _relationship.setProperty(NAME, relationship);
      tx.commit();
    }
  }

  /**
   * read a fact from the database
   *
   * @return the fact if found
   */
  public String readData() {
    try (Transaction tx = _db.beginTx()) {
      _subject = tx.getNodeById(_subject.getId());
      _object = tx.getNodeById(_object.getId());
      _relationship = tx.getRelationshipById(_relationship.getId());


      log.info("{}", _subject.getProperty(NAME));
      log.info("{}", _relationship.getProperty(NAME));
      log.info("{}", _object.getProperty(NAME));

      result =
        (_subject.getProperty(NAME)) + " → " + _relationship.getProperty(NAME) + " → " + (_object
          .getProperty(NAME));

      tx.commit();
    }
    return result;
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
  public void removeData() {
    try (Transaction tx = _db.beginTx()) {

      _subject = tx.getNodeById(_subject.getId());
      _object = tx.getNodeById(_object.getId());
      _subject.getSingleRelationship(RelTypes.KNOWS, Direction.OUTGOING).delete();
      _subject.delete();
      _object.delete();

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

package savvy.core;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.io.fs.FileUtils;

import java.io.File;
import java.io.IOException;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

/**
 * Embeds a Neo4j instance
 * and exposes some basic functionality
 */
public class EmbeddedNeo4j {
  private static final File databaseDirectory = new File("target/neo4j-hello-db");

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
    registerShutdownHook(_dbms, persistent);

  }

  /**
   * setup the index for lookup/edit of db entries
   */
  public void setupIndex() {
    try (var tx = _db.beginTx()) {
      var schema = tx.schema();
      _index = schema.indexFor(Label.label("message")).on("message").withName("message").create();

      tx.commit();

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
      _subject = tx.createNode();
      _subject.setProperty("message", subject);

      _object = tx.createNode();
      _object.setProperty("message", object);

      _relationship = _subject.createRelationshipTo(_object, RelTypes.KNOWS);
      _relationship.setProperty("message", relationship);
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

      System.out.print(_subject.getProperty("message"));
      System.out.print(_relationship.getProperty("message"));
      System.out.print(_object.getProperty("message"));

      result =
        (_subject.getProperty("message")) + " → " + _relationship.getProperty("message") + " → "
          + (_object.getProperty("message"));

      tx.commit();
    }
    return result;
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

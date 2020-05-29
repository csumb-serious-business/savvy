package savvy.core.db;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;
import static savvy.core.db.Constants.*;

import java.io.File;
import java.io.IOException;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.io.fs.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Embeds a Neo4j instance and exposes some basic functionality */
public class EmbeddedNeo4j {

  private static final File databaseDirectory = new File(DB_DIR);

  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  private GraphDatabaseService _db;
  private DatabaseManagementService _dbms;
  private IndexDefinition _index;

  // === DB management ===========================================================================\\
  private static void registerShutdownHook(
      final DatabaseManagementService managementService, boolean persist) {

    // registers shutdown hook for Neo4j
    // -> shuts down nicely on JVM exit (even for Ctrl-C)
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
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
   * create constraint for unique entities (by name) create index for lookup/edit of entities in db
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
        tx.schema()
            .constraintFor(ENTITY_LABEL)
            .assertPropertyIsUnique(NAME)
            .withName(NAME)
            .create();
        _index = tx.schema().getIndexByName(NAME);

        tx.commit();
      }
    }
  }

  /**
   * checks whether the index is populated
   *
   * @return true if index population is complete
   */
  public boolean isIndexed() {
    try (var tx = _db.beginTx()) {
      return tx.schema().getIndexPopulationProgress(_index).getCompletedPercentage() == 100F;
    }
  }

  /** manually shutdown the database (usually we rely on the shutdown hook) */
  public void shutDown() {
    System.out.println("Shutting down database ...");
    _dbms.shutdown();
  }

  /** @return the database service */
  public GraphDatabaseService getService() {
    return _db;
  }
}

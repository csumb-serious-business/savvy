package savvy.core.db;

import static savvy.core.db.Constants.ALIASES;
import static savvy.core.db.Constants.CORRELATES;
import static savvy.core.db.Constants.CYPHER_MERGE;
import static savvy.core.db.Constants.ENTITY_LABEL;
import static savvy.core.db.Constants.MODIFIER;
import static savvy.core.db.Constants.NAME;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.entity.Entity;
import savvy.core.fact.Fact;
import savvy.core.fact.Modifier;
import savvy.core.relationship.Correlate;
import savvy.core.relationship.Relationship;

/** Database access object */
public class Dao {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
  private final GraphDatabaseService _service;

  public Dao(EmbeddedNeo4j en4j) {
    _service = en4j.getService();
  }

  // --- facts -----------------------------------------------------------------------------------\\

  /**
   * add a fact to the database
   *
   * @param subject of the fact
   * @param relationship between the subject and object
   * @param object of the fact
   */
  public void createFact(
      Entity subject, Relationship relationship, Entity object, Modifier modifier) {
    createEntity(subject);
    createEntity(object);

    try (var tx = _service.beginTx()) {
      var subNode = tx.findNode(ENTITY_LABEL, NAME, subject.getName());
      var objNode = tx.findNode(ENTITY_LABEL, NAME, object.getName());

      var rel = subNode.createRelationshipTo(objNode, RelTypes.f2_1);
      rel.setProperty(NAME, relationship.getName());
      rel.setProperty(CORRELATES, new SerDe<Correlate>().fromSet(relationship.getCorrelates()));
      rel.setProperty(MODIFIER, new SerDe<Modifier>().fromObject(modifier));

      tx.commit();
    }
    log.info("createFact -- s: {}, r: {}, o: {}, m: {}", subject, relationship, object, modifier);
  }

  /**
   * add a fact to the database
   *
   * @param fact to add
   */
  public void createFact(Fact fact) {
    createFact(fact.subject, fact.relationship, fact.object, fact.modifier);
  }

  /**
   * remove a fact from the database note: if an entity has no remaining facts, it will be removed
   */
  public void deleteFact(Entity subject, Relationship relationship, Entity object) {
    try (var tx = _service.beginTx()) {

      // find & delete the relationship between subject and object
      var subNode = tx.findNode(ENTITY_LABEL, NAME, subject.getName());
      var objNode = tx.findNode(ENTITY_LABEL, NAME, object.getName());
      subNode
          .getRelationships(Direction.OUTGOING, RelTypes.f2_1)
          .forEach(
              rel -> {
                if (rel.getEndNode().equals(objNode)
                    && rel.getProperty(NAME).equals(relationship.getName())) {
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
   * Delete a fact from the database
   *
   * @param fact to delete
   */
  public void deleteFact(Fact fact) {
    deleteFact(fact.subject, fact.relationship, fact.object);
  }

  /**
   * Traverse the graph collecting all relationships and entities that are connected to a given
   * entity and return a fact for each
   *
   * @param entityName to lookup
   * @return a set of Facts corresponding to the relationships found in the traversal
   */
  public Set<Fact> readRelatedFacts(String entityName) {

    var set = new TreeSet<Fact>();
    try (var tx = _service.beginTx()) {
      var found = tx.findNodes(ENTITY_LABEL, NAME, entityName).stream().findFirst();
      if (found.isEmpty()) {
        return Set.of();
      }
      var paths =
          tx.traversalDescription()
              .depthFirst()
              .relationships(RelTypes.f2_1)
              .evaluator(Evaluators.excludeStartPosition())
              .traverse(found.get());

      for (var path : paths) {
        set.add(pathAsFact(path));
      }

      return set;
    }
  }

  /**
   * Traverse the graph collecting relationships for all entities
   *
   * @return returns a set of Facts corresponding to the relationships
   */
  public Set<Fact> readAllFacts() {
    var set = new TreeSet<Fact>();

    try (var tx = _service.beginTx()) {
      tx.findNodes(ENTITY_LABEL).stream()
          .forEach(
              n -> {
                var paths =
                    tx.traversalDescription()
                        .depthFirst()
                        .relationships(RelTypes.f2_1)
                        .evaluator(Evaluators.excludeStartPosition())
                        .traverse(n);

                for (var path : paths) {
                  set.add(pathAsFact(path));
                }
              });
    }
    return set;
  }

  /**
   * For a given path, gather its subject, relationship and object and create a fact from it
   *
   * @param path containing the relationship
   * @return a corresponding Fact
   */
  private Fact pathAsFact(Path path) {
    var rel = path.lastRelationship();
    var r = rel.getProperty(NAME).toString();
    var rc = new SerDe<Correlate>().toSet(rel.getProperty(CORRELATES));
    var rm = new SerDe<Modifier>().toType(rel.getProperty(MODIFIER));

    var serde = new SerDe<String>();
    var s = rel.getStartNode().getProperty(NAME).toString();
    var sa = serde.toSet(rel.getStartNode().getProperty(ALIASES));

    var o = rel.getEndNode().getProperty(NAME).toString();
    var oa = serde.toSet(rel.getEndNode().getProperty(ALIASES));

    var fact = new Fact(new Entity(s, sa), new Relationship(r, rc), new Entity(o, oa), rm);
    //    log.info("fact: {}, modifiers: {}", fact, rm);
    return fact;
  }

  // --- entities --------------------------------------------------------------------------------\\

  /**
   * adds an entity to the database if it did not exist yet note: it seems the fist call to this
   * takes some time to execute
   *
   * @param entity to create
   */
  public void createEntity(Entity entity) {
    try (var tx = _service.beginTx()) {
      var params = new HashMap<String, Object>();
      params.put(NAME, entity.getName());
      Node result = tx.execute(CYPHER_MERGE, params).<Node>columnAs("n").next();
      result.setProperty(ALIASES, new SerDe<String>().fromSet(entity.getAliases()));
      tx.commit();
      // return result;
    }
  }

  /**
   * update an Entity in the database
   *
   * @param previous entity version
   * @param current entity version
   */
  public void updateEntity(Entity previous, Entity current) {

    // same authoritative name -> merge them otherwise rename
    if (previous.getName().equals(current.getName())) {
      try (var tx = _service.beginTx()) {
        var entity = tx.findNode(ENTITY_LABEL, NAME, previous.getName());

        entity.setProperty(ALIASES, new SerDe<String>().fromSet(current.getAliases()));

        tx.commit();
      }
    } else {
      try (var tx = _service.beginTx()) {
        var pNode = tx.findNode(ENTITY_LABEL, NAME, previous.getName());
        pNode.setProperty(NAME, current.getName());
        pNode.setProperty(ALIASES, new SerDe<String>().fromSet(current.getAliases()));

        tx.commit();
      }
    }
  }

  /**
   * Gathers a set of all entities within the database
   *
   * @return the entities
   */
  public Set<Entity> readAllEntities() {
    var entities = new TreeSet<Entity>();
    try (var tx = _service.beginTx()) {
      tx.findNodes(ENTITY_LABEL).stream()
          .forEach(
              n -> {
                var name = n.getProperty(NAME).toString();
                var aliases = new SerDe<String>().toSet((byte[]) n.getProperty(ALIASES));
                entities.add(new Entity(name, aliases));
                // log.info("reading -- name: {}, aliases: {}", name, aliases);
              });
    }

    return entities;
  }

  // --- relationships ---------------------------------------------------------------------------\\

  /**
   * Gathers a set of all relationships whithin the database
   *
   * @return the relationships
   */
  public Set<Relationship> readAllRelationships() {
    var relationships = new HashSet<Relationship>();
    readAllFacts().forEach(f -> relationships.add(f.relationship));
    //    log.info("relationships: {}", relationships);

    return relationships;
  }

  /**
   * update a relationship in the database
   *
   * @param previous relationship version
   * @param current relationship version
   */
  public void updateRelationship(Relationship previous, Relationship current) {

    try (var tx = _service.beginTx()) {
      // all facts
      tx.findNodes(ENTITY_LABEL).stream()
          .forEach(
              n -> {
                var paths =
                    tx.traversalDescription()
                        .depthFirst()
                        .relationships(RelTypes.f2_1)
                        .evaluator(Evaluators.excludeStartPosition())
                        .traverse(n);

                for (var path : paths) {
                  var rel = path.lastRelationship();
                  var rName = rel.getProperty(NAME).toString();

                  // if matching relationship
                  if (rName.equals(previous.getName())) {
                    if (!previous.getName().equals(current.getName())) {
                      rel.setProperty(NAME, current.getName());
                    }
                    if (!previous.getCorrelates().equals(current.getCorrelates())) {
                      rel.setProperty(
                          CORRELATES, new SerDe<Correlate>().fromSet(current.getCorrelates()));
                    }
                  }
                }
              });

      tx.commit();
    }
  }
}

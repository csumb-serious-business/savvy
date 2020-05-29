package savvy.core.fact;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.db.Dao;
import savvy.core.db.EmbeddedNeo4j;
import savvy.core.entity.Entities;
import savvy.core.entity.Entity;
import savvy.core.entity.events.EntitiesRead;
import savvy.core.fact.events.DoFactCreate;
import savvy.core.fact.events.DoFactDelete;
import savvy.core.fact.events.DoFactUpdate;
import savvy.core.fact.events.DoFactsSearch;
import savvy.core.fact.events.FactCreated;
import savvy.core.fact.events.FactDeleted;
import savvy.core.fact.events.FactUpdated;
import savvy.core.fact.events.FactsSearched;
import savvy.core.relationship.Correlate;
import savvy.core.relationship.Relationship;
import savvy.core.relationship.Relationships;
import savvy.core.relationship.events.RelationshipsRead;

/** interfaces with the db */
public class Facts {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
  private final Set<Fact> _items = new HashSet<>();
  private final Set<Entity> _entities = new HashSet<>();
  private final Set<Relationship> _relationships = new HashSet<>();
  private Dao _dao;

  /**
   * searches the database for related facts
   *
   * @param filter if blank, all facts are returned otherwise only related facts
   * @return a sorted list of related facts
   */
  private List<Fact> factsSearch(String filter) {
    var found = Entities.getEntitiesWithIdentifier(_entities, filter);
    if (!found.isEmpty()) {
      filter = found.get(0).getName();
    }

    _items.clear();
    if (filter.isBlank()) {
      _items.addAll(_dao.readAllFacts());
    } else {
      _items.addAll(_dao.readRelatedFacts(filter));
    }
    return _items.stream().sorted().collect(Collectors.toList());
  }

  /**
   * create a fact with a given subject, relationship & object if any matching entity/relationship
   * already exists, it will be used
   *
   * @param subject fact component
   * @param relationship fact component
   * @param object fact component
   * @return the created fact in its final form
   */
  private Fact factCreate(String subject, String relationship, String object) {
    Entity s;
    // subject exists -> use existent
    var eFound = Entities.getEntitiesWithIdentifier(_entities, subject);
    if (eFound.isEmpty()) {
      s = new Entity(subject, Set.of());
    } else {
      s = eFound.get(0);
    }

    Entity o;
    // object exists -> use existent
    eFound = Entities.getEntitiesWithIdentifier(_entities, object);
    if (eFound.isEmpty()) {
      o = new Entity(object, Set.of());
    } else {
      o = eFound.get(0);
    }

    Relationship r;
    boolean rIsOutbound = true;
    // relationship exists -> use existent
    var rFound = Relationships.getRelationshipsWithForm(_relationships, relationship);
    if (rFound.isEmpty()) {
      r =
          new Relationship(
              relationship, Set.of(new Correlate(relationship, ("[←" + relationship + "]"))));
    } else {
      r = rFound.get(0);
      rIsOutbound = r.hasOutboundCorrelate(relationship);
    }

    Fact fact;
    if (rIsOutbound) {
      fact = new Fact(s, r, o);
    } else {
      fact = new Fact(o, r, s);
    }

    _dao.createFact(fact);
    return fact;
  }

  /**
   * delete a fact
   *
   * @param fact to delete
   */
  private void factDelete(Fact fact) {
    _dao.deleteFact(fact);
  }

  /**
   * update a fact from a previous state to a new one
   *
   * @param previous fact version
   * @param current fact version
   * @return the updated fact in its final form
   */
  private Optional<Fact> factUpdate(Fact previous, Fact current) {
    if (previous.equals(current)) {
      return Optional.empty();
    }

    factDelete(previous);

    var created =
        factCreate(
            current.getSubject().getName(),
            current.getRelationship().getName(),
            current.getObject().getName());

    return Optional.of(created);
  }

  // === events ==================================================================================\\
  // --- Emitters --------------------------------------------------------------------------------\\

  /**
   * initialize this with a given db
   *
   * @param en4j to use
   */
  public void init(EmbeddedNeo4j en4j) {
    _dao = new Dao(en4j);
    EventBus.getDefault().register(this);
  }

  // --- DO listeners ----------------------------------------------------------------------------\\
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(DoFactsSearch ev) {
    var facts = factsSearch(ev.filter);
    EventBus.getDefault().post(new FactsSearched(facts));
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(DoFactCreate ev) {
    var fact = factCreate(ev.subject, ev.relationship, ev.object);
    EventBus.getDefault().post(new FactCreated(fact));
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(DoFactUpdate ev) {
    var success = factUpdate(ev.previous, ev.current);
    success.ifPresent(fact -> EventBus.getDefault().post(new FactUpdated(ev.previous, fact)));
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(DoFactDelete ev) {
    factDelete(ev.fact);
    EventBus.getDefault().post(new FactDeleted(ev.fact));
  }

  // --- ON listeners ---------------------------------------------------------------------------\\

  // entities read -> update entities autocomplete
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(EntitiesRead ev) {
    _entities.clear();
    _entities.addAll(ev.entities);
  }

  // relationships read -> update relationships autocomplete
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(RelationshipsRead ev) {
    _relationships.clear();
    _relationships.addAll(ev.relationships);
  }
}

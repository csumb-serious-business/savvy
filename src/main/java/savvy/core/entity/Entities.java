package savvy.core.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.db.Dao;
import savvy.core.db.EmbeddedNeo4j;
import savvy.core.entity.events.DoEntitiesFilter;
import savvy.core.entity.events.DoEntitiesRead;
import savvy.core.entity.events.DoEntityUpdate;
import savvy.core.entity.events.EntitiesFiltered;
import savvy.core.entity.events.EntitiesRead;
import savvy.core.entity.events.EntityUpdated;
import savvy.core.fact.events.FactCreated;
import savvy.core.fact.events.FactDeleted;
import savvy.core.fact.events.FactUpdated;

/** models a group of entities */
public class Entities {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  private final Set<Entity> _items = new HashSet<>();

  private Dao _dao;

  /**
   * given a collection of entities, finds and returns those that have a matching identifier
   *
   * @param entities to filter
   * @param identifier to search for
   * @return found entities list
   */
  public static List<Entity> getEntitiesWithIdentifier(
      Collection<Entity> entities, String identifier) {
    return entities.stream().filter(i -> i.hasIdentifier(identifier)).collect(Collectors.toList());
  }

  /**
   * get a list of entities that match from this Entities items
   *
   * @param identifier to filter
   * @return found entities list
   */
  public List<Entity> getEntitiesWithIdentifier(String identifier) {
    return Entities.getEntitiesWithIdentifier(_items, identifier);
  }

  /**
   * goes to the db to get an up-to-date version of all entities broadcasts the read event on the
   * event bus
   */
  private List<Entity> refresh() {
    _items.clear();
    _items.addAll(_dao.readAllEntities());

    return new ArrayList<>(_items).stream().sorted().collect(Collectors.toList());
  }

  /**
   * updates a particular entity in the db broadcasts the change on the event bus
   *
   * @param previous previous version of the Entity
   * @param current current version of the Entity
   */
  private boolean entityUpdate(Entity previous, Entity current) {
    if (previous.equals(current)) {
      return false;
    }

    _dao.updateEntity(previous, current);
    return true;
  }

  /**
   * broadcasts a filtered copy of contained Entity items on the event bus
   *
   * @param filter the string filter to match against
   */
  private List<Entity> entitiesFilter(String filter) {
    if (filter.isBlank()) {
      return _items.stream().sorted().collect(Collectors.toList());
    } else {
      return _items.stream()
          .filter(i -> i.hasIdentifier(filter))
          .sorted()
          .collect(Collectors.toList());
    }
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

    // register with event bus
    EventBus.getDefault().register(this);

    var read = refresh();
    EventBus.getDefault().post(new EntitiesRead(read));
  }

  // --- DO listeners ----------------------------------------------------------------------------\\
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(DoEntitiesRead ev) {
    var read = refresh();
    EventBus.getDefault().post(new EntitiesRead(read));
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(DoEntitiesFilter ev) {
    var entities = entitiesFilter(ev.filter);
    EventBus.getDefault().post(new EntitiesFiltered(entities));
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(DoEntityUpdate ev) {
    var success = entityUpdate(ev.previous, ev.current);
    if (success) {
      EventBus.getDefault().post(new EntityUpdated(ev.previous, ev.current));
    }
  }

  // --- ON listeners ---------------------------------------------------------------------------\\
  // fact created -> refresh
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(FactCreated ev) {
    var read = refresh();
    EventBus.getDefault().post(new EntitiesRead(read));
  }

  // fact updated -> refresh
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(FactUpdated ev) {
    var read = refresh();
    EventBus.getDefault().post(new EntitiesRead(read));
  }

  // fact deleted -> refresh
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(FactDeleted ev) {
    var read = refresh();
    EventBus.getDefault().post(new EntitiesRead(read));
  }

  // entity updated -> refresh
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(EntityUpdated ev) {
    var read = refresh();
    EventBus.getDefault().post(new EntitiesRead(read));
  }
}

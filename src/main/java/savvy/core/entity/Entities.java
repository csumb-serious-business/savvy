package savvy.core.entity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * models a group of entities
 */
public class Entities {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  private final Set<Entity> _items;

  private EmbeddedNeo4j _db;

  public Entities() {
    _items = new HashSet<>();
  }

  public static List<Entity> getEntitiesWithIdentifier(Collection<Entity> entities,
    String identifier) {
    return entities.stream().filter(i -> i.hasIdentifier(identifier)).collect(Collectors.toList());
  }

  public List<Entity> getEntitiesWithIdentifier(String alias) {
    return Entities.getEntitiesWithIdentifier(_items, alias);
  }

  /**
   * initializes this with a given db
   *
   * @param db the db to use
   */
  public void init(EmbeddedNeo4j db) {
    _db = db;

    // register with event bus
    EventBus.getDefault().register(this);

    refresh();
  }

  /**
   * goes to the db to get an up-to-date version of all entities
   * broadcasts the read event on the event bus
   */
  private void refresh() {
    _items.clear();
    _items.addAll(_db.readAllEntities());

    var result = new ArrayList<>(_items).stream().sorted().collect(Collectors.toList());
    EventBus.getDefault().post(new EntitiesRead(result));
  }

  /**
   * updates a particular entity in the db
   * broadcasts the change on the event bus
   *
   * @param previous previous version of the Entity
   * @param current  current version of the Entity
   */
  private void entityUpdate(Entity previous, Entity current) {
    if (previous.equals(current)) {
      return;
    }

    _db.updateEntity(previous, current);

    EventBus.getDefault().post(new EntityUpdated(previous, current));
  }

  /**
   * broadcasts a filtered copy of contained Entity items on the event bus
   *
   * @param filter the string filter to match against
   */
  private void entitiesFilter(String filter) {
    List<Entity> entities;
    if (filter.equals("")) {
      entities = _items.stream().sorted().collect(Collectors.toList());
    } else {

      entities =
        _items.stream().filter(i -> i.hasAlias(filter)).sorted().collect(Collectors.toList());
    }
    EventBus.getDefault().post(new EntitiesFiltered(entities));
  }

  //=== event listeners =========================================================================\\
  //--- DOs -------------------------------------------------------------------------------------\\
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(DoEntitiesRead ev) {
    refresh();
  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void on(DoEntitiesFilter ev) {
    entitiesFilter(ev.filter);
  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void on(DoEntityUpdate ev) {
    entityUpdate(ev.previous, ev.current);
  }

  //--- ONs -------------------------------------------------------------------------------------\\
  // fact created -> refresh
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FactCreated ev) {
    refresh();
  }

  // fact updated -> refresh
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FactUpdated ev) {
    refresh();
  }

  // fact deleted -> refresh
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FactDeleted ev) {
    refresh();
  }

  // entity updated -> refresh
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(EntityUpdated ev) {
    refresh();
  }

}

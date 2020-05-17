package savvy.core.fact;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.db.EmbeddedNeo4j;
import savvy.core.fact.events.DoFactCreate;
import savvy.core.fact.events.DoFactDelete;
import savvy.core.fact.events.DoFactUpdate;
import savvy.core.fact.events.DoFactsRead;
import savvy.core.fact.events.FactCreated;
import savvy.core.fact.events.FactDeleted;
import savvy.core.fact.events.FactUpdated;
import savvy.core.fact.events.FactsRead;

import java.util.Set;

/**
 * interfaces with the db
 */
public class Facts {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  private EmbeddedNeo4j _db;

  /**
   * iniltializes this with a given db
   *
   * @param db the db to use
   */
  public void init(EmbeddedNeo4j db) {
    _db = db;
    EventBus.getDefault().register(this);
  }

  //=== event listeners =========================================================================\\

  @Subscribe(threadMode = ThreadMode.MAIN) public void on(DoFactCreate ev) {
    var fact = ev.fact;
    _db.createFact(fact);
    EventBus.getDefault().post(new FactCreated(fact));
  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void on(DoFactUpdate ev) {
    var previous = ev.previous;
    var current = ev.current;
    _db.deleteFact(previous);
    _db.createFact(current);

    EventBus.getDefault().post(new FactUpdated(previous, current));
  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void on(DoFactDelete ev) {
    var fact = ev.fact;
    _db.deleteFact(fact);
    EventBus.getDefault().post(new FactDeleted(fact));
  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void on(DoFactsRead ev) {
    Set<Fact> facts;
    if (ev.filter.equals("")) {
      facts = _db.readAllFacts();
    } else {
      facts = _db.readRelatedFacts(ev.filter);
    }

    EventBus.getDefault().post(new FactsRead(facts));
  }

}

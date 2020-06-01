package savvy.core.fact.events;

import savvy.ui.fact_create.FactCreateController;

public class DoSelectSubject {
  public void selectSubject(FactCreateController factCreateController) {
    factCreateController.positionCaret();
  }
}

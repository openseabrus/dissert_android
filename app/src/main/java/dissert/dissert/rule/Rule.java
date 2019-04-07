package dissert.dissert.rule;

import org.json.JSONException;
import org.json.JSONObject;

import dissert.dissert.rule.action.Action;
import dissert.dissert.rule.trigger.Trigger;

public class Rule {

    private Trigger trigger;
    private Action action;
    private int priority;

    public Rule(JSONObject rule) {
        processJSON(rule);
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public Action getAction() {
        return action;
    }

    private void processJSON(JSONObject rule) {
        try {
            trigger = new Trigger(rule.getJSONObject("trigger"));
            action = new Action(rule.getJSONObject("action"));
        } catch (JSONException e) {

        }
    }
}

package dissert.dissert.rule.trigger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import dissert.dissert.rule.RuleMember;
import dissert.dissert.rule.attribute.Attribute;
import dissert.dissert.rule.field.Field;

public class Trigger extends RuleMember {

    public Trigger(JSONObject trigger) {
        super(trigger);
    }

}

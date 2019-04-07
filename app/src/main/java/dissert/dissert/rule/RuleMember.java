package dissert.dissert.rule;

import org.json.JSONException;
import org.json.JSONObject;


import dissert.dissert.rule.attribute.Attribute;

public abstract class RuleMember {

    protected Attribute attribute;
    protected String entity;

    public RuleMember(JSONObject member) {
        processJSON(member);
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public String getEntity() {
        return entity;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        RuleMember rm = (RuleMember) obj;

        boolean equal = true;
        equal = equal && attribute.equals(rm.getAttribute());
        equal = equal && entity.equals(rm.getEntity());

        return equal;
    }

    protected void processJSON(JSONObject member) {
        try {
            entity = member.getString("entity");
            JSONObject attribute = member.getJSONObject("attribute");
            this.attribute = new Attribute(attribute);
        } catch (JSONException e) {

        }
    }
}

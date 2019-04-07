package dissert.dissert.rule.field;

import org.json.JSONException;
import org.json.JSONObject;

public class Field {

    private String name;
    private String type;
    private String unit;
    private String value;
    private String id;
    private String operator;

    public Field(JSONObject field) {
        processJSON(field);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getUnit() {
        return unit;
    }

    public String getValue() {
        return value;
    }

    public String getId() {
        return id;
    }

    public String getOperator() { return operator; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        Field fo = (Field) obj;
        boolean equal = true;

        equal = equal && (name==null ? fo.getName()==null : name.equals(fo.getName()));
        equal = equal && (type==null ? fo.getType()==null : type.equals(fo.getType()));
        equal = equal && (unit==null ? fo.getUnit()==null : unit.equals(fo.getUnit()));
        equal = equal && (value==null ? fo.getValue()==null : value.equals(fo.getValue()));
        equal = equal && (id==null ? fo.getId()==null : id.equals(fo.getId()));

        return equal;
    }

    private void processJSON(JSONObject field) {
        try {
            if (field.has("name"))
                name = field.getString("name");
            if (field.has("type"))
                type = field.getString("type");
            if (field.has("unit"))
                unit = field.getString("unit");
            if (field.has("value"))
                value = field.getString("value");
            if (field.has("id"))
                id = field.getString("id");
            if (field.has("operator"))
                operator = field.getString("operator");
        } catch (JSONException e) {

        }
    }
}

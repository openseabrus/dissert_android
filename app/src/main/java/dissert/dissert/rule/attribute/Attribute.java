package dissert.dissert.rule.attribute;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import dissert.dissert.rule.field.Field;
import dissert.dissert.utilities.Evaluator;

public class Attribute {

    private String name;
    private List<Field> fields;

    public Attribute(JSONObject attribute) {
        processJSON(attribute);
    }

    public String getName() {
        return name;
    }

    public List<Field> getFields() {
        return fields;
    }

    public List<String> getIds() {
        List<Field> fields = getFields();
        List<String> ids = new ArrayList<>(fields.size());
        for (Field f : fields) {
            ids.add(f.getId());
        }

        return ids;
    }

    public boolean matchedValues(String value) {
        return matchedValues(Arrays.asList(new String[] {value}));
    }

    public boolean matchedValues(List<String> values) {
        if (fields.size() != values.size())
            return false;
        try {
            Iterator<Field> fi = fields.iterator();
            Iterator<String> vi = values.iterator();

            while (fi.hasNext()) {
                Field field = fi.next();
                String vield = vi.next();

                boolean activate = Evaluator.evaluate(vield, field);
                if (!activate)
                    return false;
            }
        } catch (ArrayIndexOutOfBoundsException | NoSuchElementException e) {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Attribute o = (Attribute) obj;

        if (!name.equals(o.getName()))
            return false;

        if (!fields.equals(o.getFields()))
            return false;

        return true;
    }

    private void processJSON(JSONObject attribute) {
        try {
            name = attribute.getString("name");
            JSONArray fields = attribute.getJSONArray("fields");
            this.fields = new ArrayList<>(fields.length());
            for (int i = 0; i < fields.length(); i++) {
                JSONObject field = fields.getJSONObject(i);
                this.fields.add(new Field(field));
            }
        } catch (JSONException e) {

        }
    }
}

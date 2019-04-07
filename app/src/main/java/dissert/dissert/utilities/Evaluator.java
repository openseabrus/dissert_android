package dissert.dissert.utilities;

import dissert.dissert.rule.field.Field;

public class  Evaluator {

    private static final String EQUAL_TO = "EQ";
    private static final String NOT_EQUAL = "NEQ";
    private static final String LESS_THAN = "LT";
    private static final String LESS_THAN_OR_EQUAL = "LTE";
    private static final String GREATER_THAN = "GT";
    private static final String GREATER_THAN_OR_EQUAL = "GTE";

    public static boolean evaluate(String value, Field field) {
        String f = field.getValue();
        if (field.getType().equals("custom"))
            return field.getValue().equals(value);
        switch (field.getOperator()) {
            case EQUAL_TO:
                return value.equals(f);

            case NOT_EQUAL:
                return !value.equals(f);

            case LESS_THAN:
                return Double.parseDouble(value) < Double.parseDouble(f);

            case LESS_THAN_OR_EQUAL:
                return Double.parseDouble(value) <= Double.parseDouble(f);

            case GREATER_THAN:
                return Double.parseDouble(value) > Double.parseDouble(f);

            case GREATER_THAN_OR_EQUAL:
                return Double.parseDouble(value) >= Double.parseDouble(f);

            default:
                return false;
        }
    }
}

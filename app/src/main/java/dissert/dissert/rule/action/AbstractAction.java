package dissert.dissert.rule.action;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Holds a sensor value and the corresponding Actions that must be taken when that value is met
 * @param <I> Value to be held
 * @param <L> List<L> of Actions
 */
public class AbstractAction<I, L> {

    private I sensorValue;
    private List<L> actions;


    public AbstractAction(I sensorValue, List<L> actions) {
        this.sensorValue = sensorValue;
        this.actions = actions;
    }

    public I getValue() {
        return this.sensorValue;
    }

    public List<L> getActions() {
        return this.actions;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        AbstractAction<?,?> p = (AbstractAction<?,?>) obj;

        if (sensorValue != (p.getValue()))
            return false;

        try {
            Iterator i = actions.iterator();
            Iterator ip = p.getActions().iterator();

            while(i.hasNext()) {
                String is = (String) i.next();
                String ps = (String) ip.next();

                if (!is.equals(ps))
                    return false;
            }

            if(i.hasNext() != ip.hasNext())
                return false;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return sensorValue.hashCode() + actions.hashCode();
    }

    @Override
    public String toString() {
        return "(" + sensorValue + "," + Arrays.toString(actions.toArray()) + ")";
    }
}

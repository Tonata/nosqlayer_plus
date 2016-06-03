package mapping.objectToNoSQL;

/**
 * Created by martian on 2016/06/03.
 */
public class Accumulator {

    private String results_thread;

    public Accumulator() {
        results_thread = "";
    }

    public void set(String tuple) {
        results_thread += tuple;
    }

    public String get() {
        return results_thread;
    }
}

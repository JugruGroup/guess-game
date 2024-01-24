package guess.util;

import java.util.ArrayList;
import java.util.List;

/**
 * OLAP utility methods.
 */
public class OlapUtils {
    private OlapUtils() {
    }

    public static List<Long> removeTrailingZeroes(List<Long> source) {
        List<Long> result = new ArrayList<>(source);

        while ((result.size() > 0) && (result.get(result.size() - 1) == 0)) {
            result.remove(result.size() - 1);
        }

        return result;
    }
}

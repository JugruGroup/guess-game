package guess.domain.source;

import java.util.List;

public class City extends Nameable {
    public City(long id, List<LocaleItem> name) {
        super(id, name);
    }

    @Override
    public String toString() {
        return "City{" +
                "id=" + getId() +
                ", name=" + getName() +
                '}';
    }
}

package guess.domain.source;

import java.util.List;

/**
 * Topic.
 */
public class Topic extends Nameable implements Comparable<Topic> {
    private boolean defaultTopic;
    private int orderNumber;

    public Topic() {
    }

    public Topic(long id, List<LocaleItem> name, boolean defaultTopic, int orderNumber) {
        super(id, name);
        this.defaultTopic = defaultTopic;
        this.orderNumber = orderNumber;
    }

    public boolean isDefaultTopic() {
        return defaultTopic;
    }

    public void setDefaultTopic(boolean defaultTopic) {
        this.defaultTopic = defaultTopic;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    @Override
    public int compareTo(Topic o) {
        return Integer.compare(orderNumber, o.orderNumber);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "Topic{" +
                "id=" + getId() +
                ", name=" + getName() +
                ", defaultTopic=" + defaultTopic +
                ", orderNumber=" + orderNumber +
                '}';
    }
}

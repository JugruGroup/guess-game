package guess.domain.statistics.olap;

import guess.domain.Identifier;

/**
 * OLAP 3D cube dimension.
 */
public class Olap3dCubeDimension extends Identifier {
    private final String name;

    public Olap3dCubeDimension(long id, String name) {
        super(id);

        this.name = name;
    }

    public String getName() {
        return name;
    }
}

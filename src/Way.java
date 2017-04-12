import com.vividsolutions.jts.geom.Coordinate;

import java.util.List;

/**
 * Created by Beata-MacBook on 11.04.2017.
 */
public class Way {

    public int id;
    public List<Long> listOfRefs;

    public Way(int id, List<Long> listOfRefs) {
        this.id = id;
        this.listOfRefs = listOfRefs;
    }

}

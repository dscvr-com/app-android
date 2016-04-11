package co.optonaut.optonaut.Cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Mariel on 4/11/2016.
 */
public class Cache {

    //<ID,<face,6faces(boolean)>>
    public Map<String,Map<String,List<Boolean>>> upload = new HashMap<>();
}

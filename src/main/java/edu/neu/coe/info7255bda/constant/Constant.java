package edu.neu.coe.info7255bda.constant;

public class Constant {

    public static final String DIR_PREFIX = "./src/main/resources";
    public final static String PLAN_SCHEMA_FILE_PATH = DIR_PREFIX + "/json/schema/PlanSchema.json";
    public final static String OTHER_SCHEMA_FILE_PATH = DIR_PREFIX + "/json/schema/membercostshareSchema.json";

    public final static String SPLIT = "_";

    public final static String OBJECT_ID = "objectId";
    public final static String OBJECT_TYPE = "objectType";
    public final static String SCHEMA = "planSchema";

    public static final String INDEX = "index-plan";
    public static final String JOIN_FIELD = "join_field";

    public static final String CHILD_PROP = "name";
    public static final String PARENT_PROP = "parent";

    public static final String LINKED_PREFIX = "linked_";
    public static final String LINKED_PROP = "membercostshare";
    public static final String BASIC_PROP = "plan";
    public static final String ROUTING_PROP = "_routing";

    public static final String ES_INDEX_QUEUE = "ESIndexingQueue";
    public static final String ES_UPDATE_QUEUE = "ESUpdateQueue";
    public static final String ES_DELETE_QUEUE = "ESDeleteQueue";
}

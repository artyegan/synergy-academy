package querries;

public class SqlQueries {
    public static String getAllQuery(String tableName) {
        return "select * from public." + tableName;
    }

    public static String getIdQuery(String tableName, String id) {
        return "select * from public.getstudentbyid(" + id + ")";
    }
}

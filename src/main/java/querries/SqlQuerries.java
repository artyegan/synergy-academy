package querries;

public class SqlQuerries {
    public static String getAllQuerry(String tableName) {
        return "select * from public." + tableName;
    }
}

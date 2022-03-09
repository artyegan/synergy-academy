package queries;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class SqlQueries {
    public static String getAllQuery(String tableName) {
        return new QueryBuilder("select")
                .all()
                .addFromTable(tableName)
                .getQuery();
    }

    public static String getIdQuery(String tableName, String id) {
        return "select * from public.getstudentbyid(" + id + ")";
    }

    public static String getFunctionQuery(String function, String... params) {
        return new QueryBuilder("select")
                .all()
                .addFromFunction(function, params)
                .getQuery();
    }

    public static String updateQuery(JsonArray metadata, String id, String tableName, JsonObject data) {
        List<String> classifiers = new ArrayList<>();

        QueryBuilder queryBuilder = new QueryBuilder("update")
                .addTable(tableName);

        for (int i = 0; i < metadata.size(); ++i) {
            String currentColumn = metadata.getJsonObject(i).getString("column_name");
//todo
            if (metadata.getJsonObject(i).getBoolean("isclassiferid")) {
                classifiers.add(currentColumn);
                queryBuilder.addFilterSetColumn("c_" + currentColumn.substring(0, currentColumn.length() - 2),
                        currentColumn, currentColumn);
            } else {
                queryBuilder.addFilterSet(currentColumn, data.getValue(currentColumn));
            }
        }

        for (int i = 0; i < classifiers.size(); ++i) {
            queryBuilder.addFromTable("c_" + classifiers.get(i).substring(0, classifiers.get(i).length() - 2));
        }

        for (int i = 0; i < classifiers.size(); ++i) {
            queryBuilder.addFilterWhere("c_" + classifiers.get(i).substring(0, classifiers.get(i).length() - 2),
                    "name", data.getValue(classifiers.get(i)));
        }

        return queryBuilder.addFilterWhere(tableName, tableName + "id", id).getQuery();
    }
}

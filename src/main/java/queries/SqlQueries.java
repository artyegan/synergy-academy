package queries;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class SqlQueries {
    public static QueryBuilder getAllQuery(String tableName) {
        return new QueryBuilder("select")
                .all()
                .addFromTable(tableName);
    }

    public static String getIdQuery(String tableName, String id) {
        return "select * from public.getstudentbyid(" + id + ")";
    }

    public static QueryBuilder getFunctionQuery(String function, String... params) {
        return new QueryBuilder("select")
                .all()
                .addFromFunction(function, params);
    }

    public static QueryBuilder updateQuery(JsonArray metadata, String id, String tableName, JsonObject data) {
        List<String> classifiers = new ArrayList<>();

        QueryBuilder queryBuilder = new QueryBuilder("update")
                .addTable(tableName);

        for (int i = 0; i < metadata.size(); ++i) {
            String currentColumn = metadata.getJsonObject(i).getString("column_name");

            if (!data.containsKey(currentColumn)) {
                continue;
            }
//todo
            if (metadata.getJsonObject(i).getBoolean("isclassiferid")) {
                classifiers.add(currentColumn);
                queryBuilder.addFilterSetColumn(modifyClassifierTable(currentColumn),
                        currentColumn, currentColumn);
            } else {
                queryBuilder.addFilterSet(currentColumn, data.getValue(currentColumn));
            }
        }

        for (String classifier : classifiers) {
            queryBuilder.addFromTable(modifyClassifierTable(classifier));
        }

        for (String classifier : classifiers) {
            queryBuilder.addFilterWhere(modifyClassifierTable(classifier),
                    "name", data.getValue(classifier));
        }

        return queryBuilder.addFilterWhere(tableName, tableName + "id", id);
    }

    public static QueryBuilder insertQuery(JsonArray metadata, String tableName, JsonObject data) {
        QueryBuilder queryBuilder = new QueryBuilder("insert into")
                .addTable(tableName)
                .openBrace();

        for (int i = 0; i < metadata.size(); ++i) {
            String currentColumn = metadata.getJsonObject(i).getString("column_name");

            if (!data.containsKey(currentColumn)) {
                continue;
            }

            queryBuilder.append(currentColumn);

            if (i < metadata.size() - 1) {
                queryBuilder.appendComma();
            } else {
                queryBuilder.closeBrace();
            }
        }

        return queryBuilder.append(selectQuery(metadata, tableName, data).getQuery());
    }

    public static QueryBuilder selectQuery(JsonArray metadata, String tableName, JsonObject data) {
        List<String> classifiers = new ArrayList<>();

        QueryBuilder queryBuilder = new QueryBuilder("select distinct");

        for (int i = 0; i < metadata.size(); ++i) {
            String currentColumn = metadata.getJsonObject(i).getString("column_name");

            if (!data.containsKey(currentColumn)) {
                continue;
            }

            if (metadata.getJsonObject(i).getBoolean("isclassiferid")) {
                classifiers.add(currentColumn);
                queryBuilder.addColumn(modifyClassifierTable(currentColumn), currentColumn);
            } else {
                if (metadata.getJsonObject(i).getString("data_type").equals("date")) {
                    queryBuilder.appendData(data.getValue(currentColumn)).append("::date");
                }
                else if (metadata.getJsonObject(i).getString("data_type").equals("numeric")) {
                    queryBuilder.appendData(data.getValue(currentColumn)).append("::numeric");
                }
                else {
                    queryBuilder.appendData(data.getValue(currentColumn));
                }
            }

            if (i < metadata.size() - 1) {
                queryBuilder.appendComma();
            }
        }

        queryBuilder.addFromTable(tableName);

        for (String classifier : classifiers) {
            queryBuilder.addLeftJoin(modifyClassifierTable(classifier), "name", data.getValue(classifier));
        }

        return queryBuilder;
    }


    public static QueryBuilder selectQuery(JsonArray metadata, String tableName) {
        List<String> classifiers = new ArrayList<>();

        QueryBuilder queryBuilder = new QueryBuilder("select");

        for (int i = 0; i < metadata.size(); ++i) {
            String currentColumn = metadata.getJsonObject(i).getString("column_name");

            if (metadata.getJsonObject(i).getBoolean("isclassiferid")) {
                classifiers.add(currentColumn); //todo
                queryBuilder.addColumn(modifyClassifierTable(currentColumn), "name").append("as ").append(currentColumn);
            } else {
                queryBuilder.addColumn(tableName, currentColumn);
            }

            if (i < metadata.size() - 1) {
                queryBuilder.appendComma();
            }
        }

        queryBuilder.addFromTable(tableName);

        for (String classifier : classifiers) {
            queryBuilder.addLeftJoinColumn(tableName, modifyClassifierTable(classifier), classifier, classifier);
        }

        handleClassifier(queryBuilder, tableName);

        return queryBuilder;
    }

    public static QueryBuilder selectQueryFilter(JsonArray metadata, String tableName, String filterColumn, String value) {
        QueryBuilder queryBuilder = selectQuery(metadata, tableName);
        queryBuilder.addFilterWhere(tableName, filterColumn, value);

        return queryBuilder;
    }

    private static String modifyClassifierTable(String classifierColumn) {
        return "c_" + classifierColumn.substring(0, classifierColumn.length() - 2);
    }

    private static void handleClassifier(QueryBuilder queryBuilder, String tableName) {
        if (tableName.startsWith("c_")) {
            queryBuilder.append("order by ").addColumn(tableName, "name");
        }
    }
}

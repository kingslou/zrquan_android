package com.zrquan.mobile.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "industries")
public class Industry {
    public static final String PARENT_INDUSTRY_ID_FIELD_NAME = "parent_industry_id";
    public static final String ID_FIELD_NAME = "id";

    // id is generated by the database and set on the object auto magically
    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    int id;
    @DatabaseField
    String name;
    @DatabaseField(canBeNull = true, columnName = PARENT_INDUSTRY_ID_FIELD_NAME)
    int parent_industry_id;

    Industry() {
        // needed by ormlite
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getParentIndustryId() {
        return parent_industry_id;
    }
}

/*
 * Copyright (c) 2012, Codename One and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Codename One designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Codename One through http://www.codenameone.com/ if you
 * need additional information or have any questions.
 */

package com.ixzdore.properties;

import com.codename1.db.Cursor;
import com.codename1.db.Database;
import com.codename1.db.Row;
import com.codename1.io.Log;
import com.codename1.properties.ListProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBase;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.ui.EncodedImage;
import com.codename1.util.Base64;
import com.codename1.util.StringUtil;
import com.ixzdore.restdb.ziemobject.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple ORM wrapper for property objects. This is a very poor mans ORM that doesn't handle relations
 * properly at this time.
 *
 * @author Shai Almog
 */
public class SQLMap {
    private boolean verbose = false;

    public enum SqlType {
        SQL_EXCLUDE(null),
        SQL_TEXT("TEXT") {
            @Override
            protected Object getValue(Row row, int index, PropertyBase base) throws IOException {
                String s = row.getString(index);
                if (s == null) {
                    return null;
                }
                //if it is of type that is a

                Class t = base.getGenericType();
                if (t != null) {
                    ////////////Log.p(base.getName() + " GETVAL " + t.getCanonicalName().toString());
                    if (t.getCanonicalName().indexOf("ziemobject") >= 0) {
                        //this is a propertybusinessobject
                        try {
                            PropertyBusinessObject po = (PropertyBusinessObject) t.newInstance();
                            po.getPropertyIndex().fromJSON(s);
                            return po.getPropertyIndex().loadJSONList(s);
                            //return po;
                        } catch (Exception e) {
                            //
                            e.printStackTrace();
                        }
                    }
                }

                return s;
            }

            @Override
            protected Object asUpdateInsertValue(Object data, Property p) {
                if (data == null) {
                    return null;
                }
                Class t = p.getGenericType();
                if (t != null) {
                    ////////////Log.p(p.getName() + "UPDATEV " + t.getCanonicalName().toString());

                    if (t.getCanonicalName().indexOf("ziemobject") >= 0) {
                        PropertyBusinessObject po = (PropertyBusinessObject) data;
                        return po.getPropertyIndex().toJSON();
                    }
                }
                return data.toString();
            }
        },
        SQL_INTEGER("INTEGER") {
            @Override
            protected Object getValue(Row row, int index, PropertyBase base) throws IOException {
                return row.getInteger(index);
            }
        },
        SQL_BOOLEAN("BOOLEAN") {
            @Override
            protected Object getValue(Row row, int index, PropertyBase base) throws IOException {
                Integer i = row.getInteger(index);
                if (i == null) {
                    return null;
                }
                return i.intValue() == 1;
            }
        },
        SQL_LONG("INTEGER") {
            @Override
            protected Object getValue(Row row, int index, PropertyBase base) throws IOException {
                return row.getLong(index);
            }
        },
        SQL_DATE("INTEGER") {
            @Override
            protected Object getValue(Row row, int index, PropertyBase base) throws IOException {
                return new Date(row.getLong(index) * 1000);
            }

            @Override
            protected Object asUpdateInsertValue(Object data, Property p) {
                if (data == null) {
                    return null;
                }
                return ((Date) data).getTime() / 1000;
            }
        },
        SQL_SHORT("INTEGER") {
            @Override
            protected Object getValue(Row row, int index, PropertyBase base) throws IOException {
                return row.getShort(index);
            }
        },
        SQL_FLOAT("REAL") {
            @Override
            protected Object getValue(Row row, int index, PropertyBase base) throws IOException {
                return row.getFloat(index);
            }
        },
        SQL_BLOB("TEXT") {
            @Override
            protected Object getValue(Row row, int index, PropertyBase base) throws IOException {
                String s = row.getString(index);
                if (s == null) {
                    return null;
                }
                byte[] d = Base64.decode(s.getBytes());
                Class t = base.getGenericType();
                if (t == EncodedImage.class) {
                    return EncodedImage.create(d);
                }
                return d;
            }

            @Override
            protected Object asUpdateInsertValue(Object data, Property p) {
                if (data == null) {
                    return null;
                }
                Class t = p.getGenericType();
                if (t == EncodedImage.class) {
                    return Base64.encode(((EncodedImage) data).getImageData());
                }
                return Base64.encode((byte[]) data);
            }
        },
        SQL_DOUBLE("REAL") {
            @Override
            protected Object getValue(Row row, int index, PropertyBase base) throws IOException {
                return row.getDouble(index);
            }
        };

        String dbType;

        SqlType(String dbType) {
            this.dbType = dbType;
        }

        protected Object getValue(Row row, int index, PropertyBase base) throws IOException {
            return row.getString(index);
        }

        protected Object asUpdateInsertValue(Object data, Property p) {
            return data;
        }
    }

    private Database db;

    private SQLMap() {
    }

    /**
     * Creates an SQL Map instance to the given database instance
     *
     * @param db the database connection instance
     * @return an instance of the SQL mapping
     */
    public static SQLMap create(Database db) {
        SQLMap s = new SQLMap();
        s.db = db;
        return s;
    }

    /**
     * Sets the primary key for the component
     *
     * @param cmp the business object
     * @param pk  the primary key field
     */
    public void setPrimaryKey(PropertyBusinessObject cmp, Property pk) {
        cmp.getPropertyIndex().putMetaDataOfClass("cn1$pk", pk.getName());
    }

    /**
     * Sets the primary key for the component and makes it auto-increment
     *
     * @param cmp the business object
     * @param pk  the primary key field
     */
    public void setPrimaryKeyAutoIncrement(PropertyBusinessObject cmp, Property pk) {
        cmp.getPropertyIndex().putMetaDataOfClass("cn1$pk", pk.getName());
        cmp.getPropertyIndex().putMetaDataOfClass("cn1$autoinc", Boolean.TRUE);
    }

    /**
     * Sets the sql type for the column
     *
     * @param p    the property
     * @param type one of the enum values representing supported SQL data types
     */
    public void setSqlType(PropertyBase p, SqlType type) {
        p.putClientProperty("cn1$colType", type);
    }

    /**
     * Returns the SQL type for the given column
     *
     * @param p the property
     * @return the sql data type
     */
    public SqlType getSqlType(PropertyBase p) {
        SqlType s = (SqlType) p.getClientProperty("cn1$colType");
        ////////////Log.p("Finding type of " + p.getName());
        if (s == null) {
            if (p instanceof Property) {
                Class gt = p.getGenericType();
                if (gt != null) {
                    ////////////Log.p(p.getName() + " Generic Type " + gt.getCanonicalName());
                    if (gt == Integer.class) {
                        return SqlType.SQL_INTEGER;
                    }
                    if (gt == Boolean.class) {
                        return SqlType.SQL_BOOLEAN;
                    }
                    if (gt == Long.class) {
                        return SqlType.SQL_LONG;
                    }
                    if (gt == Short.class) {
                        return SqlType.SQL_SHORT;
                    }
                    if (gt == Float.class) {
                        return SqlType.SQL_FLOAT;
                    }
                    if (gt == Double.class) {
                        return SqlType.SQL_DOUBLE;
                    }
                    if (gt == Date.class) {
                        return SqlType.SQL_DATE;
                    }
                    if (gt == EncodedImage.class || gt == byte[].class) {
                        return SqlType.SQL_BLOB;
                    }
                    return SqlType.SQL_TEXT;
                }
                Object val = ((Property) p).get();
                if (val != null) {
                    ////////////Log.p(p.getName() + " Val" + val.getClass().getCanonicalName());
                    ////////////Log.p(val.toString());
                    if (val instanceof PropertyBusinessObject) {
                        return SqlType.SQL_TEXT;
                    }
                    if (val instanceof Long) {
                        return SqlType.SQL_LONG;
                    }
                    if (val instanceof Integer) {
                        return SqlType.SQL_INTEGER;
                    }
                    if (val instanceof Short) {
                        return SqlType.SQL_SHORT;
                    }
                    if (val instanceof Float) {
                        return SqlType.SQL_FLOAT;
                    }
                    if (val instanceof Double) {
                        return SqlType.SQL_DOUBLE;
                    }
                    if (gt == Date.class) {
                        return SqlType.SQL_DATE;
                    }
                }
            }
            return SqlType.SQL_TEXT;
        }
        return s;
    }

    /**
     * By default the table name matches the property index name unless explicitly modified with this method
     *
     * @param cmp  the properties business object
     * @param name the name of the table
     */
    public void setTableName(PropertyBusinessObject cmp, String name) {
        cmp.getPropertyIndex().putMetaDataOfClass("cn1$tableName", name);
    }

    /**
     * By default the table name matches the property index name unless explicitly modified with this method
     *
     * @param cmp the properties business object
     * @return the name of the table
     */
    public String getTableName(PropertyBusinessObject cmp) {
        String s = (String) cmp.getPropertyIndex().getMetaDataOfClass("cn1$tableName");
        if (s != null) {
            return s;
        }
        return cmp.getPropertyIndex().getName();
    }

    /**
     * By default the column name matches the property name unless explicitly modified with this method
     *
     * @param prop a property instance, this will apply to all the property instances for the type
     * @param name the name of the column
     */
    public void setColumnName(PropertyBase prop, String name) {
        prop.putClientProperty("cn1$sqlColumn", name);
    }

    /**
     * By default the column name matches the property name unless explicitly modified with this method
     *
     * @param prop a property instance, this will apply to all the property instances for the type
     * @return the name of the property
     */
    public String getColumnName(PropertyBase prop) {
        String val = (String) prop.getClientProperty("cn1$sqlColumn");
        if (val == null) {
            return prop.getName();
        }
        return val;
    }

    /**
     * Creates a table matching the given property component if one doesn't exist yet
     *
     * @param cmp the business object
     * @return true if the table was created false if it already existed
     */
    public boolean createTable(PropertyBusinessObject cmp) throws IOException {
        String tableName = getTableName(cmp);
        Cursor cr = null;
        boolean has = false;
        try {
            cr = executeQuery("SELECT * FROM sqlite_master WHERE type='table' AND name='" + tableName + "'");
            has = cr.next();
        } finally {
            if (cr != null) {
                cr.close();
            }
        }
        if (has) {
            return false;
        }
        StringBuilder createStatement = new StringBuilder("CREATE TABLE IF NOT EXiSTS ");
        createStatement.append(tableName);
        createStatement.append(" (");

        String pkName = (String) cmp.getPropertyIndex().getMetaDataOfClass("cn1$pk");
        boolean autoIncrement = cmp.getPropertyIndex().getMetaDataOfClass("cn1$autoinc") != null;
        boolean first = true;
        for (PropertyBase p : cmp.getPropertyIndex()) {
            SqlType tp = getSqlType(p);
            if (tp == SqlType.SQL_EXCLUDE) {
                continue;
            }
            if (!first) {
                createStatement.append(",");
            }
            first = false;
            String columnName = getColumnName(p);
            createStatement.append(columnName);
            createStatement.append(" ");
            createStatement.append(tp.dbType);
            if (columnName.equalsIgnoreCase(pkName)) {
                createStatement.append(" PRIMARY KEY");
                if (autoIncrement) {
                    createStatement.append(" AUTOINCREMENT");
                }
            }
        }

        createStatement.append(")");

        execute(createStatement.toString());
        return true;
    }

    private void execute(String stmt) throws IOException {
        if (verbose) {
            ////////////Log.p(stmt);
        }
        //////////Log.p("Executing \n"  + stmt);
        db.execute(stmt);
    }

    private void execute(String stmt, Object[] args) throws IOException {
        if (verbose) {
            ////////////Log.p(stmt);
        }
        db.execute(stmt, args);
    }

    private Cursor executeQuery(String stmt, Object[] args) throws IOException {
        if (verbose) {
            // //////////Log.p(stmt);
        }
        return db.executeQuery(stmt, args);
    }

    private Cursor executeQuery(String stmt) throws IOException {
        if (verbose) {
            ////////////Log.p(stmt);
        }
        return db.executeQuery(stmt);
    }

    /**
     * Drop a table matching the given property component
     *
     * @param cmp the business object
     */
    public void dropTable(PropertyBusinessObject cmp) throws IOException {
        String tableName = getTableName(cmp);
        execute("Drop table " + tableName);
    }

    /**
     * Adds a new business object into the database
     *
     * @param cmp the business component
     */
    public void insert(PropertyBusinessObject cmp) throws IOException {
        String tableName = getTableName(cmp);
        StringBuilder createStatement = new StringBuilder("REPLACE INTO ");
        createStatement.append(tableName);
        createStatement.append(" (");

        int count = 0;
        ArrayList<Object> values = new ArrayList<Object>();
        for (PropertyBase p : cmp.getPropertyIndex()) {
            SqlType tp = getSqlType(p);
            if (tp == SqlType.SQL_EXCLUDE) {
                continue;
            }
            if (count > 0) {
                createStatement.append(",");
            }
            if (p instanceof Property) {
                //if (p.getGenericType() != null)
                //   p.getGenericType().getCanonicalName();
                ////////////Log.p("Value " + tp.asUpdateInsertValue(((Property)p).get(), (Property)p)
                //       );
                values.add(tp.asUpdateInsertValue(((Property) p).get(), (Property) p));
            } else {
                // TODO
                String v = "[";
                if (p.getGenericType() != null) {
                    ////////////Log.p("Value " + p.getName() + " will be null but type is " +
                    //       p.getGenericType().getCanonicalName());
                    if (p.getGenericType().getCanonicalName().indexOf("ziemobject") >= 0) {
                        //it must be a list
                        if (p instanceof ListProperty) {
                            ////////////Log.p("Its a list property");

                            List<PropertyBusinessObject> a = ((ListProperty) p).asList();
                            for (PropertyBusinessObject o : a) {
                                // //////////Log.p(o.getPropertyIndex().toString());
                                //v=v+StringUtil.replaceAll( o.getPropertyIndex().toJSON(),
                                //        "\n",",");
                                v = v + o.getPropertyIndex().toJSON();
                            }
                        }
                    }
                } else {
                    //this could be an array of Strings or an array
                    //of ids of PropertyBusinessObjects
                    //How do we determine which is which/
                    if (p instanceof ListProperty) {
                        ////////////Log.p("it is a list property");
                        List<Object> a = ((ListProperty) p).asList();
                        for (Object o : a) {
                            v = v + o.toString();
                        }
                    }
                }
                v = v + "]";
                values.add(v);
            }
            count++;
            String columnName = getColumnName(p);
            ////////////Log.p("Columname is " + columnName);
            createStatement.append(columnName);
        }

        createStatement.append(") VALUES (?");

        for (int iter = 1; iter < values.size(); iter++) {
            createStatement.append(",?");
        }
        createStatement.append(")");
        //Log.p(createStatement.toString());
        //for (Object o:values){
        //    //////////Log.p("\n"+o.toString());
        // }
        execute(createStatement.toString(), values.toArray());
    }

    /**
     * The equivalent of an SQL update assumes that the object is already in the database
     *
     * @param cmp the component
     * @throws IOException
     */
    public void update(PropertyBusinessObject cmp) throws IOException {
        String pkName = (String) cmp.getPropertyIndex().getMetaDataOfClass("cn1$pk");
        if (pkName == null) {
            throw new IOException("Primary key required for update");
        }
        String tableName = getTableName(cmp);
        StringBuilder createStatement = new StringBuilder("UPDATE ");
        createStatement.append(tableName);
        createStatement.append(" SET ");

        int count = 0;
        ArrayList<Object> values = new ArrayList<Object>();
        for (PropertyBase p : cmp.getPropertyIndex()) {
            SqlType tp = getSqlType(p);
            if (tp == SqlType.SQL_EXCLUDE) {
                continue;
            }
            if (count > 0) {
                createStatement.append(",");
            }
            if (p instanceof Property) {
                values.add(tp.asUpdateInsertValue(((Property) p).get(), (Property) p));
            } else {
                // TODO
                values.add(null);
            }
            count++;
            String columnName = getColumnName(p);
            createStatement.append(columnName);
            createStatement.append(" = ?");
        }

        createStatement.append(" WHERE ");

        createStatement.append(pkName);
        createStatement.append(" = ?");

        Property p = (Property) cmp.getPropertyIndex().getIgnoreCase(pkName);
        values.add(p.get());

        execute(createStatement.toString(), values.toArray());
    }

    /**
     * Deletes a table row matching the component
     *
     * @param cmp the component
     */
    public void delete(PropertyBusinessObject cmp) throws IOException {
        String pkName = (String) cmp.getPropertyIndex().getMetaDataOfClass("cn1$pk");
        String tableName = getTableName(cmp);
        StringBuilder createStatement = new StringBuilder("DELETE FROM ");
        createStatement.append(tableName);
        createStatement.append(" WHERE ");

        if (pkName != null) {
            createStatement.append(pkName);
            createStatement.append(" = ?");
            Property p = (Property) cmp.getPropertyIndex().getIgnoreCase(pkName);
            execute(createStatement.toString(), new Object[] { p.get() });
        } else {
            int count = 0;
            Object[] values = new Object[cmp.getPropertyIndex().getSize()];
            for (PropertyBase p : cmp.getPropertyIndex()) {
                if (count == 0) {
                    String bs = createStatement.toString();
                    ////////////Log.p(bs + "Ends With WHERE " + bs.trim().endsWith("WHERE"));
                    if (!bs.trim().endsWith("WHERE"))
                        createStatement.append(",");
                }
                if (p instanceof Property) {
                    values[count] = ((Property) p).get();
                } else {
                    // TODO
                    values[count] = null;
                }
                count++;
                String columnName = getColumnName(p);
                if (createStatement.toString().trim().endsWith(",") ||
                        (createStatement.toString().trim().endsWith("WHERE"))) {
                    createStatement.append(columnName);
                } else {
                    createStatement.append(",").append(columnName);
                }
                createStatement.append(" = ?");
            }

            createStatement.append(")");

            execute(createStatement.toString(), values);
        }
    }

    /**
     * Fetches the components from the database matching the given cmp description, the fields that aren't
     * null within the cmp will match the where clause
     *
     * @param cmp         the component to match
     * @param orderBy     the column to order by, can be null to ignore order
     * @param ascending   true to indicate ascending order
     * @param maxElements the maximum number of elements returned can be 0 or lower to ignore
     * @param page        the page within the query to match the max elements value
     * @return the result of the query
     */
    public java.util.List<PropertyBusinessObject> select(
            PropertyBusinessObject cmp,
            Property orderBy,
            boolean ascending,
            int maxElements,
            int page) throws IOException, InstantiationException {
        String tableName = getTableName(cmp);
        StringBuilder createStatement = new StringBuilder("SELECT * FROM ");
        createStatement.append(tableName);
        ArrayList<Object> params = new ArrayList<Object>();

        createStatement.append(" WHERE ");
        boolean found = false;
        for (PropertyBase p : cmp.getPropertyIndex()) {
            if (p instanceof Property) {
                if (((Property) p).get() != null) {
                    if (found) {
                        createStatement.append(" AND ");
                    }
                    found = true;
                    params.add(((Property) p).get());
                    createStatement.append(getColumnName(p));
                    createStatement.append(" = ?");
                }
            }
        }

        // all properties are null undo the where append
        if (!found) {
            createStatement = new StringBuilder("SELECT * FROM ");
            createStatement.append(tableName);
        }

        if (orderBy != null) {
            createStatement.append(" ORDER BY ");
            createStatement.append(getColumnName(orderBy));
            if (!ascending) {
                createStatement.append(" DESC");
            }
        }

        if (maxElements > 0) {
            createStatement.append(" LIMIT ");
            createStatement.append(maxElements);
            if (page > 0) {
                createStatement.append(" OFFSET ");
                createStatement.append(page * maxElements);
            }
        }

        Cursor c = null;
        try {
            ArrayList<PropertyBusinessObject> response = new ArrayList<PropertyBusinessObject>();
            c = executeQuery(createStatement.toString(), params.toArray());
            while (c.next()) {
                PropertyBusinessObject pb = (PropertyBusinessObject) cmp.getClass().newInstance();
                for (PropertyBase p : pb.getPropertyIndex()) {
                    Row currentRow = c.getRow();
                    SqlType t = getSqlType(p);
                    if (t == SqlType.SQL_EXCLUDE) {
                        continue;
                    }
                    Object value = t.getValue(currentRow, c.getColumnIndex(getColumnName(p)), p);
                    if (p instanceof Property) {
                        ((Property) p).set(value);
                    }
                }
                response.add(pb);
            }
            c.close();
            return response;
        } catch (Throwable t) {
            Log.e(t);
            if (c != null) {
                c.close();
            }
            if (t instanceof IOException) {
                throw ((IOException) t);
            } else {
                throw new IOException(t.toString());
            }
        }
    }

    /*
     * Adds a batch of new business object into the database
     *
     * @param cmp the business component
     */
    public void bulkInsert(List<PropertyBusinessObject> lmp) throws IOException {
        PropertyBusinessObject cmp = lmp.get(0);

        String tableName = getTableName(cmp);
        StringBuilder createStatement = new StringBuilder("REPLACE INTO ");
        createStatement.append(tableName);
        createStatement.append(" (");
        //we setup for the first object
        int count = 0;
        ArrayList<Object> values = new ArrayList<Object>();
        for (PropertyBase p : cmp.getPropertyIndex()) {
            SqlType tp = getSqlType(p);
            if (tp == SqlType.SQL_EXCLUDE) {
                continue;
            }
            if (count > 0) {
                createStatement.append(",");
            }
            if (p instanceof Property) {
                //if (p.getGenericType() != null)
                //   p.getGenericType().getCanonicalName();
                ////////////Log.p("Value " + tp.asUpdateInsertValue(((Property)p).get(), (Property)p)
                //       );
                values.add(tp.asUpdateInsertValue(((Property) p).get(), (Property) p));
            } else {
                // TODO
                String v = "[";
                if (p.getGenericType() != null) {
                    ////////////Log.p("Value " + p.getName() + " will be null but type is " +
                    //       p.getGenericType().getCanonicalName());
                    if (p.getGenericType().getCanonicalName().indexOf("ziemobject") >= 0) {
                        //it must be a list
                        if (p instanceof ListProperty) {
                            ////////////Log.p("Its a list property");

                            List<PropertyBusinessObject> a = ((ListProperty) p).asList();
                            for (PropertyBusinessObject o : a) {
                                // //////////Log.p(o.getPropertyIndex().toString());
                                //v=v+StringUtil.replaceAll( o.getPropertyIndex().toJSON(),
                                //        "\n",",");
                                v = v + o.getPropertyIndex().toJSON();
                            }
                        }
                    }
                } else {
                    //this could be an array of Strings or an array
                    //of ids of PropertyBusinessObjects
                    //How do we determine which is which/
                    if (p instanceof ListProperty) {
                        ////////////Log.p("it is a list property");
                        List<Object> a = ((ListProperty) p).asList();
                        for (Object o : a) {
                            v = v + o.toString();
                        }
                    }
                }
                v = v + "]";
                values.add(v);
            }
            count++;
            String columnName = getColumnName(p);
            ////////////Log.p("Columname is " + columnName);
            createStatement.append(columnName);
        }

        createStatement.append(") VALUES (");
        //now we use buildPatch to create the values 
        int i = 0;
        for (PropertyBusinessObject po : lmp) {
            ////////Log.p(po.getPropertyIndex().toJSON());
            Map<String, Object> batch = buildBatch(po);
            values.clear();
            values.addAll((ArrayList<Object>) batch.get("values"));
            if (i > 0)
                createStatement.append("(");
            for (int iter = 0; iter < values.size(); iter++) {
                String value = "";
                if (values.get(iter) != null) {
                    value = values.get(iter).toString();
                    if (value.length() < 1)
                        value = "NULL";
                } else {
                    value = "NULL";
                }
                value = StringUtil.replaceAll(value, "'", "''");
                if (iter < values.size() - 1)
                    createStatement.append("'" + value + "'" + ",\n");
                if (iter == values.size() - 1)
                    createStatement.append("'" + value + "'" + "\n");
            }
            i++;
            createStatement.append("),");
            ////////////Log.p(createStatement.toString());
            //for (Object o:values){
            //    //////////Log.p("\n"+o.toString());
        }
        String stmnt = "";
        String cs = createStatement.toString();
        stmnt = cs.substring(0, cs.length() - 1);
        //now we can execute the statement
        //////Log.p("This is the bulk insert statement to execute " + stmnt);
        execute(stmnt);
    }

    /**
     * buildBatch - get the batch value to add to the batch insert statement
     *
     * @param po
     * @return HashMap<String, Object> containing the statement and the values.
     */
    public Map<String, Object> buildBatch(PropertyBusinessObject po) {
        HashMap<String, Object> batch = new HashMap<String, Object>();
        StringBuilder createStatement = new StringBuilder("REPLACE INTO ");
        int count = 0;
        ArrayList<Object> values = new ArrayList<Object>();
        for (PropertyBase p : po.getPropertyIndex()) {
            SqlType tp = getSqlType(p);
            if (tp == SqlType.SQL_EXCLUDE) {
                continue;
            }
            if (count > 0) {
                createStatement.append(",");
            }
            if (p instanceof Property) {
                //if (p.getGenericType() != null)
                //   p.getGenericType().getCanonicalName();
                ////////////Log.p("Value " + tp.asUpdateInsertValue(((Property)p).get(), (Property)p)
                //       );
                values.add(tp.asUpdateInsertValue(((Property) p).get(), (Property) p));
            } else {
                // TODO
                String v = "[";
                if (p.getGenericType() != null) {
                    ////////////Log.p("Value " + p.getName() + " will be null but type is " +
                    //       p.getGenericType().getCanonicalName());
                    if (p.getGenericType().getCanonicalName().indexOf("ziemobject") >= 0) {
                        //it must be a list
                        if (p instanceof ListProperty) {
                            ////////////Log.p("Its a list property");

                            List<PropertyBusinessObject> a = ((ListProperty) p).asList();
                            for (PropertyBusinessObject o : a) {
                                // //////////Log.p(o.getPropertyIndex().toString());
                                //v=v+StringUtil.replaceAll( o.getPropertyIndex().toJSON(),
                                //        "\n",",");
                                v = v + o.getPropertyIndex().toJSON();
                            }
                        }
                    }
                } else {
                    //this could be an array of Strings or an array
                    //of ids of PropertyBusinessObjects
                    //How do we determine which is which/
                    if (p instanceof ListProperty) {
                        ////////////Log.p("it is a list property");
                        List<Object> a = ((ListProperty) p).asList();
                        for (Object o : a) {
                            v = v + o.toString();
                        }
                    }
                }
                v = v + "]";
                values.add(v);
            }
            count++;
            String columnName = getColumnName(p);
            ////////////Log.p("Columname is " + columnName);
            createStatement.append(columnName);
        }

        createStatement.append(") VALUES (?");

        for (int iter = 1; iter < values.size(); iter++) {
            createStatement.append(",?");
        }
        createStatement.append(")");
        batch.put("statement", createStatement);
        batch.put("values", values);

        return batch;
    }

    /**
     * Toggle verbose mode
     *
     * @param verbose
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}

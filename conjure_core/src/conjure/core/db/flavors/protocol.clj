(ns conjure.core.db.flavors.protocol)

(defprotocol Flavor
  "A protocol for the database flavors. This protocol includes all functions used by Conjure to interface with the
   database. To add a datbase, simply implement this protocol for the database and set the instance in
   config.db-config."

  (db-map [flavor] "Returns a map for use in db-config.")
  (execute-query [flavor sql-vector] "Executes an sql string and returns the results as a sequence of maps.")
  (update [flavor table where-params record]
    "Runs an update given the table, where-params and a record.

      table - The name of the table to update.
      where-params - The parameters to test for.
      record - A map from strings or keywords (identifying columns) to updated values.")
  (insert-into [flavor table records]
    "Runs an insert given the table, and a set of records.

      table - The name of the table to update.
      records - A map from strings or keywords (identifying columns) to updated values.")
  (table-exists? [flavor table] "Returns true if the table with the given name exists.")
  (sql-find [flavor select-map]
    "Runs an sql select statement built from the given select-map. The valid keys are:

        table - the table to run the select statement on
        select - the columns to return
        where - the conditions")
  (create-table [flavor table specs]
    "Creates a new table with the given name and with columns based on the given specs.")
  (drop-table [flavor table] "Deletes the table with the given name.")
  (describe-table [flavor table] "Shows the columns of the given table.")
  (delete [flavor table where] "Deletes rows from the table with the given name.")
  (integer [flavor column] [flavor column mods] 
    "Returns a new spec describing an integer with the given column and spec mods map. Use this method with the 
     create-table method.

     Curently supported values for mods:
         :not-null - If the value of this key resolves to true, then add this column will be forced to be not null.
         :primary-key - If true, then make this column the primary key.")
  (id [flavor] "Returns a new spec describing the id for a table. Use this method with the create-table method.")
  (string [flavor column] [flavor column mods] 
    "Returns a new spec describing a string with the given column and spec mods map. Use this method with the
     create-table method.

     Curently supported values for mods:
         :length - The length of the varchar, if not present then the varchar defaults to 255.
         :not-null - If the value of this key resolves to true, then add this column will be forced to be not null.
         :primary-key - If true, then make this column the primary key.")
  (text [flavor column] [flavor column mods]
    "Returns a new spec describing a text with the given column and spec mods map. Use this method with the create-table
     method.

     Curently supported values for mods: None")
  (date [flavor column] [flavor column mods]
    "Returns a new spec describing a date with the given column and spec mods map. Use this method with the create-table
     method.

     Curently supported values for mods: None")
  (time-type [flavor column] [flavor column mods] 
    "Returns a new spec describing a time with the given column and spec mods map. Use this method with the create-table
     method.

     Curently supported values for mods: None")
  (date-time [flavor column] [flavor column mods]
    "Returns a new spec describing a date time with the given column and spec mods map. Use this method with the
     create-table method.

     Curently supported values for mods: None")
  (belongs-to [flavor model] [flavor model mods]
    "Returns a new spec describing a text with the given column and spec mods map. Use this method with the create-table
     method.

     Curently supported values for mods is exactly the same as integer.")
  (format-date [flavor date] "Returns the string value of the given date for use in the database.")
  (format-date-time [flavor date] "Returns the string value of the given date as a date time for use in the database.")
  (format-time [flavor date] "Returns the string value of the given date as a time for use in the database."))
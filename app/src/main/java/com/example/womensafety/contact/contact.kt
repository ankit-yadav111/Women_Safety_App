package com.example.womensafety.contact

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Contact_table")
class contact (@ColumnInfo(name="Text")val text:String){
    @PrimaryKey(autoGenerate = true) var id=0
}
/**
 * Created by Ilia Shelkovenko on 13.07.2020.
 */

package ru.skillbranch.gameofthrones.data.local.converters

import androidx.room.TypeConverter
import ru.skillbranch.gameofthrones.ui.houses.house.HouseType

class GoTConverter {
    @TypeConverter
    fun fromString(value: String) : List<String> = value.split(";")

    @TypeConverter
    fun fromArrayList(list: List<String>) : String = list.joinToString(";")

    @TypeConverter
    fun fromTitle(value: String) : HouseType = HouseType.fromString(value)

    @TypeConverter
    fun fromEnum(anEnum: HouseType) : String = anEnum.title

}
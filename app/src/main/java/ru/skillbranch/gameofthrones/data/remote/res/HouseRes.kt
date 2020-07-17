package ru.skillbranch.gameofthrones.data.remote.res

import ru.skillbranch.gameofthrones.data.local.entities.House
import ru.skillbranch.gameofthrones.extensions.dropLastUntil

data class HouseRes(
    val url: String,
    val name: String,
    val region: String,
    val coatOfArms: String,
    val words: String,
    val titles: List<String> = listOf(),
    val seats: List<String> = listOf(),
    val currentLord: String,
    val heir: String,
    val overlord: String,
    val founded: String,
    val founder: String,
    val diedOut: String,
    val ancestralWeapons: List<String> = listOf(),
    val cadetBranches: List<Any> = listOf(),
    val swornMembers: List<String> = listOf()
) : IRes {
    fun toHouse(): House {
        return House(
            url.lastSegment(),
            name,
            region,
            coatOfArms,
            words,
            titles,
            seats,
            currentLord,
            heir,
            overlord,
            founded,
            founder,
            diedOut,
            ancestralWeapons)
    }

    override val id: String
        get() = url.lastSegment()

    val shortName : String
        get() = name.split(" ").dropLastUntil { it == "of" }

    val members : List<String>
        get() = swornMembers.map { it.lastSegment() }

}
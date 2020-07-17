package ru.skillbranch.gameofthrones.repositories

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import kotlinx.coroutines.*
import ru.skillbranch.gameofthrones.AppConfig
import ru.skillbranch.gameofthrones.data.local.DbManager
import ru.skillbranch.gameofthrones.data.local.dao.CharacterDao
import ru.skillbranch.gameofthrones.data.local.dao.HouseDao
import ru.skillbranch.gameofthrones.data.local.entities.CharacterFull
import ru.skillbranch.gameofthrones.data.local.entities.CharacterItem
import ru.skillbranch.gameofthrones.data.local.entities.House
import ru.skillbranch.gameofthrones.data.remote.NetworkService
import ru.skillbranch.gameofthrones.data.remote.RestService
import ru.skillbranch.gameofthrones.data.remote.res.CharacterRes
import ru.skillbranch.gameofthrones.data.remote.res.HouseRes
import ru.skillbranch.gameofthrones.data.local.entities.Character

object RootRepository {
    private val api : RestService = NetworkService.api
    private val houseDao : HouseDao = DbManager.db.houseDao()
    private val characterDao : CharacterDao = DbManager.db.characterDao()

    private val errHandler = CoroutineExceptionHandler { _, exception ->
        println("Caught $exception")
        exception.printStackTrace()
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + errHandler)
    /**
     * Получение данных о всех домах из сети
     * @param result - колбек содержащий в себе список данных о домах
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getAllHouses(result : (houses : List<HouseRes>) -> Unit) {
        scope.launch {
            var resultList = mutableListOf<HouseRes>()
            scope.launch {
                var page = 1
                var isInsert = false
                do{
                    isInsert = resultList.addAll(api.houses(page))
                    page++
                }while (isInsert)
            }.join()
            result(resultList)
        }
    }

    /**
     * Получение данных о требуемых домах по их полным именам из сети 
     * @param houseNames - массив полных названий домов (смотри AppConfig)
     * @param result - колбек содержащий в себе список данных о домах
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getNeedHouses(vararg houseNames: String, result : (houses : List<HouseRes>) -> Unit) {
        scope.launch {
            val houses = mutableListOf<HouseRes>()
            houseNames.forEach {
                val res = houses.add(api.houseByName(it).first())
            }
            result(houses)
        }
    }

    /**
     * Получение данных о требуемых домах по их полным именам и персонажах в каждом из домов из сети
     * @param houseNames - массив полных названий домов (смотри AppConfig)
     * @param result - колбек содержащий в себе список данных о доме и персонажей в нем (Дом - Список Персонажей в нем)
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getNeedHouseWithCharacters(vararg houseNames: String, result : (houses : List<Pair<HouseRes, List<CharacterRes>>>) -> Unit) {
        val resultList = mutableListOf<Pair<HouseRes,List<CharacterRes>>>()
        var houses : List<HouseRes> = listOf()
        getNeedHouses(*houseNames) {
            houses = it
            scope.launch{
                scope.launch {
                    houses.forEach { house ->
                        val characters = mutableListOf<CharacterRes>()
                        resultList.add(house to characters)
                        house.members.forEach { characterId ->
                            launch (CoroutineName("character $characterId")){
                                api.character(characterId)
                                    .apply { houseId = house.shortName }
                                    .also { characters.add(it) }
                            }
                        }
                    }

                }.join()
                result(resultList)
            }
        }
    }

    /**
     * Запись данных о домах в DB
     * @param houses - Список персонажей (модель HouseRes - модель ответа из сети)
     * необходимо произвести трансформацию данных
     * @param complete - колбек о завершении вставки записей db
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun insertHouses(houses : List<HouseRes>, complete: () -> Unit) {
        val list = houses.map {it.toHouse()}
        scope.launch {
            houseDao.upsert(list)
            complete()
        }
    }

    /**
     * Запись данных о пересонажах в DB
     * @param Characters - Список персонажей (модель CharacterRes - модель ответа из сети)
     * необходимо произвести трансформацию данных
     * @param complete - колбек о завершении вставки записей db
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun insertCharacters(characters : List<CharacterRes>, complete: () -> Unit) {
        val list = characters.map {it.toCharacter()}
        scope.launch {
            characterDao.upsert(list)
            complete()
        }
    }

    /**
     * При вызове данного метода необходимо выполнить удаление всех записей в db
     * @param complete - колбек о завершении очистки db
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun dropDb(complete: () -> Unit) {
        scope.launch {
            if(houseDao.recordsCount() != 0) {
                characterDao.deleteTable()
                houseDao.deleteTable()
            }
            complete()
        }
    }

    /**
     * Поиск всех персонажей по имени дома, должен вернуть список краткой информации о персонажах
     * дома - смотри модель CharacterItem
     * @param name - краткое имя дома (его первычный ключ)
     * @param result - колбек содержащий в себе список краткой информации о персонажах дома
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun findCharactersByHouseName(name : String, result: (characters : List<CharacterItem>) -> Unit) {
        var list : List<CharacterItem> = listOf()
        scope.launch {
            list = characterDao.findCharacterList(name)
            result(list)
        }
    }

    /**
     * Поиск персонажа по его идентификатору, должен вернуть полную информацию о персонаже
     * и его родственных отношения - смотри модель CharacterFull
     * @param id - идентификатор персонажа
     * @param result - колбек содержащий в себе полную информацию о персонаже
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun findCharacterFullById(id : String, result: (character : CharacterFull) -> Unit) {
        scope.launch {
            result(CharacterFull("","","","","", listOf(), listOf(),"",null,null))
        }
    }

    /*fun findCharacterById(id : String, result: (character : Character) -> Unit) {
        scope.launch {
            val characterFull = characterDao.findCharacter(id)
            result(characterFull)
        }
    }*/

    /**
     * Метод возвращет true если в базе нет ни одной записи, иначе false
     * @param result - колбек о завершении очистки db
     */
    fun isNeedUpdate(result: (isNeed : Boolean) -> Unit){
        scope.launch {
            if(houseDao.recordsCount() == 0)
                result(true)
            else
                result(false)
        }
    }
    fun sync(result : () -> Unit) {
        Log.d("RootViewModel", "in sync")
        val initial = mutableListOf<House>() to mutableListOf<Character>()
        val pairs = getNeedHouseWithCharacters(*AppConfig.NEED_HOUSES){
            val list = it.fold(initial) {acc, (houseRes, characterResList) ->
                val house = houseRes.toHouse()
                val characters = characterResList.map { it.toCharacter() }
                acc.also {(hs,ch) ->
                    hs.add(house)
                    ch.addAll(characters)
                }
            }
            houseDao.upsert(list.first)
            characterDao.upsert(list.second)
            result()
        }
    }

    fun getCharactersByName(title : String) : LiveData<List<CharacterItem>>{
        return characterDao.findCharacters(title)
    }

    /*fun getCharacterById(characterId: String): LiveData<Character> {
        return characterDao.findCharacterLive(characterId)
    }*/

    /*fun getCharacterFullById(characterId: String) : LiveData<CharacterFull>{
        return characterDao.findCharacter(characterId)
    }*/

}
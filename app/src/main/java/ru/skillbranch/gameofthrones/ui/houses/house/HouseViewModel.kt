/**
 * Created by Ilia Shelkovenko on 12.07.2020.
 */

package ru.skillbranch.gameofthrones.ui.houses.house

import android.util.Log
import androidx.lifecycle.*
import ru.skillbranch.gameofthrones.data.local.entities.CharacterItem
import ru.skillbranch.gameofthrones.repositories.RootRepository
import java.lang.IllegalArgumentException
import ru.skillbranch.gameofthrones.extensions.combineAndCompute

class HouseViewModel(private val houseName : String) : ViewModel()  {
    private val repository = RootRepository
    private val queryString = MutableLiveData<String>("")

    fun getCharacters() : LiveData<List<CharacterItem>> {
        val characters = repository.getCharactersByName(houseName)
        Log.d("HouseViewModel", "${characters.value?.size}")
        return characters.combineAndCompute(queryString) {list,query->
            if(query.isEmpty()) list
            else list.filter { it.name.contains(query,true) }
        }
    }

    fun handleSearchQuery(searchStr : String){
        queryString.value = searchStr
    }
}

class HouseViewModelFactory(private val houseName: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(HouseViewModel::class.java)){
            return HouseViewModel(houseName) as T
        }
        throw  IllegalArgumentException("unknown ViewModel class")
    }
}
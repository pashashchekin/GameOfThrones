/**
 * Created by Ilia Shelkovenko on 12.07.2020.
 */

package ru.skillbranch.gameofthrones.ui.character

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import ru.skillbranch.gameofthrones.data.local.entities.CharacterFull
import ru.skillbranch.gameofthrones.extensions.combineAndCompute
import ru.skillbranch.gameofthrones.repositories.RootRepository
import java.lang.IllegalArgumentException
import ru.skillbranch.gameofthrones.data.local.entities.Character

class CharacterViewModel(private val characterId : String) : ViewModel() {
    private val repository = RootRepository

    fun getCharacter() : LiveData<Character> {
        /*val character = repository.getCharacterById(characterId)
        return character*/
        return liveData {  }
    }

}

class CharacterViewModelFactory(private val characterId : String) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(CharacterViewModel::class.java)){
            return CharacterViewModel(characterId) as T
        }
        throw  IllegalArgumentException("unknown ViewModel class")
    }
}
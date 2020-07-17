package ru.skillbranch.gameofthrones.ui.houses.house

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_house.*

import ru.skillbranch.gameofthrones.R
import ru.skillbranch.gameofthrones.data.local.entities.CharacterItem
import ru.skillbranch.gameofthrones.ui.houses.HousesFragmentDirections

/**
 * A simple [Fragment] subclass.
 */
class HouseFragment : Fragment() {
    private lateinit var charactersAdapter : CharactersAdapter
    private lateinit var houseViewModel : HouseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val houseName = arguments?.getString(HOUSE_NAME) ?: HouseType.STARK.title
        val vmFactory = HouseViewModelFactory(houseName)
        charactersAdapter = CharactersAdapter {
            val action =  HousesFragmentDirections.actionNavHousesToNavCharacter(it.id,it.house,it.name)
            findNavController().navigate(action)
        }
        houseViewModel = ViewModelProviders.of(this,vmFactory).get(HouseViewModel::class.java)
        houseViewModel.getCharacters().observe(this, Observer<List<CharacterItem>> {
            charactersAdapter.submitList(it)
        })
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        with(menu.findItem(R.id.action_search).actionView as SearchView){
            setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String): Boolean {
                    houseViewModel.handleSearchQuery(query)
                    return true
                }
                override fun onQueryTextChange(newText: String): Boolean {
                    houseViewModel.handleSearchQuery(newText)
                    return true
                }
            })
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_house, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_characters.apply {
            adapter = charactersAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        recycler_characters.addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
    }

    companion object {
        private const val HOUSE_NAME = "house_name"

        fun newInstance(houseName : String) : HouseFragment {
            return HouseFragment().apply {
                arguments = bundleOf(HOUSE_NAME to houseName)
            }
        }
    }
}

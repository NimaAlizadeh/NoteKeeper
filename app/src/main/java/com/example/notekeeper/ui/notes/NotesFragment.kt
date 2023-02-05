package com.example.notekeeper.ui.notes

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.GridLayout
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notekeeper.R
import com.example.notekeeper.databinding.FragmentNotesBinding
import com.example.notekeeper.repositories.NoteRepository
import com.example.notekeeper.ui.adapters.NotesAdapter
import com.example.notekeeper.utils.Constants
import com.example.notekeeper.viewModel.NotesPageVM
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.notekeeper.database.NoteEntity
import com.example.notekeeper.utils.DataStore

@AndroidEntryPoint
class NotesFragment : Fragment()
{
    private lateinit var binding: FragmentNotesBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    val viewModel: NotesPageVM by viewModels()

    @Inject
    lateinit var notesAdapter: NotesAdapter

    @Inject
    lateinit var repository: NoteRepository

    @Inject
    lateinit var dataStore: DataStore

    lateinit var lastList: MutableList<NoteEntity>
    
    private lateinit var popupMenu: PopupMenu

    private lateinit var tempShape : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.orders.postValue("id")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentNotesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        //changing the adapter state
        notesAdapter.adapterState = Constants.ADAPTER_STATE_NOTES

        binding.apply {

            //set the toolbar listShape icon
            lifecycle.coroutineScope.launchWhenCreated {
                dataStore.getListShape().collect {
                    tempShape = it
                }
            }


            //handling the first time using notes fragment
            val pref : SharedPreferences = context!!.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
            val notesFirstStart: Boolean = pref.getBoolean("first_time_notes_fragment", true)

            if(notesFirstStart)
            {
                lifecycle.coroutineScope.launchWhenCreated {
                    dataStore.setListShape("linear_view")
                }
                tempShape = "linear_view"

                val prefs : SharedPreferences = context!!.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
                val editor : SharedPreferences.Editor = prefs.edit()
                editor.putBoolean("first_time_notes_fragment", false)
                editor.apply()
            }

            //doing searchView works here
            val searchView = mainToolbar.menu.findItem(R.id.menu_search).actionView as SearchView
            searchView.setBackgroundColor(Color.WHITE)
            searchView.queryHint = "Search..."
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(p0: String?): Boolean { return false }

                override fun onQueryTextChange(p0: String): Boolean
                {
                    if(p0.isNotEmpty())
                        viewModel.getSearchedNotes(p0)
                    else
                    {
                        viewModel.setObservedList(lastList)
                        viewModel.loadNoteList()
                    }
                    return true
                }
            })


            //on click for filter and shape of list (toolbar item click listener)
            mainToolbar.setOnMenuItemClickListener { menuItem : MenuItem ->
                when(menuItem.itemId) {
                    //when user is filtering the notes
                    R.id.menu_filter -> {

                        initPopUpMenu(mainToolbar.findViewById(R.id.menu_filter), R.menu.sorting_menu)
                        popupMenu.setOnMenuItemClickListener {
                            when(it.itemId)
                            {
                                R.id.sorting_menu_priority -> {
                                    initPopUpMenu(mainToolbar.findViewById(R.id.menu_filter), R.menu.sorting_menu_2)
                                    filteringOnClickListener(0)
                                    return@setOnMenuItemClickListener true
                                }
                                R.id.sorting_menu_title -> {
                                    initPopUpMenu(mainToolbar.findViewById(R.id.menu_filter), R.menu.sorting_menu_2)
                                    filteringOnClickListener(2)
                                    return@setOnMenuItemClickListener true
                                }
                                R.id.sorting_menu_date -> {
                                    initPopUpMenu(mainToolbar.findViewById(R.id.menu_filter), R.menu.sorting_menu_2)
                                    filteringOnClickListener(4)
                                    return@setOnMenuItemClickListener true
                                }
                                else -> return@setOnMenuItemClickListener false
                            }
                        }
                        return@setOnMenuItemClickListener true
                    }

                    R.id.menu_list_shape -> {
                        lifecycle.coroutineScope.launchWhenCreated {
                            dataStore.getListShape().collect{
                                tempShape = it
                            }
                        }
                        if(tempShape == "linear_view")
                        {
                            mainNotesRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
                            mainNotesRecycler.adapter = notesAdapter
                            mainToolbar.menu.findItem(R.id.menu_list_shape).icon = resources.getDrawable(R.drawable.ic_baseline_view_list_24)
                                    lifecycle.coroutineScope.launchWhenCreated {
                                        dataStore.setListShape("grid_view")
                                    }
                        }
                        else if(tempShape == "grid_view")
                        {
                            mainNotesRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                            mainNotesRecycler.adapter = notesAdapter
                            mainToolbar.menu.findItem(R.id.menu_list_shape).icon = resources.getDrawable(R.drawable.ic_baseline_grid_view_24)
                            lifecycle.coroutineScope.launchWhenCreated {
                                dataStore.setListShape("linear_view")
                            }
                        }

                        return@setOnMenuItemClickListener true
                    }
                    else -> return@setOnMenuItemClickListener false
                }
            }

            
            //observing for new orders of filtering the notes
            viewModel.orders.observe(viewLifecycleOwner){ orders ->
                //observing from database for new list
                repository.getNotes(orders).observe(viewLifecycleOwner){
                    lastList = it
                    viewModel.setObservedList(it)
                    viewModel.loadNoteList()
                }
            }
            mainNotesRecycler.adapter


            //viewModel handling for show list
            viewModel.noteListLiveData.observe(viewLifecycleOwner){
                notesAdapter.setData(it)
                if(tempShape == "linear_view")
                {
                    mainNotesRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    mainToolbar.menu.findItem(R.id.menu_list_shape).icon = resources.getDrawable(R.drawable.ic_baseline_grid_view_24)
                }
                else if(tempShape == "grid_view")
                {
                    mainNotesRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
                    mainToolbar.menu.findItem(R.id.menu_list_shape).icon = resources.getDrawable(R.drawable.ic_baseline_view_list_24)
                }
                mainNotesRecycler.adapter = notesAdapter
            }


            //connect the navigation drawer to toolbar button
            actionBarDrawerToggle =  ActionBarDrawerToggle(requireActivity(), notesDrawerLayout, mainToolbar, R.string.app_name, R.string.app_name)
            actionBarDrawerToggle.syncState()


            //set onClickListener for navigation drawer items
            mainNavigationView.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.settings_drawer -> findNavController().navigate(R.id.action_notesFragment_to_settingsFragment)
                    R.id.aboutMe_drawer -> {
                        val alert = AlertDialog.Builder(context)
                        alert.setTitle("About Me")
                            .setMessage("I am Nima Alizadeh.....")
                            .setPositiveButton("OK", null)
                            .create()
                            .show()
                    }
                    R.id.remind_drawer -> {}
                    R.id.favorites_drawer -> findNavController().navigate(R.id.action_notesFragment_to_favoritesFragment)
                }

                //to close the drawer when user clicks on one of the items
                notesDrawerLayout.closeDrawer(GravityCompat.START)

                false
            }


            //viewModel handling for empty list
            viewModel.emptyList.observe(viewLifecycleOwner){
                if(it)
                {
                    emptyListLayout.visibility = View.VISIBLE
                    mainNotesRecycler.visibility = View.GONE
                }
                else
                {
                    emptyListLayout.visibility = View.GONE
                    mainNotesRecycler.visibility = View.VISIBLE
                }
            }


            //add button
            addNoteButton.setOnClickListener {
                //sending the state to change the doButton in newEditFragment
                //we have to do it with arguments and open the fragment like always
                val direction = NotesFragmentDirections.actionNotesFragmentToNewEditFragment(0, Constants.ON_CLICK_NEW)
                findNavController().navigate(direction)
                searchView.setQuery("", false)
            }

            //the buttons on the each items of notes list
            notesAdapter.setOnItemCLickListener { entity, state ->
                when(state)
                {
                    Constants.ON_CLICK_DELETE -> {
                        val alert = AlertDialog.Builder(requireContext())
                        alert.setTitle("Delete")
                            .setMessage("Are you sure to delete this note?")
                            .setPositiveButton("Yes") { _, _ ->
                                viewModel.deleteNote(entity)
                            }
                            .setNegativeButton("No", null)
                            .setCancelable(false)
                            .create()
                            .show()
                    }

                    Constants.ON_CLICK_EDIT -> {
                        //go to edit page
                        val direction = NotesFragmentDirections.actionNotesFragmentToNewEditFragment(entity.id, Constants.ON_CLICK_EDIT)
                        findNavController().navigate(direction)
                    }

                    Constants.ON_CLICK_SHARE -> {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT,
                                "((" + entity.title + "))\n\n" + entity.desc)
                            type = "text/plain"
                        }
                        startActivity(sendIntent)
                    }

                    Constants.ON_CLICK_GOTO_DETAIL -> {
                        val direction = NotesFragmentDirections.actionNotesFragmentToDetailFragment(entity.id)
                        findNavController().navigate(direction)
                    }
                }
            }
        }
    }

//    override fun onClick(view: View)
//    {
//        when(view.id)
//        {
//            R.id.priority_up -> viewModel.orders.postValue("priority_low")
//            R.id.priority_down -> viewModel.orders.postValue("priority_high")
//            R.id.title_up -> viewModel.orders.postValue("title_desc")
//            R.id.title_down -> viewModel.orders.postValue("title_asc")
//            R.id.date_up -> viewModel.orders.postValue("date_desc")
//            R.id.date_down -> viewModel.orders.postValue("date_asc")
//        }
//        binding.apply {
//            filteringLayout.animate().alpha(0f).setDuration(300).setListener(object : AnimatorListenerAdapter(){
//                override fun onAnimationEnd(animation: Animator?) {
//                    filteringLayout.visibility = View.GONE
//                }
//            })
//        }
//    }

    private fun initPopUpMenu(view: View, menu: Int)
    {
        popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(menu, popupMenu.menu)
        popupMenu.show()
    }

    private fun filteringOnClickListener(index: Int)
    {
        val orderSelect = arrayListOf("priority_high", "priority_low", "title_asc", "title_desc", "date_asc", "date_desc")
        popupMenu.setOnMenuItemClickListener { secondMenuItem: MenuItem ->
            when(secondMenuItem.itemId)
            {
                R.id.sorting_menu_2_Ascending -> {
                    viewModel.orders.postValue(orderSelect[index])
                    return@setOnMenuItemClickListener true
                }
                R.id.sorting_menu_2_Descending -> {
                    viewModel.orders.postValue(orderSelect[index+1])
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
    }
}
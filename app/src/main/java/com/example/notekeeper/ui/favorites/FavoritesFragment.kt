package com.example.notekeeper.ui.favorites


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notekeeper.databinding.FragmentFavoritesBinding
import com.example.notekeeper.ui.adapters.NotesAdapter
import com.example.notekeeper.utils.Constants
import com.example.notekeeper.viewModel.FavoritePageVM
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FavoritesFragment : Fragment()
{
    private lateinit var binding: FragmentFavoritesBinding

    private val viewModel: FavoritePageVM by viewModels()

    @Inject
    lateinit var notesAdapter: NotesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //changing the adapter state
        notesAdapter.adapterState = Constants.ADAPTER_STATE_FAVORITE

        viewModel.loadFavorite()

        binding.apply {
            //handling back button
            favoriteToolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            //empty layout handling
            viewModel.empty.observe(viewLifecycleOwner){
                if(it)
                {
                    emptyListLayout.visibility = View.VISIBLE
                    favoriteRecycler.visibility = View.GONE
                }
                else
                {
                    emptyListLayout.visibility = View.GONE
                    favoriteRecycler.visibility = View.VISIBLE
                }
            }

            //make the recycler view list to show items of notes
            viewModel.favoriteNotesList.observe(viewLifecycleOwner){
                notesAdapter.setData(it)
                favoriteRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                favoriteRecycler.adapter = notesAdapter
            }

            notesAdapter.setOnItemCLickListener { entity, state ->
                when(state)
                {
                    Constants.ON_CLICK_GOTO_DETAIL -> {
                        val direction = FavoritesFragmentDirections.actionFavoritesFragmentToDetailFragment(entity.id)
                        findNavController().navigate(direction)
                    }
                }
            }
        }
    }
}
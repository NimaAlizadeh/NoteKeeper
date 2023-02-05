package com.example.notekeeper.ui.detail

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateFormat
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.notekeeper.R
import com.example.notekeeper.database.NoteEntity
import com.example.notekeeper.databinding.FragmentDetailBinding
import com.example.notekeeper.utils.Constants
import com.example.notekeeper.utils.DataStore
import com.example.notekeeper.viewModel.DetailPageVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class DetailFragment : Fragment()
{
    private lateinit var binding: FragmentDetailBinding
    private val viewModel: DetailPageVM by viewModels()
    private val args: DetailFragmentArgs by navArgs()

    private var noteId = 0

    @Inject
    lateinit var dataStore: DataStore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        noteId = args.noteId
        viewModel.loadDetail(noteId)
        binding.apply {
            viewModel.loading.observe(viewLifecycleOwner){
                if(it)
                {
                    detailFragmentLoading.visibility = View.VISIBLE
                    detailLayout.visibility = View.GONE
                }
                else
                {
                    detailFragmentLoading.visibility = View.GONE
                    detailLayout.visibility = View.VISIBLE
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            //handling the first time using detail fragment
            val pref : SharedPreferences = context!!.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
            val detailFirstStart: Boolean = pref.getBoolean("first_time_detail_frag", true)

            if(detailFirstStart)
            {
                //do what you want to do just in first time
                //in this case im gonna put text alignment in center mode

                viewModel.setTextAlignment(Constants.TEXT_ALIGNMENT_CENTER)
                lifecycle.coroutineScope.launchWhenCreated {
                    dataStore.setTextAlignment(Constants.TEXT_ALIGNMENT_CENTER) }

                val prefs : SharedPreferences = context!!.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
                val editor : SharedPreferences.Editor = prefs.edit()
                editor.putBoolean("first_time_detail_frag", false)
                editor.apply()
            }


            //favorite handling
            viewModel.isFavorite(noteId)

            favoriteButton.setOnClickListener {
                viewModel.setFavorite(noteId)
            }

            viewModel.isFavorite.observe(viewLifecycleOwner){
                if(it)
                    favoriteButton.setImageResource(R.drawable.favorite_filled)
                else
                    favoriteButton.setImageResource(R.drawable.favorite_n_filled)
            }


            lifecycle.coroutineScope.launchWhenCreated {
                dataStore.getTextAlignment().collect{
                    setAlignment(it)
                }
            }

            viewModel.textAlignment.observe(viewLifecycleOwner){
                setAlignment(it)
            }

            btnAlignLeft.setOnClickListener {
                viewModel.setTextAlignment(Constants.TEXT_ALIGNMENT_LEFT)
            }

            btnAlignCenter.setOnClickListener {
                viewModel.setTextAlignment(Constants.TEXT_ALIGNMENT_CENTER)
            }

            btnAlignRight.setOnClickListener {
                viewModel.setTextAlignment(Constants.TEXT_ALIGNMENT_RIGHT)
            }



            //set colors
            detailToolbar.setNavigationIconTint(Color.BLACK)
            detailFragmentDesc.setTextColor(R.color.black)
            detailFragmentTitle.setTextColor(R.color.black)

            //scrollable textView for description
            detailFragmentDesc.movementMethod = ScrollingMovementMethod.getInstance()

            // back button handling
            detailToolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }


            //putting information into views
            viewModel.details.observe(viewLifecycleOwner){ noteEntity: NoteEntity ->
                if(noteEntity.title.isEmpty())
                {
                    detailFragmentTitle.setTextColor(0xFF959595.toInt())
                    detailFragmentTitle.text = "No Title"
                }
                else
                {
                    detailFragmentTitle.setTextColor(0xFF000000.toInt())
                    detailFragmentTitle.text = noteEntity.title
                }

                if(noteEntity.desc.isEmpty())
                {
                    detailFragmentDesc.setTextColor(0xFF959595.toInt())
                    detailFragmentDesc.text = "No Description"
                }
                else
                {
                    detailFragmentDesc.setTextColor(0xFF000000.toInt())
                    detailFragmentDesc.text = noteEntity.desc
                }


                val date = Date(noteEntity.date)
                detailFragmentDate.text = DateFormat.format("yyyy/MM/dd\nhh:mm:ss a", date).toString()

                //log the time to see what is going on here
                Log.d("The time we want : ", noteEntity.date.toString() + " _ " + DateFormat.format("yyyy/MM/dd\nhh:mm:ss a", date).toString())


                detailToolbar.setOnMenuItemClickListener { menuItem: MenuItem ->
                    when(menuItem.itemId)
                    {
                        R.id.menu_detail_delete -> {
                            val alert = AlertDialog.Builder(requireContext())
                            alert.setTitle("Delete")
                                .setMessage("Are you sure to delete this note?")
                                .setPositiveButton("Yes") { _, _ ->
                                    viewModel.deleteNote(noteEntity)
                                    findNavController().popBackStack()
                                }
                                .setNegativeButton("No", null)
                                .setCancelable(false)
                                .create()
                                .show()

                            return@setOnMenuItemClickListener true
                        }

                        R.id.menu_detail_share -> {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT,
                                    "((" + noteEntity.title + "))\n\n" + noteEntity.desc)
                                type = "text/plain"
                            }
                            startActivity(sendIntent)
                            return@setOnMenuItemClickListener true
                        }

                        R.id.menu_detail_edit -> {
                            val direction = DetailFragmentDirections.actionDetailFragmentToNewEditFragment(noteEntity.id, Constants.ON_CLICK_EDIT)
                            findNavController().navigate(direction)
                            return@setOnMenuItemClickListener true
                        }

                        else -> return@setOnMenuItemClickListener false
                    }
                }
            }
        }
    }

    private fun setAlignment(order: String)
    {
        binding.apply {
            when(order)
            {
                Constants.TEXT_ALIGNMENT_LEFT -> {
                    btnAlignLeft.setBackgroundColor(0xFFA6A6A6.toInt())
                    btnAlignCenter.setBackgroundColor(0xFFF0F0F0.toInt())
                    btnAlignRight.setBackgroundColor(0xFFF0F0F0.toInt())
                    detailFragmentDesc.gravity = Gravity.LEFT
                    lifecycle.coroutineScope.launchWhenCreated {
                        dataStore.setTextAlignment(Constants.TEXT_ALIGNMENT_LEFT)
                    }
                }
                Constants.TEXT_ALIGNMENT_RIGHT -> {
                    btnAlignLeft.setBackgroundColor(0xFFF0F0F0.toInt())
                    btnAlignCenter.setBackgroundColor(0xFFF0F0F0.toInt())
                    btnAlignRight.setBackgroundColor(0xFFA6A6A6.toInt())
                    detailFragmentDesc.gravity = Gravity.RIGHT
                    lifecycle.coroutineScope.launchWhenCreated {
                        dataStore.setTextAlignment(Constants.TEXT_ALIGNMENT_RIGHT)
                    }
                }
                Constants.TEXT_ALIGNMENT_CENTER -> {
                    btnAlignLeft.setBackgroundColor(0xFFF0F0F0.toInt())
                    btnAlignCenter.setBackgroundColor(0xFFA6A6A6.toInt())
                    btnAlignRight.setBackgroundColor(0xFFF0F0F0.toInt())
                    detailFragmentDesc.gravity = Gravity.CENTER_HORIZONTAL
                    lifecycle.coroutineScope.launchWhenCreated {
                        dataStore.setTextAlignment(Constants.TEXT_ALIGNMENT_CENTER)
                    }
                }
            }
        }
    }
}
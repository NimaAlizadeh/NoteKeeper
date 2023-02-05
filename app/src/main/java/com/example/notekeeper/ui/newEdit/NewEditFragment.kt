package com.example.notekeeper.ui.newEdit

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.notekeeper.R
import com.example.notekeeper.database.NoteEntity
import com.example.notekeeper.databinding.FragmentNewEditBinding
import com.example.notekeeper.repositories.NoteRepository
import com.example.notekeeper.utils.Constants
import com.example.notekeeper.viewModel.NewEditPageVM
import dagger.hilt.android.AndroidEntryPoint
import matteocrippa.it.karamba.today
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class NewEditFragment : Fragment() , AdapterView.OnItemSelectedListener
{
    @Inject
    lateinit var repository: NoteRepository

    @Inject
    lateinit var editingEntity: NoteEntity

    @Inject
    lateinit var savingEntity: NoteEntity

    private lateinit var binding: FragmentNewEditBinding

    private val viewModel: NewEditPageVM by viewModels()

    private var priority: Int = -1

    private val args: NewEditFragmentArgs by navArgs()


    private var editingTitle = ""
    private var editingDesc = ""
    private var editingPriority = 0
    private var editingIsFavorite = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentNewEditBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        getView()?.setOnKeyListener { _, keyCode, _ ->
            if( keyCode == KeyEvent.KEYCODE_BACK ) {
                checkGettingBack()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        binding.apply {

            // on click for back button on toolbar
            // handling the back button when user is not saving the changes
            newEditToolbar.setNavigationOnClickListener {
                checkGettingBack()
            }

            ///////////////////////when click to add note
            if(args.state == Constants.ON_CLICK_NEW)
            {
                //this is for focusing on title when it comes to this page
                newEditTitle.requestFocus()
                val keyboard: InputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                keyboard.showSoftInput(newEditTitle, InputMethodManager.SHOW_IMPLICIT)

                //set toolbar title
                newEditToolbar.title = "New Note"

                //handling the save button on the toolbar menu
                newEditToolbar.setOnMenuItemClickListener { menuItem: MenuItem ->
                    if(menuItem.itemId == R.id.new_edit_menu_save)
                    {
                        if(newEditDesc.text.isNotEmpty() || newEditTitle.text.isNotEmpty())
                        {
                            saveNewNote()
                        }
                        else
                            Toast.makeText(requireContext(), "Empty Box", Toast.LENGTH_SHORT).show()

                        return@setOnMenuItemClickListener true
                    }
                    else
                        return@setOnMenuItemClickListener false
                }
            }

            ///////////////////////when click to edit note
            else if(args.state == Constants.ON_CLICK_EDIT)
            {
                newEditToolbar.title = "Edit Note"

                viewModel.loading.observe(viewLifecycleOwner){
                    if(it)
                    {
                        loadingProgress.visibility = View.VISIBLE
                        fragmentLayout.visibility = View.GONE
                    }
                    else
                    {
                        loadingProgress.visibility = View.GONE
                        fragmentLayout.visibility = View.VISIBLE
                    }
                }

                //requesting from database for single note
                viewModel.loadSingleNote(args.noteId)
                viewModel.singleNote.observe(viewLifecycleOwner){
                    //set existing note's information to views
                    newEditTitle.setText(it.title)
                    newEditDesc.setText(it.desc)
                    //set the priority of existed to spinner
                    prioritySpinner.setSelection(arrayListOf(2,1,0)[it.priority])
                    //set date for editing item
                    editingEntity.date = it.date

                    //picking up the information to check
                    editingTitle = it.title
                    editingDesc = it.desc
                    editingPriority = it.priority
                    editingIsFavorite = it.isFavorite
                }

                //handling the save button on the toolbar menu
                newEditToolbar.setOnMenuItemClickListener { menuItem : MenuItem ->
                    if(menuItem.itemId == R.id.new_edit_menu_save)
                    {
                        if(newEditDesc.text.isNotEmpty() || newEditTitle.text.isNotEmpty())
                        {
                            editNote()
                        }
                        else
                            Toast.makeText(requireContext(), "Empty Box", Toast.LENGTH_SHORT).show()

                        return@setOnMenuItemClickListener true
                    }
                    else
                        return@setOnMenuItemClickListener false
                }

            }

            //init spinner
            ArrayAdapter.createFromResource(requireContext(), R.array.prioritySpinnerStringArray,
                android.R.layout.simple_spinner_dropdown_item).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                prioritySpinner.adapter = adapter
                prioritySpinner.onItemSelectedListener = this@NewEditFragment }

        }
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long)
    {
        when(parent?.getItemAtPosition(pos).toString())
        {
            "High" -> priority = 2
            "Medium" -> priority = 1
            "Low" -> priority = 0
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}

    private fun showSaveDialog(temp: Boolean)
    {
        val dialog = AlertDialog.Builder(context)
            .setMessage("Do you want to save changes you have made?")
            .setPositiveButton("Save" , DialogInterface.OnClickListener { _, _ ->
                if(temp)
                    saveNewNote()
                else
                    editNote()
            })
            .setNegativeButton("Discard", DialogInterface.OnClickListener { _, _ ->
                findNavController().popBackStack()
            })
            .setNeutralButton("Cancel", null)

        dialog.create().show()
    }

    private fun saveNewNote()
    {
        savingEntity.title = binding.newEditTitle.text.toString().trim()
        savingEntity.desc = binding.newEditDesc.text.toString().trim()
        savingEntity.date = Date().today().time
        savingEntity.priority = priority
        viewModel.saveNote(savingEntity)

        Toast.makeText(requireContext(), "Your note saved", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun editNote()
    {
        editingEntity.id = args.noteId
        savingEntity.title = binding.newEditTitle.text.toString().trim()
        savingEntity.desc = binding.newEditDesc.text.toString().trim()
        editingEntity.priority = priority
        editingEntity.isFavorite = editingIsFavorite

        viewModel.editNote(editingEntity)
        Toast.makeText(requireContext(), "Note Edited", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
        viewModel.loading.postValue(false)
    }

    private fun checkGettingBack()
    {
        binding.apply {
            if(args.state == Constants.ON_CLICK_NEW) {
                if(newEditTitle.text.isNotEmpty() || newEditDesc.text.isNotEmpty()) {
                    showSaveDialog(true)
                }
                else
                    findNavController().popBackStack()
            }
            else if(args.state == Constants.ON_CLICK_EDIT) {
                if((newEditTitle.text.toString() != editingTitle) || (newEditDesc.text.toString() != editingDesc)
                    || (editingPriority != priority)){
                    showSaveDialog(false)
                }
                else
                    findNavController().popBackStack()
            }
        }
    }
}
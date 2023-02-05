package com.example.notekeeper.ui.settings

import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.notekeeper.R
import com.example.notekeeper.databinding.FragmentSettingsBinding
import com.example.notekeeper.utils.setCustomFont

class SettingsFragment : Fragment()
{

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            //handling back button click
            settingsToolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }


            titleFontText.setCustomFont(R.font.calistoga, requireContext())
        }
    }
}
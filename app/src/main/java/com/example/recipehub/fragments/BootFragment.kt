package com.example.recipehub.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipehub.R
import com.example.recipehub.databinding.FragmentBootBinding
import com.example.recipehub.utils.SimulateLoading

class BootFragment : Fragment() {
    private var binding: FragmentBootBinding? = null
    private val _binding get() = binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBootBinding.inflate(inflater, container, false)
        _binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                var isLoading by remember { mutableStateOf(true) }
                SimulateLoading(
                    onLoadingComplete = { isLoading = false },25
                )
                LaunchedEffect(isLoading) {
                    if (!isLoading) {
                        findNavController().navigate(R.id.action_to_home)
                    }
                }
            }
        }
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
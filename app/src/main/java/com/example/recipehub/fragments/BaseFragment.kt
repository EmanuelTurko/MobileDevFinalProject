package com.example.recipehub.fragments

import androidx.fragment.app.Fragment
import com.example.recipehub.utils.BottomNavController
import android.content.Context

abstract class BaseFragment :Fragment() {
    private var _bottomNavController: BottomNavController? = null
    protected val bottomNavController : BottomNavController?
        get() = activity as? BottomNavController ?: throw IllegalStateException(
            "Activity must implement BottomNavController"
        )
    override fun onAttach(context:Context){
        super.onAttach(context)
        _bottomNavController = context as? BottomNavController
    }
    override fun onDetach(){
        super.onDetach()
        _bottomNavController = null
    }
}
package com.example.coneonapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.coneonapp.databinding.FragmentHomeBinding
import com.example.coneonapp.databinding.FragmentProfileBinding
import com.example.coneonapp.utils.Constant
import com.example.coneonapp.utils.SharedPreferenceHelper

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment(){

    private lateinit var binding: FragmentProfileBinding
    private lateinit var prefs : SharedPreferenceHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =  FragmentProfileBinding.inflate(layoutInflater)
        prefs = SharedPreferenceHelper.getInstance(requireContext())
        binding.logoutButton.setOnClickListener {
            startActivity(Intent(activity, LoginActivity::class.java))
            activity?.finish()
            prefs.clear()

        }
        return  binding.root
    }


}
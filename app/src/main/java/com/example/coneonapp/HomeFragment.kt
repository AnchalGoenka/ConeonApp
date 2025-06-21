package com.example.coneonapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coneonapp.adapter.ProductAdapter
import com.example.coneonapp.databinding.FragmentHomeBinding
import com.example.coneonapp.model.ProductsDataClass
import com.example.coneonapp.utils.Constant
import com.example.coneonapp.utils.SharedPreferenceHelper


class HomeFragment : Fragment() {

    private lateinit var binding:FragmentHomeBinding
    private lateinit var prefs : SharedPreferenceHelper
    var dataList = ArrayList<ProductsDataClass>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentHomeBinding.inflate(layoutInflater)
        prefs = SharedPreferenceHelper.getInstance(requireContext())

        dataList = prefs.getArrayList(Constant.PRODUCT_LIST)
        if (dataList.isEmpty()&& dataList.size==0){
            loadProduct()
        }
        setProductListRecyclerView()

        return binding.root
    }


    private fun setProductListRecyclerView(){
        // create  layoutManager
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(activity)
        // pass it to rvLists layoutManager
        binding.recyclerview.setLayoutManager(layoutManager)
        // initialize the adapter,
        // and pass the required argument
        val productAdapter = ProductAdapter(dataList)
        // attach adapter to the recycler view
        binding.recyclerview.adapter = productAdapter
    }

    // add items to the list manually in our case
    private fun loadProduct() {
        dataList.add(ProductsDataClass(R.drawable.cement, "Cement","400",false))
        dataList.add(ProductsDataClass(R.drawable.pipe, "Pipe","100",false))
        dataList.add(ProductsDataClass(R.drawable.steel, "Steel","300",false))
        dataList.add(ProductsDataClass(R.drawable.wire, "Wire","90",false))
        dataList.add(ProductsDataClass(R.drawable.labour, "Labour","500",false))
        dataList.add(ProductsDataClass(R.drawable.safty_glove, "Safety glasses","100",false))
        dataList.add(ProductsDataClass(R.drawable.hats, "Hats","100",false))
    }

    override fun onPause() {
        super.onPause()
        prefs.putArrayList(Constant.PRODUCT_LIST, dataList)
    }


}
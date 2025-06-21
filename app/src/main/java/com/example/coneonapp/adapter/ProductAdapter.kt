package com.example.coneonapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.coneonapp.LocationActivity
import com.example.coneonapp.databinding.CardViewDesignBinding
import com.example.coneonapp.model.ProductsDataClass
import com.example.coneonapp.utils.NotificationHelper

class ProductAdapter(private val list: List<ProductsDataClass>):  RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val v = LayoutInflater.from(parent.context)
        val listItemBinding = CardViewDesignBinding.inflate(v, parent, false)
        return ViewHolder(listItemBinding)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder){
            with(list[position]){
                listItemBinding.title.text = this.title
                listItemBinding.price.text = "₹${this.price}"

                Glide.with(listItemBinding.root.context)
                    .setDefaultRequestOptions(RequestOptions().frame(1000000L))
                    .load(this.image).centerCrop().into(listItemBinding.imageview)
                if (this.isOderPlace){
                    listItemBinding.placeOderBtn.visibility = View.GONE
                    listItemBinding.trackBtn.visibility = View.VISIBLE
                }
                listItemBinding.placeOderBtn.setOnClickListener {

                        Toast.makeText(holder.itemView.context, "Order Booked", Toast.LENGTH_SHORT).show()
                        this.isOderPlace = true
                        listItemBinding.placeOderBtn.text = "Placed"
                        listItemBinding.placeOderBtn.visibility = View.GONE
                        listItemBinding.trackBtn.visibility = View.VISIBLE
                        NotificationHelper.showLocalPush(holder.itemView.context,"Order Placed","You ordered a ${this.title} for ₹${this.price}")


                }

                listItemBinding.trackBtn.setOnClickListener {
                    listItemBinding.root.context.startActivity(Intent(listItemBinding.root.context, LocationActivity::class.java))

                }
            }
        }
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ViewHolder(val listItemBinding: CardViewDesignBinding) : RecyclerView.ViewHolder(listItemBinding.root)

}
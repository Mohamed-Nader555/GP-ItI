package com.mohamednader.shoponthego.Profile.View.Addresses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mohamednader.shoponthego.Model.Pojo.Customers.Address
import com.mohamednader.shoponthego.databinding.ItemAddressBinding

class AddressAdapter(private val context: Context, private val listener: OnAddressClickListener) :
        ListAdapter<Address, AddressViewHolder>(AddressDiffUtil()) {

    private lateinit var binding: ItemAddressBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        binding = ItemAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address: Address = getItem(position)

        binding.streetText.text = address.address1
        binding.cityText.text = address.city
        binding.countryText.text = address.country
        binding.phoneText.text = address.phone
        binding.nameText.text = "${address.firstName} + ${address.lastName}"

        if(address.province == "Profile"){
            binding.makeDefaultAddressBtn.visibility = View.VISIBLE
            binding.deleteAddressBtn.visibility = View.VISIBLE

            if (address.default == true){
                binding.makeDefaultAddressBtn.visibility = View.GONE
            }


        }else if(address.province == "Payment"){
            binding.makeDefaultAddressBtn.visibility = View.GONE
            binding.makeDefaultAddressBtn.visibility = View.GONE

            binding.addressCardView.setOnClickListener {
                listener.onAddressClickListener(address.id!!)
            }

        }
        binding.makeDefaultAddressBtn.setOnClickListener{
            listener.onMakeDefaultClickListener(address.id!!)
        }

        binding.deleteAddressBtn.setOnClickListener {
            listener.onDeleteClickListener(address.id!!)
        }

    }
}

class AddressViewHolder(var binding: ItemAddressBinding) : RecyclerView.ViewHolder(binding.root)

class AddressDiffUtil : DiffUtil.ItemCallback<Address>() {
    override fun areItemsTheSame(oldItem: Address, newItem: Address): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Address, newItem: Address): Boolean {
        return oldItem == newItem
    }

}
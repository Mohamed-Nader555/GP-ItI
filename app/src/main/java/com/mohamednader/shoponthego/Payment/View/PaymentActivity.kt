package com.mohamednader.shoponthego.Payment.View

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mohamednader.shoponthego.DataStore.ConcreteDataStoreSource
import com.mohamednader.shoponthego.Database.ConcreteLocalSource
import com.mohamednader.shoponthego.Model.Pojo.Customers.Address
import com.mohamednader.shoponthego.Model.Pojo.Customers.Customer
import com.mohamednader.shoponthego.Model.Pojo.DraftOrders.DraftOrder
import com.mohamednader.shoponthego.Model.Repo.Repository
import com.mohamednader.shoponthego.Network.ApiClient
import com.mohamednader.shoponthego.Network.ApiState
import com.mohamednader.shoponthego.Payment.ViewModel.PaymentViewModel
import com.mohamednader.shoponthego.Profile.View.Addresses.AddressAdapter
import com.mohamednader.shoponthego.Profile.View.Addresses.OnAddressClickListener
import com.mohamednader.shoponthego.Utils.Constants
import com.mohamednader.shoponthego.Utils.GenericViewModelFactory
import com.mohamednader.shoponthego.databinding.ActivityPaymentBinding
import com.mohamednader.shoponthego.databinding.BottomSheetDialogAddressesBinding
import com.mohamednader.shoponthego.databinding.BottomSheetDialogPaymentMethodBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PaymentActivity : AppCompatActivity(), OnAddressClickListener {

    val TAG = "PaymentActivity_INFO_TAG"

    private lateinit var binding: ActivityPaymentBinding

    //View Model Members
    private lateinit var paymentViewModel: PaymentViewModel
    private lateinit var factory: GenericViewModelFactory

    //Addresses Bottom Sheet
    lateinit var addressBottomSheetBinding: BottomSheetDialogAddressesBinding
    lateinit var paymentMethodBottomSheetBinding: BottomSheetDialogPaymentMethodBinding
    lateinit var addressBottomSheetDialog: BottomSheetDialog
    lateinit var paymentMethodBottomSheetDialog: BottomSheetDialog
    private lateinit var addressAdapter: AddressAdapter
    private lateinit var addressLinearLayoutManager: LinearLayoutManager

    //Needed Variables
    lateinit var draftOrder: DraftOrder
    lateinit var customer: Customer
    lateinit var addressesList: List<Address>
    lateinit var customerId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initViews()

    }

    private fun initViews() {

        //View Model
        factory = GenericViewModelFactory(Repository.getInstance(ApiClient.getInstance(),
                ConcreteLocalSource(this@PaymentActivity),
                ConcreteDataStoreSource(this@PaymentActivity)))
        paymentViewModel = ViewModelProvider(this, factory).get(PaymentViewModel::class.java)

        //Address Bottom Sheet
        addressAdapter = AddressAdapter(this@PaymentActivity, this, "Payment")
        addressLinearLayoutManager =
            LinearLayoutManager(this@PaymentActivity, RecyclerView.VERTICAL, false)
        addressBottomSheetBinding = BottomSheetDialogAddressesBinding.inflate(layoutInflater)
        addressBottomSheetDialog = BottomSheetDialog(this@PaymentActivity)
        addressBottomSheetDialog.setContentView(addressBottomSheetBinding.root)
        addressBottomSheetBinding.addressesRecyclerView.apply {
            adapter = addressAdapter
            layoutManager = addressLinearLayoutManager
        }
        addressBottomSheetBinding.fab.visibility = View.GONE



        paymentMethodBottomSheetBinding = BottomSheetDialogPaymentMethodBinding.inflate(layoutInflater)
        paymentMethodBottomSheetDialog = BottomSheetDialog(this@PaymentActivity)
        paymentMethodBottomSheetDialog.setContentView(paymentMethodBottomSheetBinding.root)






        binding.backArrowImg.setOnClickListener {
            onBackPressed()
        }

        binding.changePaymentMethod.setOnClickListener {
            paymentMethodBottomSheetDialog.show()
        }


        binding.changeAddressBtn.setOnClickListener {
            addressBottomSheetDialog.show()
        }


        binding.placeOrderBtn.setOnClickListener {

        }

        paymentMethodBottomSheetBinding.cashSelectBtn.setOnClickListener {
            binding.paymentMethodText.setText("Cash On Delivery")
            paymentMethodBottomSheetDialog.dismiss()
        }
        paymentMethodBottomSheetBinding.paypalSelectBtn.setOnClickListener {
            binding.paymentMethodText.setText("Using PayPal")
            paymentMethodBottomSheetDialog.dismiss()
        }

        apiRequests()
    }

    private fun apiRequests() {

        lifecycleScope.launchWhenStarted {

            launch {
                paymentViewModel.getStringDS(Constants.customerIdKey).asLiveData()
                    .observe(this@PaymentActivity) { customerID ->
                        // Update UI with the retrieved name
                        Log.i(TAG, "apiRequests: VIP: $customerID")
                        customerId = customerID!!
                        paymentViewModel.getAllDraftOrdersFromNetwork(customerId.toLong())
                        paymentViewModel.getCustomerByID(customerId.toLong())
                    }
            }



            launch {
                paymentViewModel.draftOrdersList.collectLatest { result ->
                    when (result) {
                        is ApiState.Success<List<DraftOrder>> -> {
                            if (result.data.isNotEmpty()) {
                                draftOrder = result.data.get(0)
                                Log.i(TAG, "onCreate: Success: Draft Orders:  ${draftOrder.id}")
                                Log.i(TAG, "onCreate: Success: Draft Orders:  ${draftOrder.email}")
                                Log.i(TAG, "onCreate: Success: Draft Orders:  ${
                                    draftOrder.lineItems?.get(0)?.quantity
                                }")

                                binding.subtotalValue.text = draftOrder.subtotalPrice
                                binding.taxValue.text = draftOrder.totalTax
                                binding.discountValue.text = draftOrder.appliedDiscount?.amount
                                binding.shippingValue.text = "20"
                                binding.totalValue.text = draftOrder.totalPrice

                            } else {
                                Log.i(TAG, "onCreate: Success...The list is empty}")
                            }
                        }
                        is ApiState.Loading -> {
                            Log.i(TAG, "onCreate: Loading...")
                        }
                        is ApiState.Failure -> {
                            //hideViews()
                            Toast.makeText(this@PaymentActivity,
                                    "There Was An Error",
                                    Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }



            launch {
                paymentViewModel.updatedDraftCartOrder.collect { result ->
                    when (result) {
                        is ApiState.Success<DraftOrder> -> {

                            // to update draft order address and discount code

                            draftOrder = result.data

                        }
                        is ApiState.Loading -> {
                            Log.i(TAG, "onCreate: updatedDraftOrder Loading...")
                        }
                        is ApiState.Failure -> { //hideViews()
                            Toast.makeText(this@PaymentActivity,
                                    "There Was An Error",
                                    Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }


            launch {
                paymentViewModel.customer.collect { result ->
                    when (result) {
                        is ApiState.Success<Customer> -> {
                            customer = result.data
                            binding.streetText.text = customer.defaultAddress?.address1
                            binding.cityText.text = customer.defaultAddress?.city
                            binding.countryText.text = customer.defaultAddress?.country
                            binding.phoneText.text = customer.defaultAddress?.phone
                            binding.nameText.text =
                                "${customer.defaultAddress?.firstName}  ${customer.defaultAddress?.lastName}"
                            addressesList = customer.addresses!!
                            addressAdapter.submitList(addressesList)
                        }
                        is ApiState.Loading -> {
                            Log.i(TAG, "onCreate: updatedDraftOrder Loading...")
                        }
                        is ApiState.Failure -> { //hideViews()
                            Toast.makeText(this@PaymentActivity,
                                    "There Was An Error",
                                    Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

    }

    override fun onAddressClickListener(addressId: Long) {
        addressesList.filter {
            it.id == addressId
        }.map {
            binding.streetText.text = it.address1
            binding.cityText.text = it.city
            binding.countryText.text = it.country
            binding.phoneText.text = it.phone
            binding.nameText.text =
                "${it.firstName} + ${it.lastName}"
        }
        addressBottomSheetDialog.dismiss()
    }

    override fun onMakeDefaultClickListener(addressId: Long) {
        TODO("Not yet implemented")
    }

    override fun onDeleteClickListener(addressId: Long) {
        TODO("Not yet implemented")
    }
}
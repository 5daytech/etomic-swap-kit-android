package com.fridaytech.demo.presentation.swap

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.fridaytech.etomicswapkit.R
import com.fridaytech.etomicswapkit.models.SwapResponse
import com.fridaytech.demo.presentation.MainViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_swap_maker.*

class MakerFragment : Fragment() {
    lateinit var viewModel: MainViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            viewModel = ViewModelProviders.of(it).get(MainViewModel::class.java)

            viewModel.makerOrderRequestData.observe(this, Observer {
                makerRequestData.text = Gson().toJson(it)
            })

            viewModel.makerContractIdLive.observe(this, Observer {
                makerDepositContractId.text = it
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_swap_maker, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        makerCreateOrder.setOnClickListener {
            viewModel.newContract(
                makerCoinSpinner.selectedItem.toString(),
                takerCoinSpinner.selectedItem.toString(),
                makerCoinAmount.text.toString().toBigDecimal(),
                takerCoinAmount.text.toString().toBigDecimal()
            )
        }

        makerConfirm.setOnClickListener {
            val takerResponse = Gson().fromJson(takerResponseData.text.toString(), SwapResponse::class.java)
            viewModel.onMakerDeposit(takerResponse)
        }

        makerWithdraw.setOnClickListener {
            viewModel.makerWithdraw(takerResponseContractId.text.toString())
        }
    }
}
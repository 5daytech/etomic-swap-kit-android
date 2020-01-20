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
import com.fridaytech.etomicswapkit.models.SwapRequest
import com.fridaytech.demo.presentation.MainViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_swap_taker.*

class TakerFragment : Fragment() {
    lateinit var viewModel: MainViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            viewModel = ViewModelProviders.of(it).get(MainViewModel::class.java)

            viewModel.takerOrderRequestData.observe(this, Observer {
                takerResponseData.text = Gson().toJson(it)
            })

            viewModel.takerContractIdLive.observe(this, Observer {
                takerDepositContractId.text = it
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_swap_taker, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        takerConfirm.setOnClickListener {
            val swapRequest = Gson().fromJson(makerRequestData.text.toString(), SwapRequest::class.java)
            viewModel.onSwapResponse(swapRequest)
        }

        takerDeposit.setOnClickListener {
            viewModel.onTakerDeposit()
        }

        takerWithdraw.setOnClickListener {
            viewModel.takerWithdraw(makerContractId.text.toString())
        }
    }
}
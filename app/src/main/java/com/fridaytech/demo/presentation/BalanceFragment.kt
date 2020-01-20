package com.fridaytech.demo.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.fridaytech.etomicswapkit.R
import kotlinx.android.synthetic.main.fragment_balance.*

class BalanceFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            viewModel = ViewModelProviders.of(it).get(MainViewModel::class.java)

            viewModel.rethBalance.observe(this, Observer { balance ->
                rethBalance.text = "RETH: ${balance ?: 0}"
            })

            viewModel.kethBalance.observe(this, Observer { balance ->
                kethBalance.text = "KETH: ${balance ?: 0}"
            })

            viewModel.lastBlockHeightRopsten.observe(this, Observer { lastBlock ->
                currentBlockHeightRopsten.text = "Ropsten Last block: ${lastBlock ?: 0}"
            })

            viewModel.lastBlockHeightKovan.observe(this, Observer { lastBlock ->
                currentBlockHeightKovan.text = "Kovan Last block: ${lastBlock ?: 0}"
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_balance, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refresh.setOnClickListener {
            viewModel.refresh()
        }

        receiveAddressBtn.setOnClickListener {
            receiveAddress.text = viewModel.receiveAddress
        }
    }
}

package com.fridaytech.demo.presentation.swap

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProviders
import com.fridaytech.etomicswapkit.R
import com.fridaytech.demo.presentation.MainViewModel
import kotlinx.android.synthetic.main.fragment_swap.*

class SwapFragment : Fragment() {
    private lateinit var adapter: SwapPagerAdapter
    lateinit var viewModel: MainViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            viewModel = ViewModelProviders.of(it).get(MainViewModel::class.java)
        }

        adapter = SwapPagerAdapter(childFragmentManager)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_swap, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swapViewPager.adapter = adapter
        swapTabLayout.setupWithViewPager(swapViewPager)
    }

    private class SwapPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment = when(position) {
            0 -> MakerFragment()
            else -> TakerFragment()
        }

        override fun getCount(): Int = 2

        override fun getPageTitle(position: Int): CharSequence? = when(position) {
            0 -> "Maker"
            else -> "Taker"
        }

    }
}
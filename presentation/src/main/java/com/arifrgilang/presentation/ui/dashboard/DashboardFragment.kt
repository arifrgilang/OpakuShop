package com.arifrgilang.presentation.ui.dashboard

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.arifrgilang.presentation.ui.MainActivity
import com.arifrgilang.presentation.R
import com.arifrgilang.presentation.databinding.FragmentDashboardBinding
import com.arifrgilang.presentation.model.ItemUiModel
import com.arifrgilang.presentation.model.UserUiModel
import com.arifrgilang.presentation.util.CustomRvMargin
import com.arifrgilang.presentation.util.base.BaseBindingFragment
import com.arifrgilang.presentation.util.base.BaseRecyclerAdapter
import com.arifrgilang.presentation.util.event.observeEvent
import com.arifrgilang.presentation.util.view.toast
import com.arifrgilang.presentation.util.view.LogoutDialogFragment
import com.google.android.material.chip.Chip
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class DashboardFragment : BaseBindingFragment<FragmentDashboardBinding>() {
    private val rvAdapter: DashboardRvAdapter by inject()
    private val viewModel by viewModel<DashboardViewModel>()
    private val clothesList = mutableListOf("Tops & Tees", "Bottoms & Leggings", "Pajamas & Socks", "Dresses & Jumpsuits")
    private var currentCategory = "Tops & Tees"
    override fun contentView(): Int = R.layout.fragment_dashboard

    override fun setupData(savedInstanceState: Bundle?) {
        setViewModelObservers()
        viewModel.getUserData()
    }

    private fun setViewModelObservers() {
        viewModel.userData.observe(this, ::bindUserData)
        viewModel.isLoadingProfile.observe(this, ::showLoadingProfile)
        viewModel.isError.observeEvent(this, ::showError)
        viewModel.clothesData.observe(this, ::onDataFetched)
    }

    private fun onDataFetched(list: List<ItemUiModel>?) {
        rvAdapter.clearAndNotify()
        rvAdapter.insertAndNotify(list)
    }

    private fun bindUserData(userData: UserUiModel) {
        binding.user = userData
    }

    private fun showLoadingProfile(isLoading: Boolean) {
        binding.clUser.isVisible = !isLoading
        binding.pbUser.isVisible = isLoading
    }

    private fun showError(unit: Unit) {
        requireContext().toast("Error occurred")
    }

    override fun setupView() {
        initRecyclerView()
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
        binding.cgClothes.setOnCheckedChangeListener { _, checkedId ->
            /*
             * - The index need to be modded by statusList.size because CardGroup is generating
             *   unique itemId (n) even after onDestroy()
             * - The minus 1 is because array starts from zero index
             */
            Timber.d("Item Clicked")
            val index = (checkedId%clothesList.size)-1
            val filteredIndex = if(index<0) clothesList.size-1 else index
            clothesList[filteredIndex].let { status ->
                currentCategory = status
                viewModel.getClothesWithCategory(currentCategory)
            }
        }
        setupChipGroupClothes()
    }

    private fun initRecyclerView() {
        with(binding.rvClothes) {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = rvAdapter.apply {
                setOnItemClickListener(
                    object: BaseRecyclerAdapter.AdapterOnClick {
                        override fun onRecyclerItemClicked(extra: String) {
                            navigateToDetail(extra)
                        }
                    }
                )
            }
            addItemDecoration(
                CustomRvMargin(
                    requireContext(),
                    16,
                    CustomRvMargin.LINEAR_HORIZONTAL
                )
            )
        }
    }

    private fun navigateToDetail(itemId: String) {
        requireContext().toast("ITEM CLICKED $itemId")
    }

    private fun setupChipGroupClothes() {
        for (item in clothesList) {
            val chip = (layoutInflater.inflate(
                R.layout.item_selectable_chip,
                binding.cgClothes,
                false) as Chip).apply {
                text = item
            }
            binding.cgClothes.addView(chip)
        }
        binding.cgClothes.getChildAt(0).performClick()
    }

    private fun showLogoutDialog() {
        LogoutDialogFragment(
            object : LogoutDialogFragment.DialogCallback {
                override fun isAgree() {
                    (activity as MainActivity).performLogout()
                }
            }
        ).show(
            requireActivity().supportFragmentManager,
            "Logout Dialog Fragment"
        )
    }
}
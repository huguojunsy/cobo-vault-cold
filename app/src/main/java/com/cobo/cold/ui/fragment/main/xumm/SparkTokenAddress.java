/*
 *
 *  Copyright (c) 2020 Cobo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.cobo.cold.ui.fragment.main.xumm;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.cobo.cold.R;
import com.cobo.cold.databinding.CommonModalBinding;
import com.cobo.cold.databinding.SparkTokenAddressBinding;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.viewmodel.SparkTokenViewModel;

import java.util.Objects;

public class SparkTokenAddress extends BaseFragment<SparkTokenAddressBinding> {

    private int addressIndex;

    @Override
    protected int setView() {
        return R.layout.spark_token_address;
    }

    @Override
    protected void init(View view) {
        Bundle data = Objects.requireNonNull(getArguments());
        addressIndex = data.getInt("index");
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.info.setOnClickListener(v -> showGuide());
        ViewModelProviders.of(this)
                .get(SparkTokenViewModel.class)
                .getAddress(addressIndex).observe(this, s -> {
            mBinding.address.setText(s);
            mBinding.qr.qrcode.disableMultipart();
            mBinding.qr.qrcode.setData(s);
            mBinding.addressPath.setText(
                    String.format("(%s/0/%s)", SparkTokenViewModel.XPUB_PATH, addressIndex));
        });
    }

    private void showGuide() {
        ModalDialog modalDialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(mActivity), R.layout.common_modal,
                null, false);
        binding.title.setText(getString(R.string.spark_token_prepare_title));
        binding.subTitle.setText(R.string.spark_token_claim_guide);
        binding.subTitle.setGravity(Gravity.START);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(vv -> modalDialog.dismiss());
        modalDialog.setBinding(binding);
        modalDialog.show(mActivity.getSupportFragmentManager(), "");
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}

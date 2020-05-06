/*
 * Copyright (c) 2020 Cobo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cobo.cold.ui.fragment.main.electrum;

import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.cobo.coinlib.utils.Base43;
import com.cobo.cold.R;
import com.cobo.cold.databinding.ElectrumTxBinding;
import com.cobo.cold.db.entity.TxEntity;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.ui.fragment.main.TxConfirmFragment;
import com.cobo.cold.viewmodel.CoinListViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.cobo.cold.ui.fragment.main.electrum.ElectrumTxConfirmFragment.showExportTxnDialog;


public class ElectrumTxFragment extends BaseFragment<ElectrumTxBinding> {

    public static final String KEY_TX_ID = "txid";
    private TxEntity txEntity;

    @Override
    protected int setView() {
        return R.layout.electrum_tx;
    }

    @Override
    protected void init(View view) {
        Bundle data = Objects.requireNonNull(getArguments());
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        CoinListViewModel viewModel = ViewModelProviders.of(mActivity).get(CoinListViewModel.class);
        viewModel.loadTx(data.getString(KEY_TX_ID)).observe(this, txEntity -> {
            mBinding.setTx(txEntity);
            this.txEntity = txEntity;
            String signTx = getSignTxString(txEntity);
            if (signTx.length() <= 1000) {
                new Handler().postDelayed(() -> mBinding.txDetail.qrcodeLayout.qrcode.setData(signTx), 500);
            } else {
                mBinding.txDetail.qrcodeLayout.qrcode.setVisibility(View.GONE);
            }
            refreshAmount();
            refreshFromList();
            refreshReceiveList();
            mBinding.txDetail.exportToSdcard.setOnClickListener(v -> {
                showExportTxnDialog(mActivity, txEntity.getTxId(), txEntity.getSignedHex());
            });
        });

    }

    private void refreshFromList() {
        String from = txEntity.getFrom();
        List<TxConfirmFragment.TransactionItem> items = new ArrayList<>();
        try {
            JSONArray outputs = new JSONArray(from);
            for (int i = 0; i < outputs.length(); i++) {
                JSONObject out = outputs.getJSONObject(i);
                items.add(new TxConfirmFragment.TransactionItem(i,
                        out.getLong("value"), out.getString("address")));
            }
        } catch (JSONException e) {
            return;
        }
        TxConfirmFragment.TransactionItemAdapter adapter
                = new TxConfirmFragment.TransactionItemAdapter(mActivity,
                TxConfirmFragment.TransactionItem.ItemType.INPUT);
        adapter.setItems(items);
        mBinding.txDetail.fromList.setAdapter(adapter);
    }

    private void refreshAmount() {
        SpannableStringBuilder style = new SpannableStringBuilder(txEntity.getAmount());
        style.setSpan(new ForegroundColorSpan(mActivity.getColor(R.color.colorAccent)),
                0, txEntity.getAmount().indexOf(" "), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mBinding.txDetail.amount.setText(style);
    }

    private void refreshReceiveList() {
        String to = txEntity.getTo();
        List<TxConfirmFragment.TransactionItem> items = new ArrayList<>();
        try {
            JSONArray outputs = new JSONArray(to);
            for (int i = 0; i < outputs.length(); i++) {
                items.add(new TxConfirmFragment.TransactionItem(i,
                        outputs.getJSONObject(i).getLong("value"),
                        outputs.getJSONObject(i).getString("address")
                ));
            }
        } catch (JSONException e) {
            return;
        }
        TxConfirmFragment.TransactionItemAdapter adapter =
                new TxConfirmFragment.TransactionItemAdapter(mActivity, TxConfirmFragment.TransactionItem.ItemType.OUTPUT);
        adapter.setItems(items);
        mBinding.txDetail.toList.setAdapter(adapter);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private String getSignTxString(TxEntity txEntity) {
        byte[] txData = Hex.decode(txEntity.getSignedHex());
        return Base43.encode(txData);
    }

}
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

package com.cobo.cold.ui.fragment;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;

import com.cobo.cold.R;
import com.cobo.cold.databinding.TabletQrcodeBinding;
import com.cobo.cold.mnemonic.MnemonicInputTable;
import com.cobo.cold.ui.fragment.setup.SetupVaultBaseFragment;

public class TabletQrcodeFragment extends SetupVaultBaseFragment<TabletQrcodeBinding> {
    @Override
    protected int setView() {
        return R.layout.tablet_qrcode;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.next.setOnClickListener(v -> next());
        mBinding.createSharding.setOnClickListener(v->navigate(R.id.action_to_shardingSettingFragment));
        mBinding.tablet.setOnClickListener(new View.OnClickListener() {
            final int COUNTS = 3;
            final long DURATION = 3000L;
            long[] mHits = new long[COUNTS];

            @Override
            public void onClick(View v) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
                    navigate(R.id.action_to_rollingDiceGuideFragment);
                }
            }
        });
    }

    private void next() {
        viewModel.setMnemonicCount(MnemonicInputTable.TWEENTYFOUR);
        navigate(R.id.action_to_generateMnemonicFragment);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}

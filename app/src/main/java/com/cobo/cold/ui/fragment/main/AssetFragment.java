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

package com.cobo.cold.ui.fragment.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.AppExecutors;
import com.cobo.cold.R;
import com.cobo.cold.databinding.AssetFragmentBinding;
import com.cobo.cold.databinding.DialogBottomSheetBinding;
import com.cobo.cold.db.entity.CoinEntity;
import com.cobo.cold.ui.MainActivity;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.ui.modal.ProgressModalDialog;
import com.cobo.cold.viewmodel.AddAddressViewModel;
import com.cobo.cold.viewmodel.CoinViewModel;
import com.cobo.cold.viewmodel.PublicKeyViewModel;
import com.cobo.cold.viewmodel.WatchWallet;
import com.cobo.cold.viewmodel.XummTxConfirmViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static com.cobo.cold.ui.fragment.Constants.KEY_COIN_CODE;
import static com.cobo.cold.ui.fragment.Constants.KEY_COIN_ID;
import static com.cobo.cold.ui.fragment.Constants.KEY_ID;

public class AssetFragment extends BaseFragment<AssetFragmentBinding>
        implements Toolbar.OnMenuItemClickListener, NumberPickerCallback {

    public static final String TAG = "AssetFragment";

    private final ObservableField<String> query = new ObservableField<>();

    private boolean isInSearch;
    private Fragment[] fragments;
    private boolean showPublicKey;
    private String coinId;
    private String coinCode;
    private long id;
    private AddressNumberPicker mAddressNumberPicker;
    private boolean hasAddress;
    private WatchWallet watchWallet;

    @Override
    protected int setView() {
        return R.layout.asset_fragment;
    }

    @Override
    protected void init(View view) {
        watchWallet = WatchWallet.getWatchWallet(mActivity);
        if (watchWallet == WatchWallet.XRP_TOOLKIT) {
            mBinding.toolbar.setNavigationIcon(R.drawable.menu);
            mBinding.toolbar.setTitle(watchWallet.getWalletName(mActivity));
            coinId = Coins.XRP.coinId();
            coinCode = Coins.XRP.coinCode();
            LiveData<CoinEntity> coinEntityLiveData = ViewModelProviders.of(this)
                    .get(XummTxConfirmViewModel.class)
                    .loadXrpCoinEntity();

            coinEntityLiveData.observe(this, coinEntity -> {
                id = coinEntity.getId();
                updateUI();
                coinEntityLiveData.removeObservers(this);
            });
        } else {
            Bundle data = Objects.requireNonNull(getArguments());
            coinId = data.getString(KEY_COIN_ID);
            coinCode = data.getString(KEY_COIN_CODE);
            id = data.getLong(KEY_ID);
            showPublicKey = Coins.showPublicKey(coinCode);
            updateUI();
        }
    }

    private void updateUI() {
        if (watchWallet == WatchWallet.POLKADOT_JS) {
            mBinding.button.setVisibility(View.VISIBLE);
            mBinding.button.setOnClickListener(v -> syncPolkadot());
        } else {
            mBinding.toolbar.inflateMenu(getMenuResId());
            mBinding.button.setVisibility(View.GONE);
        }
        mBinding.toolbar.setOnMenuItemClickListener(this);
        mBinding.toolbar.setNavigationOnClickListener(v -> {
            if (watchWallet == WatchWallet.XRP_TOOLKIT) {
                ((MainActivity) mActivity).toggleDrawer(v);
            } else {
                navigateUp();
            }
        });
        initSearchView();
        initTabs();
    }

    private void syncPolkadot() {
        Bundle bundle = new Bundle();
        bundle.putString("coinCode", coinCode);
        navigate(R.id.action_to_syncFragment,bundle);
    }

    private int getMenuResId() {
        if (watchWallet == WatchWallet.XRP_TOOLKIT) {
            return R.menu.xrp_toolkit;
        }
        return (showPublicKey || Coins.isPolkadotFamily(coinCode)) ? R.menu.asset_without_add : R.menu.asset;
    }

    private void initTabs() {
        if (!showPublicKey) {
            initViewPager();
        } else {
            PublicKeyViewModel viewModel = ViewModelProviders.of(this)
                    .get(PublicKeyViewModel.class);
            Handler handler = new Handler();
            AppExecutors.getInstance().diskIO().execute(() -> {
                String address = viewModel.getAddress(coinId);
                hasAddress = !TextUtils.isEmpty(address);
                handler.post(this::initViewPager);
            });
        }

    }

    private void initViewPager() {
        String[] title = {showPublicKey && !hasAddress ? getString(R.string.tab_my_pubkey)
                : getString(R.string.tab_my_address),
                getString(R.string.tab_transaction_history)};
        if (fragments == null) {
            fragments = new Fragment[title.length];
            if (showPublicKey) {
                fragments[0] = PublicKeyFragment.newInstance(coinId);
            } else {
                fragments[0] = AddressFragment.newInstance(id, coinId, coinCode);
            }
            fragments[1] = TxListFragment.newInstance(coinId, coinCode);
        }

        mBinding.viewpager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager(),
                BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments[position];
            }

            @Override
            public int getCount() {
                return title.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return title[position];
            }
        });
        mBinding.tab.setupWithViewPager(mBinding.viewpager);
    }

    private void initSearchView() {
        mBinding.btnCancel.setOnClickListener(v -> exitSearch());
        View.OnKeyListener backListener = (view, key_code, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                if (key_code == KeyEvent.KEYCODE_BACK) {
                    if (isInSearch) {
                        exitSearch();
                        return true;
                    }
                }
            }
            return false;
        };
        mBinding.search.setOnKeyListener(backListener);
        query.set("");
        mBinding.setQuery(query);
        query.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (fragments[0] instanceof AddressFragment) {
                    AddressFragment addressFragment = (AddressFragment) fragments[0];
                    addressFragment.setQuery(query.get());
                }

                TxListFragment txListFragment = (TxListFragment) fragments[1];
                txListFragment.setQuery(query.get());

            }
        });

    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        CoinViewModel.Factory factory = new CoinViewModel.Factory(mActivity.getApplication(), id, coinId);
        CoinViewModel viewModel = ViewModelProviders.of(this, factory)
                .get(CoinViewModel.class);

        mBinding.setCoinViewModel(viewModel);
        subscribeUi(viewModel);
    }

    private void subscribeUi(CoinViewModel viewModel) {
        viewModel.getObservableCoin().observe(this, viewModel::setCoin);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                enterSearch();
                break;
            case R.id.action_add:
                handleAddAddress();
                break;
            case R.id.action_more:
                showBottomSheetMenu();
                break;
            case R.id.action_scan:
                Bundle data = new Bundle();
                data.putString("purpose", "xrpTransaction");
                navigate(R.id.action_to_QRCodeScanFragment, data);
                break;
            default:
                break;
        }
        return true;
    }

    private void handleAddAddress() {
        if (fragments[0] instanceof AddressFragment) {
            ((AddressFragment) fragments[0]).exitEditAddressName();
        }
        if (mAddressNumberPicker == null) {
            mAddressNumberPicker = new AddressNumberPicker();
            mAddressNumberPicker.setCallback(this);
        }
        mAddressNumberPicker.show(mActivity.getSupportFragmentManager(), "");
    }

    private void showBottomSheetMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        DialogBottomSheetBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.dialog_bottom_sheet,null,false);
        binding.addAddress.setOnClickListener(v-> {
            handleAddAddress();
            dialog.dismiss();

        });
        binding.exportXpubToElectrum.setOnClickListener(v-> {
            navigate(R.id.action_to_electrum_guide);
            dialog.dismiss();
        });
        dialog.setContentView(binding.getRoot());
        dialog.show();
    }

    private void enterSearch() {
        isInSearch = true;
        if (fragments[0] != null && fragments[0] instanceof AddressFragment) {
            ((AddressFragment) fragments[0]).enterSearch();
        }
        mBinding.searchBar.setVisibility(View.VISIBLE);
        mBinding.search.requestFocus();
        InputMethodManager inputManager =
                (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(mBinding.search, 0);
        }
    }

    private void exitSearch() {
        isInSearch = false;
        mBinding.search.setText("");
        mBinding.searchBar.setVisibility(View.INVISIBLE);
        mBinding.search.clearFocus();
        InputMethodManager inputManager =
                (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(mBinding.search.getWindowToken(), 0);
        }
    }

    @Override
    public void onValueSet(int value) {
        AddAddressViewModel.Factory factory = new AddAddressViewModel.Factory(mActivity.getApplication(),
                id);
        AddAddressViewModel viewModel = ViewModelProviders.of(this, factory)
                .get(AddAddressViewModel.class);

        ProgressModalDialog dialog = ProgressModalDialog.newInstance();
        dialog.show(Objects.requireNonNull(mActivity.getSupportFragmentManager()), "");
        Handler handler = new Handler();
        AppExecutors.getInstance().diskIO().execute(() -> {
            CoinEntity coinEntity = viewModel.getCoin(coinId);
            if (coinEntity != null) {
                int addrCount = coinEntity.getAddressCount();
                List<String> observableAddressNames = new ArrayList<>();
                for (int i = addrCount; i < value + addrCount; i++) {
                    String name = coinEntity.getCoinCode() + "-" + i ;
                    observableAddressNames.add(name);
                }
                viewModel.addAddress(observableAddressNames);

                handler.post(() -> viewModel.getObservableAddState().observe(this, complete -> {
                    if (complete) {
                        handler.postDelayed(dialog::dismiss, 500);
                    }
                }));
            }
        });
    }
}

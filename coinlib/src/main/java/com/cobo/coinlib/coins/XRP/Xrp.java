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

package com.cobo.coinlib.coins.XRP;

import androidx.annotation.NonNull;

import com.cobo.coinlib.coins.AbsCoin;
import com.cobo.coinlib.coins.AbsDeriver;
import com.cobo.coinlib.coins.AbsTx;
import com.cobo.coinlib.coins.SignTxResult;
import com.cobo.coinlib.exception.InvalidTransactionException;
import com.cobo.coinlib.interfaces.Coin;
import com.cobo.coinlib.interfaces.SignCallback;
import com.cobo.coinlib.interfaces.Signer;
import com.cobo.coinlib.utils.B58;
import com.cobo.coinlib.utils.Coins;

import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.DeterministicKey;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;

public class Xrp extends AbsCoin implements Coin {
    public static final String DEFAULT_ALPHABET = "rpshnaf39wBUDNEGHJKLM4PQRST7VWXYZ2bcdeCg65jkm8oFqi1tuvAxyz";

    public Xrp(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.XRP.coinCode();
    }

    public void signTx(@NonNull JSONObject txObj, SignCallback callback, Signer signer) {
        if (signer == null) {
            callback.onFail();
            return;
        }
        SignTxResult result = ((XrpImpl)impl).signTx(txObj, signer);
        if (result != null && result.isValid()) {
            callback.onSuccess(result.txId, result.txHex);
        } else {
            callback.onFail();
        }
    }

    public static String encodeAccount(String pubkey) {
        return new B58(DEFAULT_ALPHABET)
                .encodeToStringChecked(Utils.sha256hash160(Hex.decode(pubkey)), 0);
    }

    public static class Tx extends AbsTx {

        public Tx(JSONObject object, String coinCode) throws JSONException, InvalidTransactionException {
            super(object, coinCode);
        }

        @Override
        protected void parseMetaData() throws JSONException {
            to = metaData.getString("to");
            amount = metaData.getLong("amount") / Math.pow(10, decimal);
            memo = metaData.optString("memo");
            fee = metaData.getLong("fee") / Math.pow(10, decimal);
        }


    }

    public static class Deriver extends AbsDeriver {

        @Override
        public String derive(String xPubKey, int changeIndex, int addrIndex) {
            DeterministicKey address = getAddrDeterministicKey(xPubKey, changeIndex, addrIndex);
            System.out.println(Hex.toHexString(address.getPubKey()));
            return new B58(DEFAULT_ALPHABET).encodeToStringChecked(address.getPubKeyHash(), 0);
        }

        @Override
        public String derive(String xPubKey) {
            return new B58(DEFAULT_ALPHABET)
                    .encodeToStringChecked(getDeterministicKey(xPubKey).getPubKeyHash(), 0);
        }
    }
}

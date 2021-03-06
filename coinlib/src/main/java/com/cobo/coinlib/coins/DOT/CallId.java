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

package com.cobo.coinlib.coins.DOT;

public enum CallId {
    POLKADOT(0x0503, 0x0500),
    KUSAMA(0x0603, 0x0600);

    public int transferKeepAlive;
    public int transfer;

    CallId(int transferKeepAlive,
           int transfer) {
        this.transferKeepAlive = transferKeepAlive;
        this.transfer = transfer;
    }
}
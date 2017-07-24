package com.nexflare.silentplace.Model;

import java.util.ArrayList;

/**
 * Created by 15103068 on 24-07-2017.
 */

public class DistanceMatrixResult {
    ArrayList<Rows> rows;

    public DistanceMatrixResult(ArrayList<Rows> rows) {
        this.rows = rows;
    }

    public ArrayList<Rows> getRows() {
        return rows;
    }
}

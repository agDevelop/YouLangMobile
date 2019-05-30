package com.ag.youlang;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public enum AddWordActionResult implements Serializable {
    @SerializedName("0")
    Added,

    @SerializedName("1")
    Deleted,

    @SerializedName("2")
    Failed
}
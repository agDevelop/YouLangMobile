package com.ag.youlang;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public enum ActionResult implements Serializable {
    @SerializedName("0")
    Success,

    @SerializedName("1")
    Failed
}

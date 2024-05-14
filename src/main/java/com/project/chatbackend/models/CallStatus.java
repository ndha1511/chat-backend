package com.project.chatbackend.models;

public enum CallStatus {
    START, // bắt đầu gọi, chưa accept
    CALLING, // đang gọi
    REJECT, // từ chối
    CANCEL, // hủy
    MISSED, // nhỡ
    END // kết thúc
}

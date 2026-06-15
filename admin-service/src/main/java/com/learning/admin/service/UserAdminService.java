package com.learning.admin.service;

public interface UserAdminService {
    void updateUserStatus(Long userId, Integer status, Integer role);
}

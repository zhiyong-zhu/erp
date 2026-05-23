package com.erp.product.security;

import com.erp.common.security.domain.LoginUser;
import com.erp.common.security.util.SecurityUtils;
import com.erp.product.permission.ProductPermissionCodes;
import org.springframework.stereotype.Service;

@Service
public class ProductFieldPermissionServiceImpl implements ProductFieldPermissionService {
    @Override
    public boolean canViewCostPrice() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return loginUser != null && loginUser.getPermissions().contains(ProductPermissionCodes.PRODUCT_COST);
    }
}

package com.revy.example.admin.api.account;

import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.common.AbstractCrudApi;
import com.revy.example.core.common.ApiPageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/accounttransaction")
class AccountTransactionController extends AbstractCrudApi<Long, Void, Void, Void, Void> {

    @Override
    protected ApiPageResponse<Void> getPage(Pageable pageable, Void searchRequest) {
        return null;
    }

    @Override
    protected Void doCreate(Void request) {
        return null;
    }

    @Override
    protected Void doGet(Long id) {
        return null;
    }

    @Override
    protected Void doUpdate(Long id, Void request) {
        return null;
    }

    @Override
    protected void doDelete(Long id) {
    }
}

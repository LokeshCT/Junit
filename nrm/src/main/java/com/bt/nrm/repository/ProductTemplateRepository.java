package com.bt.nrm.repository;

import java.util.List;
import java.util.Set;
import com.bt.pms.dto.ProductCategoryDTO;

public interface ProductTemplateRepository {
    Set<String> getProductIdsByUserId(String userId);
    List<ProductCategoryDTO> getProductsByUserId(String userId);
}

package com.organizer.grocery.service;

import com.organizer.grocery.dto.ProductDto;
import com.organizer.grocery.model.Product;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface ProductService {
    List<ProductDto> getAllProducts();
    ProductDto getProductById(Long id);
    ProductDto createProduct(ProductDto productDto, boolean notify) throws Exception;
    ProductDto updateProduct(Long id, ProductDto productDto) throws Exception;
    void notifyProductsUpdated(List<ProductDto> updatedProducts) throws Exception;
    void notifyUpdateProduct(ProductDto updatedProduct) throws Exception;
    void notifyUpdateProduct(Product updatedProduct) throws Exception;
    void deleteProduct(Long id, boolean notify) throws Exception;
}
package com.organizer.grocery.service;

import com.organizer.grocery.dto.ProductDto;
import com.organizer.grocery.exceptions.ProductLocationExistsException;
import com.organizer.grocery.exceptions.ProductNotFoundException;
import com.organizer.grocery.model.Product;
import com.organizer.grocery.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImplement implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImplement(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        return convertToDto(product);
    }

    @Override
    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        int x = productDto.location().x();
        int y = productDto.location().y();
        if (productRepository.findByLocation_XAndLocation_Y(x, y).isPresent()) {
            throw new ProductLocationExistsException("A product already exists at location (" + x + ", " + y + ").");
        }
        Product product = convertToEntity(productDto);
        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        existingProduct.setName(productDto.name());
        existingProduct.setPrice(productDto.price());
        existingProduct.setQuantity(productDto.quantity());
        existingProduct.setLocation(productDto.location());

        Product updatedProduct = productRepository.save(existingProduct);
        return convertToDto(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    private ProductDto convertToDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getQuantity(),
                product.getLocation()
        );
    }

    private Product convertToEntity(ProductDto productDto) {
        return new Product(
                productDto.id(),
                productDto.name(),
                productDto.price(),
                productDto.quantity(),
                productDto.location()
        );
    }
}
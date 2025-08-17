package com.organizer.grocery.service;

import com.organizer.grocery.dto.ProductDto;
import com.organizer.grocery.exceptions.ProductLocationExistsException;
import com.organizer.grocery.exceptions.ProductNotFoundException;
import com.organizer.grocery.model.Product;
import com.organizer.grocery.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImplement implements ProductService {

    private final ProductRepository productRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ProductServiceImplement(ProductRepository productRepository, SimpMessagingTemplate messagingTemplate) {
        this.productRepository = productRepository;
        this.messagingTemplate = messagingTemplate;
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
    public ProductDto createProduct(ProductDto productDto, boolean notify) throws Exception {
        int x = productDto.location().x();
        int y = productDto.location().y();
        Optional<Product> existingProduct = productRepository.findByLocation_XAndLocation_Y(x, y);
        if (existingProduct.isPresent()) {
            if(!existingProduct.get().getName().equals(productDto.name())) {
                throw new ProductLocationExistsException("A different product already exists at location (" + x + ", " + y + ") with name: " + existingProduct.get().getName());
            }else if(!existingProduct.get().getPrice().equals(productDto.price())) {
                throw new ProductLocationExistsException("The same product already exists at location (" + x + ", " + y + ") with price: " + existingProduct.get().getPrice() + "but the new product costs " + productDto.price());
            }
            else{
                existingProduct.get().setQuantity(existingProduct.get().getQuantity() + productDto.quantity());
                Product updatedProduct = productRepository.save(existingProduct.get());
                return convertToDto(updatedProduct);
            }
        }
        Product product = convertToEntity(productDto);
        Product savedProduct = productRepository.save(product);
        ProductDto createdProductDto = convertToDto(savedProduct);
        if(notify){
            notifyUpdateProduct(createdProductDto);
        }
        return createdProductDto;
    }

    @Override
    @Transactional
    public ProductDto updateProduct(Long id, ProductDto productDto) throws Exception {
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
    public void deleteProduct(Long id, boolean notify) throws Exception {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        if(notify) {
            notifyProductsUpdated(getAllProducts());
        }
    }

    @Override
    public void notifyProductsUpdated(List<ProductDto> products) {
        messagingTemplate.convertAndSend("/topic/productsUpdate", products);
    }

    @Override
    public void notifyUpdateProduct(ProductDto product) {
        messagingTemplate.convertAndSend("/topic/productsUpdate", product);
    }

    @Override
    public void notifyUpdateProduct(Product updatedProduct) {
        notifyUpdateProduct(convertToDto(updatedProduct));
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
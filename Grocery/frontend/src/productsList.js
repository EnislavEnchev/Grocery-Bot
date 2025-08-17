import React, { useState, useEffect, useRef} from 'react';
import { productService } from './services/api';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const ProductsList = () => {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [newProduct, setNewProduct] = useState({ name: '', price: '', quantity: '', x: '', y: '' });
    const [showLocations, setShowLocations] = useState(false);

    const [productQuantityMap, setProductQuantityMap] = useState({});
    const stompClientRef = useRef(null);

    useEffect(() => {
        const fetchProducts = async () => {
            try {
                const response = await productService.getAllProducts();
                setProducts(response.data);
                const productsMap = new Map();
                response.data.forEach(product => {
                    if(productsMap[product.name]) {
                        productsMap[product.name] += product.quantity;
                    }else{
                        productsMap[product.name] = product.quantity;
                    }
                });
                setProductQuantityMap(productsMap);
            } catch (err) {
                setError('Failed to fetch products.');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        fetchProducts();
    }, []);

    useEffect(() => {
        if (stompClientRef.current) return;
        stompClientRef.current = true;
        const socket = new SockJS("http://localhost:8081/ws");
        console.log("Connecting to WebSocket...");
        const stompClient = new Client({
            webSocketFactory: () => socket,
            reconnectDelay: 5000,
        });
        stompClient.onConnect = () => {
            stompClient.subscribe("/topic/productsUpdate", (message) => {
                const updatedProduct = JSON.parse(message.body);
                setProducts((prev) => {
                    const existingIndex = prev.findIndex(p => p.id === updatedProduct.id);
                    let newList;
                    if (existingIndex >= 0) {
                        newList = [...prev];
                        newList[existingIndex] = updatedProduct;
                    } else {
                        newList = [...prev, updatedProduct];
                    }
                    setProductQuantityMap(prevMap => ({
                        ...prevMap,
                        [updatedProduct.name]: updatedProduct.quantity
                    }));
                    return newList;
                });
            });
        };
        stompClient.activate();
        stompClientRef.current = stompClient;

        return () => {
            if (stompClientRef.current && stompClientRef.current.deactivate) {
                stompClientRef.current.deactivate();
            }
            stompClientRef.current = null;
        };
    }, []);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setNewProduct(prev => ({ ...prev, [name]: value }));
    };

    const handleAddProduct = async (e) => {
        e.preventDefault();
        try {
            const response = await productService.createProduct({
                name: newProduct.name,
                price: parseFloat(newProduct.price).toFixed(2),
                quantity: parseInt(newProduct.quantity, 10),
                location: {
                    x: parseFloat(newProduct.x),
                    y: parseFloat(newProduct.y)
                }
            });
            setProducts(prev => [...prev, response.data]);
            setProductQuantityMap(prevMap => {
                const updatedMap = { ...prevMap };
                if (updatedMap[response.data.name]) {
                    updatedMap[response.data.name] += response.data.quantity;
                } else {
                    updatedMap[response.data.name] = response.data.quantity;
                }
                return updatedMap;
            });
            setNewProduct({ name: '', price: '', quantity: '', x: '', y: '' });
        } catch (err) {
            setError('Failed to add product.');
        }
    };

    const handleDeleteProduct = async (id) => {
        try {
            await productService.deleteProduct(id);
            setProducts(prev => prev.filter(p => p.id !== id));
        } catch (err) {
            setError('Failed to delete product.');
        }
    };

    if (loading) return <p>Loading products...</p>;
    if (error) return <p>{error}</p>;

    return (
        <div>
            <h2>Available Products</h2>
            <button onClick={() => setShowLocations(prev => !prev)} style={{ marginBottom: '1em' }}>
                {showLocations ? 'Hide Locations' : 'Show Product Locations'}
            </button>
            {!showLocations && (
                <form onSubmit={handleAddProduct} style={{ marginBottom: '1em' }}>
                    <input
                        type="text"
                        name="name"
                        placeholder="Name"
                        value={newProduct.name}
                        onChange={handleInputChange}
                        required
                    />
                    <input
                        type="number"
                        name="price"
                        placeholder="Price"
                        value={newProduct.price}
                        onChange={handleInputChange}
                        required
                        step="0.01"
                    />
                    <input
                        type="number"
                        name="quantity"
                        placeholder="Quantity"
                        value={newProduct.quantity}
                        onChange={handleInputChange}
                        required
                    />
                    <input
                        type="number"
                        name="x"
                        placeholder="X coordinate"
                        value={newProduct.x}
                        onChange={handleInputChange}
                        required
                        step="0.01"
                    />
                    <input
                        type="number"
                        name="y"
                        placeholder="Y coordinate"
                        value={newProduct.y}
                        onChange={handleInputChange}
                        required
                        step="0.01"
                    />
                    <button type="submit">Add Product</button>
                </form>
            )}
            {!showLocations && (
                <ul>
                    {Object.entries(productQuantityMap).map(([name, quantity]) => (
                        <li key={name}>
                            {name} - Total Quantity: {quantity}
                        </li>
                    ))}
                </ul>
            )}
            {showLocations && (
                <div>
                    <h3>Product Locations</h3>
                    <ul>
                        {products && products.map(product => (
                            <li key={product.id}>
                                {product.name} ({product.quantity} at ${product.price} each)- Location: (
                                    {product.location?.x}, {product.location?.y}
                            )
                            <button onClick={() => handleDeleteProduct(product.id)} style={{ marginLeft: '1em' }}>Delete</button>
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    );
};
export default ProductsList;
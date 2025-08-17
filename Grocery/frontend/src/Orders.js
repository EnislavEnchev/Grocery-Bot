import React, { useState, useEffect } from 'react';
import { productService, orderService } from './services/api';

const Orders = () => {
    const [orders, setOrders] = useState([]);
    const [products, setProducts] = useState([]);
    const [selectedProducts, setSelectedProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [orderId, setOrderId] = useState('');
    const [singleOrder, setSingleOrder] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [, productsRes] = await Promise.all([
                    productService.getAllProducts()
                ]);
                setProducts(productsRes.data);
            } catch (err) {
                setError('Failed to fetch products.');
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, []);

    const handleProductSelect = (productId) => {
        setSelectedProducts(prev =>
            prev.includes(productId)
                ? prev.filter(id => id !== productId)
                : [...prev, productId]
        );
    };

    const handleOrderSubmit = async (e) => {
        e.preventDefault();
        if (selectedProducts.length === 0) return;
        try {
            await orderService.createOrder({ productIds: selectedProducts });
            setSelectedProducts([]);
            const ordersRes = await orderService.getAllOrders();
            setOrders(ordersRes.data);
        } catch (err) {
            setError('Failed to create order.');
        }
    };

    const handleFetchOrder = async (e) => {
        e.preventDefault();
        if (!orderId) return;
        setError(null);
        setSingleOrder(null);
        try {
            const res = await orderService.getOrder(orderId);
            setSingleOrder(res.data);
        } catch (err) {
            setError('Order not found.');
        }
    };

    if (loading) return <p>Loading...</p>;
    if (error) return <p>{error}</p>;

    return (
        <div>
            <h2>Place a New Order</h2>
            <form onSubmit={handleOrderSubmit}>
                <ul>
                    {products.map(product => (
                        <li key={product.id}>
                            <label>
                                <input
                                    type="checkbox"
                                    checked={selectedProducts.includes(product.id)}
                                    onChange={() => handleProductSelect(product.id)}
                                />
                                {product.name} (In stock: {product.quantity})
                            </label>
                        </li>
                    ))}
                </ul>
                <button type="submit" disabled={selectedProducts.length === 0}>Submit Order</button>
            </form>
            <h2>View Single Order</h2>
            <form onSubmit={handleFetchOrder} style={{ marginBottom: '1em' }}>
                <input
                    type="number"
                    placeholder="Order ID"
                    value={orderId}
                    onChange={e => setOrderId(e.target.value)}
                    min="1"
                    required
                />
                <button type="submit">Fetch Order</button>
            </form>
            {singleOrder && (
                <div>
                    <h3>Order #{singleOrder.id}</h3>
                    <ul>
                        {singleOrder.products.map(p => (
                            <li key={p.id}>{p.name} (Qty: {p.quantity})</li>
                        ))}
                    </ul>
                </div>
            )}
            <h2>Your Orders</h2>
            <ul>
                {orders.map(order => (
                    <li key={order.id}>
                        Order #{order.id} - Products: {order.products.map(p => p.name).join(', ')}
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default Orders;

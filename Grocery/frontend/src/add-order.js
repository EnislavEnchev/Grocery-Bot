import React, { useState } from 'react';
import {orderService} from "./services/api";

const AddOrder = () => {
    const [productName, setProductName] = useState('');
    const [quantity, setQuantity] = useState(1);
    const [items, setItems] = useState([]);
    const [sending, setSending] = useState(false);

    const handleAddItem = () => {
        if (!productName || quantity < 1) return;
        setItems([...items, { productName, quantity }]);
        setProductName('');
        setQuantity(1);
    };

    const handleDeleteItem = (idx) => {
        setItems(items.filter((_, i) => i !== idx));
    };

    const handleSendOrder = async () => {
        setSending(true);
        try {
            const response = await orderService.createOrder({ items });
            const orderId = response.orderId || (response.data && response.data.orderId);
            alert('Order sent!\nOrder ID: ' + orderId + '\n' + JSON.stringify(items, null, 2));
            setItems([]);
        } catch (e) {
            let errorOrderId = e?.response?.data?.orderId;
            let msg = 'Failed to send order';
            if (errorOrderId) {
                msg += `\nOrder ID: ${errorOrderId}`;
            }
            alert(msg);
        }
        setSending(false);
    };

    return (
        <div>
            <h2>Add Order</h2>
            <div>
                <input
                    type="text"
                    placeholder="Product name"
                    value={productName}
                    onChange={e => setProductName(e.target.value)}
                />
                <input
                    type="number"
                    min="1"
                    value={quantity}
                    onChange={e => setQuantity(Number(e.target.value))}
                />
                <button onClick={handleAddItem}>Add Item</button>
            </div>
            <ul>
                {items.map((item, idx) => (
                    <li key={idx}>
                        {item.productName} - Quantity: {item.quantity}
                        <button
                            style={{marginLeft: '1em', color: 'white', background: 'red'}}
                            onClick={() => handleDeleteItem(idx)}
                        >
                            Delete
                        </button>
                    </li>
                ))}
            </ul>
            <button onClick={handleSendOrder} disabled={items.length === 0 || sending}>
                {sending ? 'Sending...' : 'Send Order'}
            </button>
        </div>
    );
};

export default AddOrder;
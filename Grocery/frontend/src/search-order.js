import React, {useEffect, useState} from 'react';
import { orderService } from './services/api';
import { orderItemService}  from "./services/api";
import {routeService} from "./services/api";

const SearchOrder = () => {
  const [orderId, setOrderId] = useState('');
  const [singleOrder, setSingleOrder] = useState(null);
  const [loading, setLoading] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [productsInOrder, setProductsInOrder] = useState([]);
  const [missingMap, setMissingMap] = useState({});
  const [route, setRoute] = useState([]);

  const handleFetchOrder = async (e) => {
    e.preventDefault();
    setLoading(true);
    setSingleOrder(null);
    try {
      const order = await orderService.getOrder(orderId);
      const products = await orderItemService.getProductsInOrder(orderId);
      const routeRes = await routeService.getRouteByOrderId(orderId);
      setSingleOrder(order.data);
      console.log('Fetched single order:', products.data);
      setProductsInOrder(products.data);
      setRoute(routeRes.data.visitedLocations);
      console.log('Fetched order:', order);
    } catch (error) {
      setSingleOrder({ error: error.message || 'Order not found' });
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteOrder = async () => {
    console.log('singleOrder:', singleOrder);
    if (!singleOrder || !singleOrder.orderId) return;
    setLoading(true);
    setDeleting(true);
    try {
      await orderService.deleteOrder(singleOrder.orderId);
      setSingleOrder(null);
      setProductsInOrder([]);
      setMissingMap({});
      alert('Order deleted successfully');
    } catch (error) {
      console.error('Error deleting order:', error);
      alert('Failed to delete order: ' + (error.message || 'Unknown error'));
    }
    setDeleting(false);
    setLoading(false);
  };

  function parseMissingItems(message) {
    const map = {};
    if (!message) return map;
    const items = message.split(';');
    console.log('Parsed items:', items);
    items.forEach(item => {
      const [name, qty] = item.split(':');
      if (name) {
        map[name] = parseInt(qty, 10);
      }
    });
    return map;
  }

  useEffect(() => {
    if (singleOrder && singleOrder.message) {
      setMissingMap(parseMissingItems(singleOrder.message));
    } else {
      setMissingMap({});
    }
  }, [singleOrder]);

  useEffect(() => {
    console.log('Updated route:', route);
  }, [route]);



  return (
      <div>
        <h2>Search Order by ID</h2>
        <form onSubmit={handleFetchOrder} style={{ marginBottom: '1em' }}>
          <input
              type="number"
              placeholder="Order ID"
              value={orderId}
              onChange={e => setOrderId(e.target.value)}
              min="1"
              required
          />
          <button type="submit" disabled={loading}>
            {loading ? 'Fetching...' : 'Fetch Order'}
          </button>
        </form>
        {singleOrder && !singleOrder.error && (
            <div>
              <h3>Order by {singleOrder.userFullName}</h3>
              <div>
                Status:
                {singleOrder.status === 'SUCCESSFUL' && <span style={{color: 'green'}}>  ✓ Order completed</span>}
                {singleOrder.status === 'PENDING' && <span style={{color: 'orange'}}> ⌛ Awaiting processing</span>}
                {singleOrder.status === 'FAILED' &&
                    <span style={{color: 'red'}}> ✗ Order failed </span>}
              </div>
              <button
                  onClick={handleDeleteOrder}
                  disabled={loading || deleting}
                  style={{marginBottom: '1em', color: 'white', background: 'red'}}
              >
                {deleting ? 'Deleting...' : 'Delete Order'}
              </button>
              <ul>
                {productsInOrder.map(product => {
                  const isMissing = missingMap.hasOwnProperty(product.productName);
                  return (
                      <li key={product.id}>
                        {product.productName}:
                        <span style={{marginLeft: '1em'}}>
                            {isMissing ? (
                                <span style={{color: 'red'}}>
                                Requested: {product.quantity} - Available: {missingMap[product.productName]}
                              </span>
                            ) : (
                                <span style={{color: 'green'}}>
                                Requested: {product.quantity}
                              </span>
                            )}
                        </span>
                      </li>
                  );
                })}
              </ul>
              {route.length > 0 && (
                  <div>
                    <h4>Route Coordinates:</h4>
                    <ol>
                      {route.map(routeStep => (
                          <li>
                            {routeStep.name} - {routeStep.quantity} for ${routeStep.price} at location ({routeStep.x}, {routeStep.y})
                          </li>
                      ))}
                    </ol>
                  </div>
              )}
            </div>
        )}

      </div>
  );
};

export default SearchOrder;


import axios from 'axios';

const API_URL = 'http://localhost:8081';

export function setAuthToken(token) {
    if (token) {
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else {
        delete axios.defaults.headers.common['Authorization'];
    }
}

export const authService = {
    login(credentials) {
        return axios.post(`${API_URL}/auth/login`, credentials);
    },
    signup(userInfo) {
        return axios.post(`${API_URL}/auth/signup`, userInfo);
    }
};

export const productService = {
    getAllProducts() {
        return axios.get(`${API_URL}/products`);
    },
    createProduct(product) {
        return axios.post(`${API_URL}/products`, product, {
            headers: { 'X-Notify': 'false' }
        });
    },
    deleteProduct(productId) {
        return axios.delete(`${API_URL}/products/${productId}`, {
            headers: { 'X-Notify': 'false' }
        });
    }
};

export const orderService = {
    createOrder(order) {
        return axios.post(`${API_URL}/orders`, order);
    },
    getOrder(orderId) {
        return axios.get(`${API_URL}/orders/${orderId}`);
    },
    deleteOrder(orderId) {
        return axios.delete(`${API_URL}/orders/${orderId}`);
    }
};

export const orderItemService = {
    getProductsInOrder(orderId) {
        return axios.get(`${API_URL}/orders/${orderId}/products`);
    }
};

export const routeService = {
    getRouteByOrderId(orderId) {
        return axios.get(`${API_URL}/routes?orderId=${orderId}`);
    }
};
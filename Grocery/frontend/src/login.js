import React, {useContext, useState} from 'react';
import {authService, setAuthToken} from './services/api';
import {useNavigate} from "react-router-dom";
import {AuthContext} from './AuthContext.js';

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');
    const navigate = useNavigate();
    const { login } = useContext(AuthContext);

    const loginRequest = {
        email,
        password
    }

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const response = await authService.login(loginRequest);
            const userData = response.data;
            login(userData);
            setAuthToken(userData.token);
            localStorage.setItem('token', userData.token);
            setMessage('Login successful!');
            console.log('Login successful:', localStorage.getItem('user'));
            setTimeout(() => {
                navigate('/productsList');
                window.location.reload();
            }, 500);
        } catch (error) {
            setMessage('Login failed. Please check your credentials.');
            console.error(error);
        }
    };

    return (
        <div>
            <h2>Login</h2>
            <form onSubmit={handleLogin}>
                <div>
                    <label>Email:</label>
                    <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
                </div>
                <div>
                    <label>Password:</label>
                    <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
                </div>
                <button type="submit">Login</button>
            </form>
            {message && <p>{message}</p>}
        </div>
    );
};

export default Login;
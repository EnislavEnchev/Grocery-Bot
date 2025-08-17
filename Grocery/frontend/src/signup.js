import React, {useContext, useState} from 'react';
import { useNavigate } from 'react-router-dom';
import {authService, setAuthToken} from './services/api';
import {AuthContext} from './AuthContext.js';
const Signup = () => {
    const [fullName, setFullName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [phoneNumber, setPhoneNumber]= useState('');
    const [address, setAddress] = useState('');
    const { login } = useContext(AuthContext);

    const [message, setMessage] = useState('');
    const [error, setError] = useState('');

    const navigate = useNavigate();

    const handleSignup = async (e) => {
        e.preventDefault();
        setError('');
        setMessage('');

        const signupRequest = {
            fullName,
            email,
            password,
            phoneNumber,
            address
        };

        try {

            const response = await authService.signup(signupRequest);
            const userData = response.data;
            login(userData);
            setAuthToken(userData.token);
            setMessage('Signup successful!');
            setTimeout(() => {
                navigate('/productsList');
                window.location.reload();
            }, 500);

        } catch (err) {
            if (err.response && err.response.data) {
                setError(err.response.data);
            } else {
                setError('Registration failed. Please try again later.');
            }
            console.error(err);
        }
    };

    return (
        <div className="auth-container">
            <h2>Create Your Account</h2>
            <form onSubmit={handleSignup}>
                <div className="form-group">
                    <label>Full Name:</label>
                    <input
                        type="text"
                        value={fullName}
                        onChange={(e) => setFullName(e.target.value)}
                        required
                    />
                </div>
                <div className="form-group">
                    <label>Email:</label>
                    <input
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                </div>
                <div className="form-group">
                    <label>Password:</label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                        minLength="8"
                    />
                </div>
                <div className="form-group">
                    <label>PhoneNumber:</label>
                    <input
                        type="phone"
                        value={phoneNumber}
                        onChange={(e) => setPhoneNumber(e.target.value)}
                    />
                </div>
                <div className="form-group">
                    <label>Address:</label>
                    <textarea
                        value={address}
                        onChange={(e) => setAddress(e.target.value)}
                        rows="3"
                    />
                </div>
                <button type="submit" className="btn-primary">Sign Up</button>
            </form>
            {message && <p className="success-message">{message}</p>}
            {error && <p className="error-message">{error}</p>}
        </div>
    );
};

export default Signup;


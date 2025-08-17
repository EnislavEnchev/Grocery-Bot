import React, { useState, useEffect } from 'react';
import {BrowserRouter as Router, Route, Routes, Link, Navigate, useLocation} from 'react-router-dom';
import ProductsList from './productsList';
import Login from './login';
import Signup from './signup';
import Orders from './Orders';
import AddOrder from './add-order';
import SearchOrder from './search-order';
import {AuthProvider} from "./AuthContext";
import ProtectedRoute from "./ProtectedRoute";
import {setAuthToken} from "./services/api";

function AppContent({ currentUser, handleLogout, handleLogin }) {
    const location = useLocation();
    return (
        <div>
            {<div>
                <nav>
                    <ul>
                        {currentUser? (
                            <>
                                {}
                                <li><span className="welcome-message">Welcome, {currentUser.fullName}</span></li>
                                {}
                                <li>
                                    <button onClick={handleLogout} className="logout-button">
                                        <Link to="/login">Logout</Link>
                                    </button>
                                </li>
                                <li>
                                    {location.pathname !== "/productsList" && (
                                        <Link to="/productsList" style={{marginRight: "1.5em"}}>Product List</Link>
                                    )}
                                    {location.pathname !== "/search-order" && (
                                        <Link to="/search-order" style={{marginRight: "1.5em"}}>Search Order</Link>
                                    )}
                                    {location.pathname !== "/add-order" && (
                                        <Link to="/add-order">Add Order</Link>
                                    )}
                                </li>
                            </>
                        ) : (
                            <>
                                <li><Link to="/login">Login</Link></li>
                                <li><Link to="/signup">Sign Up</Link></li>
                            </>
                        )}
                    </ul>
                </nav>

                <hr/>

                <Routes>
                {}
                    <Route
                        path="/"
                        element={<ProtectedRoute> <ProductsList /> </ProtectedRoute>}
                    />

                    {}
                    <Route
                        path="/orders"
                        element={<ProtectedRoute> <Orders /> </ProtectedRoute>}
                    />

                    {}
                    <Route
                        path="/login"
                        element={!currentUser? <Login onLoginSuccess={handleLogin} /> : <Navigate to="/productsList" />}
                    />

                    {}
                    <Route
                        path="/signup"
                        element={!currentUser? <Signup /> : <Navigate to="/productsList" />}
                    />

                    {}
                    <Route
                        path="/productsList"
                        element={currentUser? <ProductsList /> : <Navigate to="/login" />}
                    />

                    {}
                    <Route
                        path="/search-order"
                        element={<ProtectedRoute> <SearchOrder /> </ProtectedRoute>}
                    />

                    {}
                    <Route
                        path="/add-order"
                        element={<ProtectedRoute> <AddOrder /> </ProtectedRoute>}
                    />

                    {}
                    <Route path="*" element={<Navigate to="/" />} />
                </Routes>
            </div>}
        </div>
    );
}


function App() {
    const [currentUser, setCurrentUser] = useState(null);

    useEffect(() => {
        const user = localStorage.getItem('user');
        if (user && user !== 'undefined') {
            console.log("The user mf user: " + user);
            const parsedUser = JSON.parse(user);
            setCurrentUser(parsedUser);
            setAuthToken(parsedUser.token);
        }
    }, []);

    const handleLogin = (userData) => {
        localStorage.setItem('user', JSON.stringify(userData));
        setCurrentUser(userData);
    };

    const handleLogout = () => {
        localStorage.removeItem('user');
        localStorage.removeItem('token');
        setCurrentUser(null);
    };

    return (
        <AuthProvider>
        <Router>
            <AppContent
                currentUser={currentUser}
                handleLogout={handleLogout}
                handleLogin={handleLogin}
            />
        </Router>
        </AuthProvider>
    );
}

export default App;
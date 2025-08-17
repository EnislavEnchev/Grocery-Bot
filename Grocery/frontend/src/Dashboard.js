import React, { useEffect } from 'react';

function Dashboard({ jwtToken }) {
    useEffect(() => {
        fetch('http://your-api-url/endpoint', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + jwtToken
            }
        })
            .then(response => response.json())
            .then(data => {
                console.log(data);
            });
    }, [jwtToken]);

    return <div>Dashboard</div>;
}

export default Dashboard;
# Introduction
This is a project that simulates a warehouse with a little bot that collects orders. 
It uses the following technologies:
- Spring
- ReactJS
- Amazon Web Services
- Valkey (Redis fork)
## Notable features:
- Real-time updates of the frontend when products are being added/taken
- Security features like JWT and password encoding
- EC2 instance that periodically tries to execute failed orders
- Caching for the orders and routes through an EC2 bastion host (easier for testing)
## How to set up
To set the project up correctly, you would need:
- To run the ```sh npm install``` and ```sh npm install @stomp/stompjs sockjs-client``` commands in the frontend folder
- Create two EC2 instances, one SQS-FIFO queue, one SNS-Standard topic, one ElastiCache Valkey instance, and one RDS PostgresQL instance
- Put the https/arn links of the above in the corresponding places in the application.properties (the ones with ...)
- Configure the ports to be used for the backend (e.g. 8081) and frontend (e.g. 3000)
- Configure one of the EC2 instances and the ElastiCache instance to be in the same security group that gives inbound access to ports 22, 80, 443, and 6379, as well as to every other instance in the security group.
- Run the application without caching on the EC2 instance
## Christofides algorithm
The application uses the Christofides algorithm for finding a good path for an order. The algorithm works by finding a minimum spanning tree and a perfect matching in the graph induced by the product locations and then finds a Eulerian cycle with origin (0,0).  
A list of subsets of the possible product locations is fed to the algorithm and the best of them is chosen.  
The algorithm's time complexity depends heavily on the number of products with the same name but different locations. For a small number of these, the algorithm works in $O(n^3)$, with an average of 10% deviation from the optimal solution and 50% in the worst-case scenario. 
## Future additions
- More algorithms for the route strategy (like tabu search, a greedy method, and simulated annealing)
- Tests (unit tests, integration tests, automated UI tests)

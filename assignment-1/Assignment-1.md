# Assignment 1

### 1. **Git Repository**
https://github.com/yty20/CS6650-project

### 2. **Client Design**

#### Part 1: Multi-Thread Client
The architecture includes the following components, illustrated in the accompanying diagram:
- **`LiftRideData`**: Stores data that needs to be randomly generated, excluding `seasonID` and `dayID`.
- **`LiftRideQueue`**: A `BlockingQueue` shared between `EventGenerator` and `PostRequestThread`, with a capacity of 3200. This queue manages the flow of lift ride data between producer and consumer threads.
- **`EventGenerator`**: A single thread dedicated to generating 200,000 new `LiftRideData` instances and placing them into the `LiftRideQueue`. Upon completing the data generation, it inserts a poison pill into the queue as a termination signal for consumer threads.
- **`PostRequestThread`**: Consumes data from `LiftRideQueue` and sends POST requests until it sent 1000 requests or encounters the poison pill. It implements `Callable` to return the counts of `successfulRequests` and `failedRequests` upon completion.
- **`ClientManager`**: Manages a ThreadPool with 32 threads for processing POST requests and coordinates the execution and completion of all tasks. It collects and summarizes the results from all futures to calculate the total number of requests and displays the final metrics.
   
![alt text](multi-thread-client.png)   

Additionally, a throughput test (`testThroughput`) is conducted with 10,000 POST requests in a single thread to estimate performance based on Little's Law, with the prediction result visualized below.

**Prediction Result**:   

![alt text](<Little's Law.png>)  

#### Part 2: Multi-Thread Client 2
The enhanced design introduces additional components for performance monitoring and data management, as detailed in the following diagram:
- **`PerformanceData`**: Captures the status of each request for performance analysis.
- **`PerformanceQueue`**: A `ConcurrentLinkedQueue` that facilitates the transfer of performance data from `PostRequestThread` to `PerformanceMonitor`.
- **`PerformanceMonitor`**: Consumes data from `PerformanceQueue` to log and analyze request performance, ultimately calculating and displaying latency metrics.
- **`PostRequestThread`**: Now implements `Runnable` and utilizes `AtomicInteger` for real-time updates on request counts, enabling dynamic throughput plotting.
- **`ClientManager`**: Using `ScheduledExecutorService` to get the total requests every 30 seconds.

![alt text](package.png)   

### 3. **Client Part 1 Analysis**
**Configuration**: Employs 32 threads for sending POST requests, in addition to a single thread for generating random data. The actual throughput observed with this configuration is compared to the predicted values, indicating the practical performance under the specified workload.

![alt text](<client part 1.png>)    

**Further Configuration and Analysis**: Adjustments to the thread configuration provide additional insights into the scalability and efficiency of the multi-threaded client approach.

![alt text](<client part 1-2.png>) 

### 4. **Client Part 2 Insights**
**Configuration**: The setup includes 32 threads for POST requests, one thread for data generation, one for latency logging, and another for plotting throughput over time. This configuration highlights the system's operational efficiency and performance characteristics under a multi-threaded workload.

![alt text](<client part 2.png>)

The comparison of throughput between Part 1 and Part 2 indicates a slight performance variance, with Part 2 achieving 96.9% of Part 1's throughput. The throughput over time is further analyzed and presented in a plot, demonstrating the system's dynamic performance.

![alt text](image.png)
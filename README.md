# Instant Messenger Project
Welcome to our `Instant Messenger Project`! This project was created by Luis Jaco, Jake Rossillo, Aiden Duaz, and Daniel Felipe. This project allows many clients to connect to a
server and message each other with ease. The server also connects to a MySQL database to manage account information and store messages.
<br><br>
This program runs inside the terminal. A user can host a server using the `Server` class, allowing other users to connect with the `Client` class. The program then allows user to
sign in or sign up, access message history, and initiate chats with other active users.
## Usage
> [!NOTE]
> To set up a Server, you must also use a proper MySQL database. You can find the DDL to create the proper database [here](./instant_messager.sql).
### Database Class
The `Database` class controls all sql queries and updates. `Database.connect()` must be configured before you can run a `Server`. You can achieve this by passing in the
MySQL database **IP address**, **port**, **username**, and **password**.
```java
public void connect() {
        try {
            String databaseIp = "?"; // TODO SET YOUR DATABASE IP HERE
            String port = "?"; // TODO ENTER THE SQL PORT HERE
            String user = "?"; // TODO ENTER YOUR DB USER HERE
            String password = "?"; // TODO ENTER YOUR USER PASSWORD HERE
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + databaseIp + ":" + port + "/instant_messenger?serverTimezone=UTC&connectTimeout=5000",
                    user,
                    password
            );
            connected = true;
            System.out.println("[!] Connected to SQL database.");
            ...
```
### Server Class
The `Server` class handles all `Client` connections and creates new threads to handle communications. You must configure the `Server` before running it. To achieve this, you must input the server
**IP address** and **port** in `main()`. 
```java
public static void main(String[] args) throws IOException {
        InetAddress ip = InetAddress.getByName("?"); // TODO INPUT THE IP YOU WANT TO HOST ON HERE
        int port = 65432; // TODO ENTER THE SERVER PORT HERE
        ServerSocket serverSocket = new ServerSocket(port, 50, ip);
        Server server = new Server(serverSocket);
        server.start();
    }
```
### Client Class
The `Client` class connects to server and handles sending & reading information. In `main()`, you must configure the server **IP address**, **port**, and **client file download directory**. 
The **client file download directory** is where files will be sent when client A sends a file to client B.
```java
public static void main(String[] args) throws IOException {
        String ipAddress = ""; // todo SET THE SERVER IP HERE
        int port = 65432; // todo SET THE SERVER PORT HERE
        String downloadFileDirectory = ""; // todo SET YOUR DOWNLOAD DIRECTORY (WHERE FILES WILL DOWNLOAD).
        try {
            Socket socket = new Socket(ipAddress, port);
            Client client = new Client(socket, downloadFileDirectory);
            client.start();
        ...
```

## License
[MIT](https://choosealicense.com/licenses/mit/)
# transfer-service

To run the server

      gradlew run

To test

      gradlew test

Paths
* `GET /accounts` - Retrieves a list of accounts
* `POST /accounts`- Creates a new account
* `GET /accounts/{id}` - Retrieves a specific account
* `GET /transfers` - Retrieves a list of transfers
* `POST /transfers`- Creates a new transfer
* `GET /transfers/{id}` - Retrieves a specific transfer

To get and post data

  http://localhost:8080/accounts  
	http://localhost:8080/transfers

    curl -H "Content-Type: application/json" -X GET http://localhost:8080/accounts
    
    curl -H "Content-Type: application/json" -d "{\"balance\":1500.50}" http://localhost:8080/accounts
    
    curl -H "Content-Type: application/json" -X GET http://localhost:8080/accounts/1
    

    curl -H "Content-Type: application/json" -X GET http://localhost:8080/transfers
    
    curl -H "Content-Type: application/json" -d "{\"sourceAccountId\":1,\"destinationAccountId\":2,\"amount\":356.41}" http://localhost:8080/transfers
    
    curl -H "Content-Type: application/json" -X GET http://localhost:8080/transfers/1

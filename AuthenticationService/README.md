# Authorization Service - Spring Boot + Auth0 Implementation

# Table of Contents
* Project Overview

* Prerequisites

* Auth0 Configuration

* Local Setup

* Database Setup

* Running the Application

# Project Overview


This service provides OAuth2 token validation and fine-grained authorization using:

* Java Spring Boot 3.1

* Auth0 for JWT authentication

* SQLite for permission storage

* REST API for authorization decisions

Key features:

* JWT validation with Auth0

* Permission resolution with precedence rules

* Wildcard and nested resource support

* Caching for performance optimization


# Prerequisites 

* Java 17+ 

* Maven 3.8+

* Auth0 account (free tier)

* Postman (for testing)


# Auth0 Configuration <a name="auth0-configuration"></a>

1. Create Tenant
   Go to Auth0 and sign up

   
2. Create API In Auth0 Dashboard:

    Go to Applications → APIs

    Click Create API

    Name: Authz Service API

    Identifier: https://api.authz-service.com

    Signing Algorithm: RS256

    Click Create


3. Create Application

    Go to Applications → Applications 

    Click Create Application

    Name: Authorization Service

    Type: Machine to Machine Applications

    Select your API (Authz Service API)

    Grant all permissions

    Click Authorize


4. Configure Database Connection
    
    Go to Authentication → Database
 
    Click Create DB Connection

    Name: Username-Password-Authentication

    Enable: Password, Password Policy

    Click Create

    Link to application:

    * Go to Applications → Your Application → Connections
    * Enable Username-Password-Authentication


5. Create Test Users
   Go to User Management → Users

    Create users:

    * user123@authz.com / Password123!
    * user456@authz.com / Password456!
    * user789@authz.com / Password789!
    * admin789@authz.com / PasswordAdmin!


6. Set Default Directory

    Go to Settings → Advanced

    Under Default Directory, enter:
    * Username-Password-Authentication
    
   Click Save Changes


7. Enable Password Grant
   Go to Applications → Your Application → Settings

    Scroll to Advanced Settings → Grant Types

    Enable: 
    * Password

    * Client Credentials

    Click Save Changes

# Local Setup 

1. Clone the repository:

bash

    git clone https://github.com/Dayanithi-8148/Authentication.git

    cd authz-service

    Configure environment variables:

    Create .env file:

text

    AUTH0_ISSUER=https://your-tenant.us.auth0.com/

    AUTH0_AUDIENCE=https://api.authz-service.com

    AUTH0_USER_ID_CLAIM=https://api.authz-service.com/user_id


2. Update application.yml:

yaml

    auth0:
        issuer: ${AUTH0_ISSUER}
        audience: ${AUTH0_AUDIENCE}
        userIdClaim: ${AUTH0_USER_ID_CLAIM}


# Database Setup <a name="database-setup"></a>

1. The service uses SQLite with auto-initialization:

Schema (schema.sql)

    CREATE TABLE IF NOT EXISTS user_permissions (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id TEXT NOT NULL,
        action TEXT NOT NULL,
        resource TEXT NOT NULL,
        effect TEXT NOT NULL
    );

Data (data.sql)

    INSERT INTO user_permissions (user_id, action, resource, effect) VALUES
        ('user123', 'read', 'transactions', 'allow'),
        ('user123', 'write', 'transactions', 'allow'),
        ('user123', 'delete', 'transactions', 'deny'),
        ('user123', 'read', 'accounts', 'allow'),
        ('user456', 'read', 'wallets/*', 'allow'),
        ('user456', 'write', 'wallets/wallet-789', 'allow'),
        ('user456', 'read', 'wallets/wallet-789/transactions', 'allow'),
        ('user789', 'write', 'wallets/*/transactions/*', 'allow'),
        ('admin789', 'read', '**', 'allow'),
        ('admin789', 'write', '**', 'allow'),
        ('admin789', 'delete', '**', 'allow');

# Running the Application <a name="running-the-application"></a>


Step 1: 

* Open Project
Launch IntelliJ IDEA

* Select "Open" from the welcome screen

* Navigate to project directory

* Select the pom.xml file and click "Open"

* Choose "Open as Project" when prompted

Step 2: 

* Configure Run/Debug Settings
Go to Run → Edit Configurations

* Click  → Maven
* Configure:
  * Name: Authz Service
  * Working directory: Your project root
* Command line: 
  * spring-boot:run

* Click "Apply" → "OK"



Step 3: 

* Run the Application
Click the green "Run" button next to the configuration dropdown

* Or use shortcut: Shift+F10

* Wait for startup to complete:

text

    Tomcat started on port 8080
    Initialized database with 11 permissions

Step 4: 

* Verify Startup

        Started AuthzApplication in 5.234 seconds (process running for 6.456)


Testing with Postman

* Open Postman

* Click "Import" → "Raw text"

* Paste this collection JSON

* Set environment variables in Postman:

* auth0_domain: Your Auth0 domain

* client_id: Your Auth0 client ID

* client_secret: Your Auth0 client secret

Get Token:

* Run "Get Auth0 Token" request

* Check response for access_token

Test Authorization:

* Use different method/path combinations for test cases

    
    id      User	Method	    Path	                              Expected

    1	user123	  GET	   /transactions	                        ALLOW
    2	user123	  DELETE   /transactions/txn-456	                DENY
    3	user456	  GET	   /wallets/wallet-789/transactions	        ALLOW
    4	user789	  POST	   /wallets/wallet-456/transactions/txn-999	ALLOW
    5	user456	  POST	   /wallets/wallet-789/transactions	        DENY
    6	admin789  DELETE   /accounts/acc-123/settings	                ALLOW
    7	user456	  GET	   /wallets/wallet-999	                        ALLOW

Request:


    {
        "access_token": "eyJhbGci...",
        "method": "GET",
        "path": "/transactions"
    }

Successful Response:

    {
        "decision": "ALLOW",
        "user_id": "user123",
        "reason": "User has allow permission for transactions",
        "matched_permissions": [
            {
                "action": "read",
                "resource": "transactions",
                "effect": "allow"
            }
        ]
    }

Error Response:

    {
        "decision": "DENY",
        "user_id": "unknown",
        "reason": "Invalid token"
    }
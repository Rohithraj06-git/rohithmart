# Contributing to RohithMart

Follow these exact steps to clone, configure, build, and run RohithMart locally.

## Prerequisites
- **JDK 17 LTS** (OpenJDK / Temurin / Oracle)
- **Apache Maven 3.8+**
- **Apache Tomcat 9.0.x**

## Step-by-Step Setup Guide

### 1. Clone Repository
```bash
git clone https://github.com/your-username/rohithmart.git
cd rohithmart
```

### 2. Environment Configuration
Copy the template configuration:
```bash
cp .env.example .env
```
Ensure configuration matches your local environment. For local development, H2 embedded mode is configured by default.

### 3. Build the Application
Compile the code and execute automated unit/DAO tests:
```bash
mvn clean verify
```

### 4. Deploy and Run Locally

#### Option A: Direct Deployment to Apache Tomcat 9
1. Build the WAR artifact:
   ```bash
   mvn clean package
   ```
2. Copy `target/rohithmart.war` to your Tomcat installation's `webapps/` folder:
   ```bash
   cp target/rohithmart.war /path/to/tomcat/webapps/
   ```
3. Start Tomcat:
   - Linux/macOS: `./bin/startup.sh`
   - Windows: `bin\startup.bat`
4. Access the marketplace at: `http://localhost:8080/rohithmart`

### 5. Default Test Accounts
- **Admin**: `admin@rohithmart.com` / `Password@123`
- **Seller**: `seller@rohithmart.com` / `Password@123`
- **Buyer**: `buyer@rohithmart.com` / `Password@123`

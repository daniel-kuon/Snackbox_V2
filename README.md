# Snackbox

Snackbox is a tool for managing snack purchases within a company. It allows employees to use barcode cards to buy snacks, track their purchases, and make payments.

## Features

- **User Management**: Create, edit, and delete users
- **Barcode Management**: Assign payment and admin barcodes to users
- **Session Tracking**: Track barcode scans in sessions
- **Payment Management**: Record and track payments
- **Localization**: Fully localized UI (English and German)
- **QR Code Generation**: Generate PayPal.me QR codes for payments

## Technology Stack

- **Kotlin 2.1.0**: Modern, concise programming language
- **Compose Desktop 1.7.3**: UI framework for desktop applications
- **Exposed**: SQL framework for database operations
- **PostgreSQL**: Robust, open-source relational database
- **pgAdmin 4**: Web-based PostgreSQL administration tool
- **Hoplite**: Configuration library for YAML parsing
- **ZXing**: Library for QR code generation
- **KotlinLogging**: Logging library for Kotlin

## Project Structure

- **Models**: Data classes representing the domain entities (User, Barcode, Session, Payment)
- **Repositories**: Classes for database operations
- **Services**: Business logic for the application
- **UI**: Compose Desktop UI components
- **Localization**: Internationalization support
- **Configuration**: Application configuration

## Setup

### Prerequisites

- JDK 17 or higher
- Docker and Docker Compose (optional, for containerized deployment)

### Local Setup

1. Clone the repository:
   ```
   git clone https://github.com/your-username/snackbox.git
   cd snackbox
   ```

2. Build the project:
   ```
   ./gradlew build
   ```

3. Run the application:
   ```
   ./gradlew run
   ```

### Docker Setup

1. Clone the repository:
   ```
   git clone https://github.com/your-username/snackbox.git
   cd snackbox
   ```

2. Build and run with Docker Compose:
   ```
   docker-compose up -d
   ```

3. Access the application at http://localhost:3000

4. Access pgAdmin at http://localhost:5050
   - Email: admin@example.com
   - Password: admin

5. Connect to PostgreSQL in pgAdmin:
   - Host: postgres
   - Port: 5432
   - Database: snackbox
   - Username: snackbox
   - Password: snackbox_password

## Database Setup

The application uses PostgreSQL as its database. When running with Docker Compose, the following services are set up:

### PostgreSQL

- A PostgreSQL database server is automatically set up with the following configuration:
  - Database: snackbox
  - Username: snackbox
  - Password: snackbox_password
  - Port: 5432 (mapped to host)
- Data is persisted in a Docker volume named `postgres-data`

### pgAdmin 4

- A web-based PostgreSQL administration tool is available at http://localhost:5050
- Login credentials:
  - Email: admin@example.com
  - Password: admin
- To connect to the PostgreSQL database:
  1. Click "Add New Server"
  2. In the "General" tab, enter a name (e.g., "Snackbox")
  3. In the "Connection" tab, enter:
     - Host: postgres
     - Port: 5432
     - Database: snackbox
     - Username: snackbox
     - Password: snackbox_password
  4. Click "Save"

## Configuration

The application can be configured using the `application.yaml` file. You can customize:

- Database settings
- Session timeout
- PayPal.me address
- UI settings
- Default language

Example configuration:

```yaml
# Database settings
database:
  url: "jdbc:postgresql://postgres:5432/snackbox"
  driver: "org.postgresql.Driver"
  username: "snackbox"
  password: "snackbox_password"

# Application settings
application:
  sessionTimeoutMinutes: 2
  paypalMeAddress: "paypal.me/yourcompany"
  currencySymbol: "€"

# UI settings
ui:
  title: "Snackbox"
  width: 1024
  height: 768
  defaultLanguage: "en"
```

## Usage

### Normal Users

Normal users interact with the application using a barcode scanner. When a user scans their barcode:

1. A new session is created or the existing session is updated
2. The UI displays:
   - Amount added in the current session
   - Total open amount
   - List of recent sessions
   - List of recent payments
   - QR code for PayPal payment

### Admin Users

Admin users can access additional functionality by scanning an admin barcode:

1. User Management: Add, edit, or delete users
2. Barcode Management: Assign barcodes to users
3. Payment Management: Record payments for users

## License

This project is licensed under the MIT License - see the LICENSE file for details.

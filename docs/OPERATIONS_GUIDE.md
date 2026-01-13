# Operations Guide: Build & Deployment

This guide provides the necessary commands to build and deploy the Spring Petclinic application across different environments.

## 🛠 Build Commands

Use these commands to package the application for a specific environment. This ensures that the corresponding `application-{profile}.properties` file is used for configuration and testing.

| Environment | Maven Build Command |
| :--- | :--- |
| **Development** | `mvn clean install "-Dspring.profiles.active=dev"` |
| **SIT** | `mvn clean install "-Dspring.profiles.active=sit"` |
| **UAT** | `mvn clean install "-Dspring.profiles.active=uat"` |
| **Production** | `mvn clean install "-Dspring.profiles.active=prod"` |

---

## 🚀 Deployment Commands (WildFly)

The application uses the `wildfly-maven-plugin` for deployment. You can deploy it to a running WildFly instance using the following commands.

### **1. Direct Deployment via Maven**
To deploy to a WildFly server that is already running (e.g., locally or accessible via management port):

| Environment | Deployment Command |
| :--- | :--- |
| **Development** | `mvn wildfly:deploy "-Dspring.profiles.active=dev"` |
| **SIT** | `mvn wildfly:deploy "-Dspring.profiles.active=sit"` |
| **UAT** | `mvn wildfly:deploy "-Dspring.profiles.active=uat"` |
| **Production** | `mvn wildfly:deploy "-Dspring.profiles.active=prod"` |

### **2. Manual Deployment (WAR Copy)**
If you prefer to move the artifact manually to the WildFly deployment folder:

1.  **Build the WAR**:
    `mvn clean install "-Dspring.profiles.active=prod"`
2.  **Copy to WildFly**:
    `cp target/petclinic.war $WILDFLY_HOME/standalone/deployments/`

---

## ⚙️ Environment Configuration Summary

| Profile | Target Database | Log Level | DDL Mode |
| :--- | :--- | :--- | :--- |
| `dev` | `petclinic` (local) | DEBUG | update |
| `sit` | `petclinic_sit` (server) | DEBUG | validate |
| `uat` | `petclinic_uat` (server) | INFO | validate |
| `prod` | `petclinic_prod` (server) | WARN | validate |

### **Important Notes**
*   **Java Version**: This project requires **Java 21**. Ensure your `JAVA_HOME` points to JDK 21 before running these commands.
*   **Active Profile**: If no profile is specified, the application defaults to `dev` as configured in `application.properties`.

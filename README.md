# Open Team One Server

![Logo](src/main/resources/static/admin/logo128.png)

Open Team One Server is a backend component to support the communication function of SAP Team One mobile apps. At this point in time the software is ready to be used productively, although some areas are still work in progress.

## Scope

Open Team One Server aimes to provide a simple and functional solution of high performance for those needing a communication server under their own control - in terms of source code and operations. Instead of re-inventing the wheel we use freely available mobile clients, namely the SAP Team One mobile apps that are available for multiple platforms.

The SAP Team One apps are normally operated against a SAP Sports One backend that integrates communication with multiple sports applications. Typical examples are Match Insights videos, questionnaires to capture user feedback and performance KPIs, and information packages combining media and editorials. All those integration scenarios are missing from Open Team One for the simple reason that Open Team One is a best-of-breed communication hub for mobile users, not a sports solution.

One function that cannot be provided for legal reasons are push notifications. The delivery of push notifications would require to reverse-engineer a secret API key.

## Current Limitations

At the time of writing there are limitations in two areas. One is the type of database provided which is an in-memory H2 database. Due to the JPA software interface, it should not be difficult to connect a disk-based SQL database like MySQL or similar. In the meantime there is an export/import function that can be used to persist snapshots.

The other limited area is the web application used by the administrator. Functionally the web app needs to offer maintenance modules for configuration data and master data. At the moment those can only be maintained through the export/import interface. In addition to functional enhancements, the web app needs a good web design and a modern implementation resulting in a pleasant best-of-breed user experience.

In fact, Open Team One has a configuration switch allowing the admin to switch between separate web applications, should there ever be more than one.

## Quick Guide (How to run the server with demo content)

A running server instance consists of two data sources: the integrated relational database for structured objects, and the file system for potentially large images and file attachments. The structured objects in the database are sometimes referred to as "business objects". They can be exported and imported in a serialized JSON representation (but internally they strictly live as strongly typed Java objects). The entirety of the database can be exported and imported in the form of large JSON files, which can also be stored in the file system of the server.

Therefore, in order to bootstrap a demo server we need to provide a filesystem directory with content, including a JSON serialization of the database content. We then import the JSON file into the database, and voilà, the demo instance is ready.

### Step 1: Preparation

In step 1 you need to designate a data directory for the server, or rather, for the tenant that this server instance represents. In this example the directory chosen is /var/cache/openTeamOne but you are free to choose any other directory.

You copy the complete content of the project directory "demo" into the designated data directory. At the time of writing that would be the three subdirectories "attachments", "profiles" and "snapshots". The result should look like this:

![Preparation](docu/demo1.png)

### Step 2: Login

Next step is to start the server and log in to the admin section. Since this project is distributed as source code, you would import it as Java project into a suitable IDE like IntelliJ IDEA or Eclipse. When asked for project types you would probably choose "Maven" since all dependencies are declared in a pom.xml file.

When the server is running you call up the admin page in a web browser. Assuming your server listens on TCP port 8080 (depends on your run configuration), you would enter the URL http://localhost:8080 in your browser.

The default login is user "admin" with password "admin".

![Admin Login](docu/demo2.png)

### Step 3: Configuration

The server is smart enough to configure missing information when it starts up. There are not many configuration settings to begin with, and every setting can be changed by the administrator. However, there is one setting the server cannot guess: the data directory you have chosen in step 1.

So before you continue with anything, you need to make this directory known to the server. This is done by providing the tenant parameter "dataDirectory" and hitting the "Set" button, as shown in the following picture:

![Configuration](docu/demo3.png)

### Step 4: Import

In the last step you import the database content of the demo instance from the JSON file "demo". If you followed the previous steps, the server will find it when you hit the "Load" button as in the following picture:

![Import](docu/demo4.png)

Congratulations! The demo instance is now operational.

### Step 5: Profit

The demo instance contains 3 users that you can log in as from your mobile devices. The user names are player01, player02 and player03. All 3 of them have the case-sensitive password "pass".

![App Login](docu/screenshot5.png)

Familiarize yourself with the system by creating transactional content through mobile devices, saving database snapshots to the server and studyding the JSON objects inside those snapshots. 

At this point in time the admin functionality is limited to the bare minimum, which means that master data creation and maintenance (users, persons, rooms, room memberships) can only be done through JSON snapshots that are uploaded. Note that a JSON file can be partial. For instance, if a JSON file contains only users and persons, it will not affect other object types in the database.

## Screenshots of Demo Content

Here are a few screenshots of SAP Team One connected against Open Team One Server, displaying the default demo content. The screenshots show the drawer, the landing page, the content of a room, the room details.

![Drawer](docu/screenshot1.png)
![Landing page](docu/screenshot2.png)
![Room content](docu/screenshot3.png)
![Room details](docu/screenshot4.png)

## Persistence via MariaDB

MariaDB is a popular MySQL fork, so everything written in this section works for MySQL, too.

For a first trial it is quite convenient to use the H2 in-memory database as it comes with Spring Boot and requires no configuration. It is also quite easy to switch to a disk based SQL database, like MariaDB. To do so, you first need to prepare an empty database for a standard user. Having started the database daemon, you simply enter the following three commands in any SQL console connected to the database, for instance the _mysql_ binary:

	mysql --password
	> create database teamone;
	> create user 'springuser'@'localhost' identified by 'dbPassword';
	> grant all on teamone.* to 'springuser'@'localhost';
	> quit

In this example the database is called _teamone_ and contains a user _springuser_ with password _dbPassword_. The database name, the user name and the password can be freely chosen. In a productive environment, once the database schema has been created, you are advised to reduce the granted authorizations to select, insert, update and delete.

Next you need to add the JDBC database driver to the project. We have already added the following dependency to our Maven file pom.xml:

	<dependency>
		<groupId>mysql</groupId>
		<artifactId>mysql-connector-java</artifactId>
	</dependency>

So, for MariaDB or MySQL you don't need to do anything. If you use a different SQL database, you'll have to add the corresponding driver.

Finally we add the following properties to the application.properties file. In fact, you just need to uncomment them and insert the correct names and passwords, and maybe the TCP port of the database server.

	spring.jpa.hibernate.ddl-auto=update
	spring.datasource.driver-class-name=com.mysql.jdbc.Driver
	spring.datasource.url=jdbc:mysql://localhost:3306/teamone
	spring.datasource.username=springuser
	spring.datasource.password=dbPassword

Restart Open Team One Server and you're done. If you want to migrate the content over to the new DB, save and import a snapshot via the admin tools. 

Note: we ran into one issue when running the server against MariaDB, caused by a Java property that coincided with a protected SQL identifier. We solved the issue by re-naming the property.


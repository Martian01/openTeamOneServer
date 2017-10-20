# Open Team One Server

![Logo](src/main/resources/static/admin/logo128.png)

Open Team One Server is a backend component to support the communication function of SAP Team One mobile apps.

At this point in time the software is ready to be used productively, although some areas are still work in progress. More information is found below.

## Scope

tbd

## Current Limitations

tbd

## Quick Guide to Demo

A running server instance consists of two data sources: the integrated relational database for structured objects, and the file system for potentially large images and file attachments. The structured objects in the database are sometimes referred to as "business objects". They can be exported and imported in a serialized JSON representation (but internally they strictly live as strongly typed Java objects). The entirety of the database can be exported and imported in the form of large JSON files, which can also be stored in the file system of the server.

Therefore, in order to bootstrap a demo server we need to provide a filesystem directory with content, including a JSON serialization of the database content. We then import the JSON file into the database, and voilà, the demo instance is ready.

### Step 1: Preparation

In step 1 you need to designate a data directory for the server, or rather, for the tenant that this server instance represents. In this example the directory chosen is /var/cache/openTeamOne but you are free to chose any other directory.

You copy the demo content from the project directory "demo" into the designated data directory. The result should look like this:

![Preparation](docu/demo1.png)

### Step 2: Logon

Next step is to start the server and logon to the admin section. Since this project is distributed as source code, you would import it as Java project into a suitable IDE like IntelliJ IDEA or Eclipse. When asked for project types you would probably choose "Maven" since all dependencies are declared in a pom.xml file.

When the server is running you call up the admin page in a web browser. Assuming your server listens on TCP port 8080 (depends on your run configuration), you would enter the URL http://localhost:8080 in your browser.

The default logon is user "admin" with password "admin".

![Logon](docu/demo2.png)

### Step 3: Configuration

The server is smart enough to configure itself when it is started the first time. There are not many configuration seetings to begin with, and every setting can be changed by the administrator. However, there is one setting the server cannot guess: the data directory you have chosen in step 1.

So before you can continue with anything, you need to make this directory known to the server. This is done by providing the tenant parameter "dataDirectory" as shown in the following picture:

![Configuration](docu/demo3.png)

### Step 4: Import

As a last step you import the database content of the demo instance from the JSON file "demo". If you followed the previous steps, the server will find it when you hit the "Load" button as in the following picture:

![Import](docu/demo4.png)

Congratulations. The demo instance is now operational.

### Step 5: Profit

The demo instance contains 3 users that you can log in as from your mobile devices. The user names are player01, player02 and player03. All 3 of them have the password "pass".

Familiarize yourself with the system by creating transactional content through mobile devices, saving database snapshots to the server and studyding the JSON objects inside those snapshots. 

At this point in time the admin functionality is limited to the bare minimum, which means that master data creation and maintenance (users, persons, rooms, room memberships) can only be done through JSON snapshots that are uploaded. Note that a JSON file can be partial. For instance, if a JSON file contains only users and persons, it will not affect other object types in the database.

## Screenshots of Demo Content

Here are a few screenshots of SAP Team One connected against Open Team One Server, displaying the default demo content. The screenshots show the menu drawer, the landing page, the content of a room plus the room details.

![Drawer](docu/screenshot1.png)
![Landing page](docu/screenshot2.png)
![Room content](docu/screenshot3.png)
![Room details](docu/screenshot4.png)


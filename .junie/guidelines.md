# Project Guidelines

Technical Specs:
* Write tests for everything you do. These tests should cover all relevant scenarios
* Document the code
* Create a readme that describes what the project is about, key aspects of the code, used libraryies and describes how to set up the project locally and let it run
* Build a docker configuration for the codes so that it can be started with one command. Make sure that docker persists all data so it doesn't get lost when restarted
* The UI is using Compose Desktop, makes sure we use version 1.7.3
* Use Kotlin 2.1.0
* The UI must be fully localizable
* Any data shoudl be stored in a database
* The operating system is windows
* Use a config file
* Check if the app is building

Hardware constraint that the project will have:
* A computer display is used to show the ui
* Normal users only input is a barcode scanner. The scanner works as a keyboard and will send a number of digits followed by a return at the end. The whole ui for these users should require no input other then the code from the scanner
* Admin users can also use keyboard and mouse, so the admin ui can be more complex

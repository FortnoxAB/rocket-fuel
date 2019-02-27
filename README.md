# Rocket Fuel ( still under development )

[![Build Status](https://travis-ci.com/FortnoxAB/rocket-fuel.svg?branch=master)](https://travis-ci.com/FortnoxAB/rocket-fuel)
[![codecov](https://codecov.io/gh/FortnoxAB/rocket-fuel/branch/master/graph/badge.svg)](https://codecov.io/gh/FortnoxAB/rocket-fuel)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=se.fortnox:rocket-fuel&metric=alert_status)](https://sonarcloud.io/dashboard?id=se.fortnox:rocket-fuel)

Rocket fuel is a question/answer platform that you deploy with docker wherever you want. Its main goal is to spread knowledge.

This backend is built with Reactive wizard, see https://github.com/FortnoxAB/reactive-wizard. Reactive Wizard project makes it easy to build performant and scalable web applications that harness the power of RxNetty (i.e., RxJava and Netty). The frontend is built with react and talks with the backend though a REST api. 

The application's main goal is to share knowledge inside an organisation. One may find a lot of information on google, stack overflow etc. But typically an organisation has a lot of internal knowledge, that everyone needs to learn. Historically a lot of tools have been used to aid this problem (Confluence etc.). The problem is that this documentation has typically been hard to find and hard to maintain. Rocket fuel solves this problem by integrating with Slack, a tool used by many organisations. With the help of the slack integration, questions and answers can be added to rocket fuel by integrating it to the workflow people are used to. 

After some time, a lot of questions and answers will be added to rocket fuel. You can search for questions in rocket fuel as well as answer questions there. To filter out all the noise, a rating system is used for questions and answers, so that the most relevant questions and answers will be shown to the user.

## This is how it works. 

### The slack way

A thread is started in slack, the rocket fuel bot detects that a question has been asked and the slack rocket fuel bot asks the members of the thread if they want to save the question and the answers in rocket fuel, so that anyone in the organisation easily can find the question and the answers later on, instead of asking the question again. 

### The normal way
A user can add a question to rocket fuel directly in the ui, and then wait for anyone to answer it, this is very similar to how it works on stack overflow. 


### Contribution

Feel free to contribute to rocket fuel. Create a issue, when the issue is accepted, create a fork and create a pull request. 

#### Running in intellij

Import the project as a maven project. Intellij will probably fix this for you. To be able to run the application from intellij, you need to go to build , Preferences -> execution, deployment > compiler  > java compiler and type "-parameters" in the text box labeled "additional command line parameters". Then rebuild the project. Now you can add a run configuration. Add a application configuration. In the text field labeled "Main class" type "se.fortnox.reactivewizard.Main". In the text field labeled "Program arguments", type "db-migrate config.yml". The working directory should point to the impl module ( use the browse functionality).    

#### Compiling with maven
run maven clean package in the root folder. It will execute all the tests and generate a fat jar for you, that you can execute with java -jar.

The tests inside the spec module requires docker and at least two gigabytes of free space. The reason is that a postgres container is used to test the sql queries.

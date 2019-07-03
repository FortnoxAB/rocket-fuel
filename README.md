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
A user can add a question to rocket fuel directly in the ui, and then wait for anyone to answer it, this is very similar to how it works on stack overflow. When a answer has been submitted, the user will be notified through slack. The user will be given a link to the question. Then its up to the user to accept the answer or write back. 


### Contribution

Feel free to contribute to rocket fuel. Create a issue, when the issue is accepted, create a fork and create a pull request. 

#### Prerequisites

To be able to run and develop rocket fuel, you will need to have the following installed on your machine.

* Java 11
* Maven 3.04 or higher
* Node (verified with v6.9.5, but others should work )
* Npm ( verified with 3.10.10, but others should work )
* Docker ( verified with 18.09.1 )
* Linux or Mac environment ( windows might work, but we dont know )
* Patience and stubbornness ( verified )
* Beard is not mandatory, but recommended.

Rocket fuel interacts with google openId and slack. Knowledge how to setup openId and how slack works will save you some time.


Rocket fuel depends on a configuration file, that needs to be correctly configured, or you will only be able to run unit tests, but not the application it self. If you want to run the application locally, you should start with the instructions in `impl/config.example.yml`. Then you can continure reading ***Running rocket fuel in developer mode in intellij*** or ***Compiling with maven***. The ui is a story on its own, and has its own chapter further down "running the ui".
 

#### Running rocket fuel in developer mode in intellij

Import the project as a maven project. Intellij will probably fix this for you. To be able to run the application from intellij, you need to go to build , `Preferences -> execution, deployment > compiler  > java compiler` and type `-parameters` in the text box labeled "additional command line parameters". Then rebuild the project. Now you can add a run configuration. Add a application configuration. In the text field labeled "Main class" type `se.fortnox.reactivewizard.Main`. In the text field labeled "Program arguments", type `db-migrate config.yml`. The working directory should point to the impl module ( use the browse functionality). Look in the `dev.example.yml` file for details regarding `config.yml`. When started you will have a working rocket fuel backend. Its now time to startup the frontend. Continue with ***Running the ui in developer mode***.

#### Compiling with maven
Run `mvn clean package` in the root folder. It will execute all the tests and generate a fat jar for you, that you can execute with `java -jar`.

The tests inside the spec module requires docker and at least two gigabytes of free space. The reason is that a postgres container is used to test the sql queries. When maven successfully has compiled and packaged the backend, you  can run the backend with the fat jar, located in the `impl/target` folder. Now it's a good time to start the frontend. Continue with ***Running the ui in developer mode***.

#### Running the ui in developer mode

Okay, so you have a running backend, congratulations! The frontend is a react app and will be served with help of webpack. First you need to run ´npm install´ in the ui folder. Now you need to supply the ui with the openid clientid from google. Open `config.js` in the ui folder and replace `window.googleClientId = '{{ .Env.OPENID_CLIENT_ID }}';` with the real openid. Now, you stay in the ui folder and type 'npm run dev'. Webpack will start a dev server that will run the ui and proxy api request to the rocket fuel backend. That's all. You are ready to start coding.

#### Release

When a feature or bug has been merged, a release can be made. The release will be executed with the maven release plugin. The release can only be performed by the maintainers of the repository. Only maintainers will have the credentials. 

To perform a release, first make sure you are on a updated master branch without any local commits. The branch should be identical to the remote master. Make sure you have ssh keys in place for git. Only ssh is supported currently. You need to make sure you have access to dockerhub. You will need to provide credentials in your settings.xml file for this. You can read on fabric8 documentation for the maven docker plugin about how to provide credentials.

Then in the root of the project type the following to prepare for a release:

```mvn release:prepare``` 

Now it's time to do a real release.

```mvn release:perform```

A complete release has now been performed. You can now make the release official at github.

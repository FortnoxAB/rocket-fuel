# Rocket Fuel ( still under development )

[![Build Status](https://travis-ci.com/FortnoxAB/rocket-fuel.svg?branch=master)](https://travis-ci.com/FortnoxAB/rocket-fuel)
[![codecov](https://codecov.io/gh/FortnoxAB/rocket-fuel/branch/master/graph/badge.svg)](https://codecov.io/gh/FortnoxAB/rocket-fuel)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=se.fortnox:rocket-fuel&metric=alert_status)](https://sonarcloud.io/dashboard?id=se.fortnox:rocket-fuel)

Rocket fuel is a question/answer platform that you deploy with docker wherever you want. Its main goal is to spread knowledge.

This backend is built with [Reactive wizard](https://github.com/FortnoxAB/reactive-wizard). Reactive Wizard projects makes it easy to build performant and scalable web applications that harness the power of RxNetty (i.e., RxJava and Netty). The frontend is built with React and talks with the backend though a REST api. 

The application's main goal is to share knowledge inside an organisation. One may find a lot of information on Google, Stack Overflow etc. Typically an organisation has a lot of internal knowledge that everyone needs to learn where historically a lot of tools have been used to aid this problem (Confluence etc.). The problem is that this documentation has typically been hard to find and maintain. Rocket fuel solves this problem by integrating with Slack, a tool used by many organisations. With the help of the Slack integration, questions and answers can be added to Rocket fuel by integrating it to the workflow people are used to. 

After some time, a lot of questions and answers will be added to Rocket fuel. You can search for questions in Rocket fuel as well as answer questions there. To filter out all the noise, a rating system is used for questions and answers, so that the most relevant questions and answers will be shown to the user.

## This is how it works. 

### The Slack way

As a thread is started in Slack, the Rocket fuel bot detects that a question has been asked and the bot asks the members of the thread if they want to save the question and the answers in Rocket fuel, so that anyone in the organisation easily can find the question and the answers later on, instead of asking the question again. 

### The normal way
A user can add a question to Rocket fuel directly in the UI, and then wait for anyone to answer it, this is very similar to how it works on Stack overflow. When an answer has been submitted, the user will be notified through Slack. The user will be given a link to the question. Then its up to the user to accept the answer or write back. 


### Contribution

Feel free to contribute to Rocket fuel by initially creating issues on Github. When the issue has been accepted then submit your changes in a pull request from a forked repository. 

#### Prerequisites

To be able to run and develop Rocket fuel, you will need to have the following installed on your machine.

* Java 11
* Maven 3.04 or higher
* Node (verified with v6.9.5, but others should work )
* Npm ( verified with 3.10.10, but others should work )
* Docker ( verified with 18.09.1 )
* Linux or Mac environment ( windows might work, but we dont know )
* Patience and stubbornness ( verified )
* Beard is not mandatory, but recommended.

Rocket fuel interacts with Google OpenID and Slack. Knowledge how to setup OpenID and how Slack works will save you some time.

Rocket fuel depends on a configuration file that needs to be correctly configured or you will only be able to run unit tests but not the application in it self. If you want to run the application locally you should start with the instructions in `impl/config.example.yml`. You can then continue reading ***Running Rocket fuel in developer mode in IntelliJ*** or ***Compiling with Maven***. The UI is a story on its own, and has its own chapter further down "running the UI".
 

#### Running Rocket fuel in developer mode in IntelliJ

Import the project as a Maven project. IntelliJ will probably fix this for you. To be able to run the application from IntelliJ, you need to go to build , `Preferences -> execution, deployment > compiler  > java compiler` and type `-parameters` in the text box labeled "additional command line parameters". Then rebuild the project. Now you can add a run configuration. Add a application configuration. In the text field labeled "Main class" type `se.fortnox.reactivewizard.Main`. In the text field labeled "Program arguments", type `db-migrate config.yml`. The working directory should point to the impl module ( use the browse functionality). Look in the `dev.example.yml` file for details regarding `config.yml`. When started you will have a working Rocket fuel backend. Its now time to startup the frontend. Continue with ***Running the UI in developer mode***.

#### Compiling with maven
Run `mvn clean package` in the root folder. It will execute all the tests and generate a fat jar for you, that you can execute with `java -jar`.

The tests inside the spec module requires Docker and at least two gigabytes of free space. The reason is that a postgres container is used to test the SQL queries. When Maven successfully has compiled and packaged the backend, you  can run the backend with the fat jar, located in the `impl/target` folder. Now it's a good time to start the frontend. Continue with ***Running the UI in developer mode***.

#### Running the UI in developer mode

Okay, so you have a running backend, congratulations! The frontend is an React app and will be served with the help of Webpack. First you need to run `npm install` in the `ui` folder. Now you need to supply the UI with the OpenID client id from Google. Open `config.js` in the `ui` folder and replace `window.googleClientId = '{{ .Env.OPENID_CLIENT_ID }}';` with the real OpenID. Now, you stay in the `ui` folder and type `npm run dev`. Webpack will start a dev server that will run the UI and proxy API request to the Rocket fuel backend. That's all. You are ready to start coding.

#### Release

When a feature or bug has been merged, a release can be made. The release will be executed with the Maven release plugin. The release can only be performed by the maintainers of the repository. Only maintainers will have the credentials. 

To perform a release, first make sure you are on a updated master branch without any local commits. The branch should be identical to the remote master. Make sure you have SSH keys in place for Git. Only SSH is supported currently. You need to make sure you have access to dockerhub. You will need to provide credentials in your settings.xml file for this. You can read on fabric8 documentation for the Maven docker plugin about how to provide credentials.

Then in the root of the project type the following to prepare for a release:

```mvn release:prepare``` 

Now it's time to do a real release.

```mvn release:perform```

A complete release has now been performed. You can now make the release official at Github.

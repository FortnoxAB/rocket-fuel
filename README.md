# Rocket Fuel ( still under development )
Question/answer platform to share knowledge

This backend is built with Reactive wizard, a open source platform for building efficent fast applications in Java. The frontend is built with react and talks with the backend though a REST api. 

The application's main goal is to share knowledge inside an organisation. One may find a lot of information on google, stack overflow etc. But typically an organisation has a lot of internal knowledge, that everyone needs to learn. Historically a lot of tools have been used to aid this problem (Confluence etc.). The problem is that this documentation has typically been hard to find and hard to maintain. Rocket fuel solves this problem by integrating with Slack, a tool used by many organisations. With the help of the slack integration, questions and answers can be added to rocket fuel by integrating it to the workflow people are used to. 

After some time, a lot of questions and answers will be added to rocket fuel. You can search for questions in rocket fuel as well as answer questions there. To filter out all the noise, a rating system is used for questions and answers, so that the most relevant questions and answers will be shown to the user.

## This is how it works. 

### The slack way

A thread is started in slack, the rocket fuel bot detects that a question has been asked and the slack rocket fuel bot asks the members of the thread if they want to save the question and the answers in rocket fuel, so that anyone in the organisation easily can find the question and the answers later on, instead of asking the question again. 

### The normal way
A user can add a question to rocket fuel directly in the ui, and then wait for anyone to answer it, this is very similar to how it works on stack overflow. 



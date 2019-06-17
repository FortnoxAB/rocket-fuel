import React from 'react';
import { t } from 'ttag';
import Loader from '../components/utils/loader';
import QuestionCard from '../components/questions/questioncard';
import { UserContext } from '../usercontext';
import * as Question from '../models/question';

class HomeView extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            userQuestions: [],
            latestQuestions: [],
            loaded: false
        };
    }

    componentDidMount() {
        this.fetchQuestions();
    }

    fetchQuestions() {
        const user = this.context.state.user;
        const userQuestions = Question.getQuestionsFromUser(user.id);
        const latestQuestions = Question.getLatestQuestion();

        Promise.all([userQuestions, latestQuestions]).then((response) => {
            console.log(latestQuestions);
            this.setState({
                userQuestions: response[0],
                latestQuestions: response[1],
                loaded: true
            });
        }).catch(() => {
            this.setState({
                loaded: true
            });
        });
        /*
        Question.getQuestionsFromUser(user.id).then((questions) => {
            this.setState({
                questions: questions,
                loaded: true
            });
        });

        Question.getLatestQuestion().then((questions) => {
            console.log(questions);
        });
         */
    }

    getUserQuestions() {
        return this.state.userQuestions.map((question, index) => {
            return <QuestionCard small key={index} question={question} />;
        });
    }

    getLatestQuestions() {
        return this.state.latestQuestions.map((question, index) => {
            return <QuestionCard small key={index} question={question} />;
        });
    }

    getDisplayName() {
        return this.context.state.user.name;
    }

    render() {
        if (!this.state.loaded) {
            return (
                <Loader fillPage />
            );
        }
        return (
            <div>
                <div className="user-space">
                    <h1>{this.getDisplayName()}</h1>
                </div>
                <div className="flex list-row">
                    <div className="flex-basis-2">
                        <h3>{t`Latest questions`}</h3>
                        {this.getLatestQuestions()}
                    </div>
                    <div className="flex-basis-2">
                        <h3>{t`Your recent questions`}</h3>
                        {this.getUserQuestions()}
                    </div>
                </div>
            </div>
        )

    }
}

HomeView.contextType = UserContext;

export default HomeView;

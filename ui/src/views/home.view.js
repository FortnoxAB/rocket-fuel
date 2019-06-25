import React from 'react';
import { t } from 'ttag';
import Loader from '../components/utils/loader';
import QuestionRow from '../components/questions/questionrow';
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
    }

    getUserQuestions() {
        return this.state.userQuestions.map((question, index) => {
            return <QuestionRow small key={index} question={question} />;
        });
    }

    getLatestQuestions() {
        return this.state.latestQuestions.map((question, index) => {
            return <QuestionRow small key={index} question={question} />;
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
                <div className="user-space padded-bottom">
                    <img src={this.context.state.user.picture} alt={this.getDisplayName()} />
                    <h2>{this.getDisplayName()}</h2>
                </div>
                <div className="row spacing">
                    <div className="col-2">
                        <div className="headline">{t`Latest questions`}</div>
                        {this.getLatestQuestions()}
                    </div>
                    <div className="col-2">
                        <div className="headline">{t`Your recent questions`}</div>
                        {this.getUserQuestions()}
                    </div>
                </div>
            </div>
        )

    }
}

HomeView.contextType = UserContext;

export default HomeView;

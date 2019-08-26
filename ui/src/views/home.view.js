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
            popularQuestions: [],
            popularUnansweredQuestions: [],
            recentlyAcceptedQuestions: [],
            loaded: false
        };
    }

    componentDidMount() {
        this.fetchQuestions();
    }

    fetchQuestions() {
        const user = this.context.state.user;
        const userQuestions              = Question.getQuestionsFromUser(user.id,5);
        const latestQuestions            = Question.getLatestQuestions(5);
        const popularQuestions           = Question.getPopularQuestions(5);
        const popularUnansweredQuestions = Question.getPopularUnansweredQuestions(5);
        const recentlyAcceptedQuestions  = Question.getRecentlyAcceptedQuestions(5);

        Promise.all([userQuestions, latestQuestions, popularQuestions, popularUnansweredQuestions, recentlyAcceptedQuestions]).then((response) => {
            this.setState({
                userQuestions: response[0],
                latestQuestions: response[1],
                popularQuestions: response[2],
                popularUnansweredQuestions: response[3],
                recentlyAcceptedQuestions: response[4],
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
            return <QuestionRow small key={index} question={question} onDeleteQuestion={this.fetchQuestions.bind(this)} />;
        });
    }

    getLatestQuestions() {
        return this.state.latestQuestions.map((question, index) => {
            return <QuestionRow small key={index} question={question} onDeleteQuestion={this.fetchQuestions.bind(this)} />;
        });
    }

    getPopularQuestions() {
        return this.state.popularQuestions.map((question, index) => {
            return <QuestionRow small key={index} question={question} onDeleteQuestion={this.fetchQuestions.bind(this)} />;
        });
    }

    getPopularUnansweredQuestions() {
        return this.state.popularUnansweredQuestions.map((question, index) => {
            return <QuestionRow small key={index} question={question} onDeleteQuestion={this.fetchQuestions.bind(this)} />;
        });
    }

    getRecentlyAcceptedQuestions() {
        return this.state.recentlyAcceptedQuestions.map((question, index) => {
            return <QuestionRow small key={index} question={question} onDeleteQuestion={this.fetchQuestions.bind(this)} />;
        });
    }

    render() {
        if (!this.state.loaded) {
            return (
                <Loader fillPage />
            );
        }
        return (
            <div>
                <div className="row flex-grow spacing">
                    <div className="col-2">
                        <div className="headline">{t`Latest questions`}</div>
                        {this.getLatestQuestions()}
                    </div>
                    <div className="col-2">
                        <div className="headline">{t`Popular questions`}</div>
                        {this.getPopularQuestions()}
                    </div>
                </div>
                <div className="row flex-grow spacing">
                    <div className="col-2">
                        <div className="headline">{t`Popular unanswered questions`}</div>
                        {this.getPopularUnansweredQuestions()}
                    </div>
                    <div className="col-2">
                        <div className="headline">{t`Recently accepted questions`}</div>
                        {this.getRecentlyAcceptedQuestions()}
                    </div>
                </div>
                <div className="row flex-grow spacing">
                    <div className="col-1">
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

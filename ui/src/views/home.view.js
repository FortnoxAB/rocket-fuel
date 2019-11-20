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

        Promise.all([userQuestions,
            latestQuestions,
            popularQuestions,
            popularUnansweredQuestions,
            recentlyAcceptedQuestions]).then((response) => {
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
            return <QuestionRow key={index} question={question} onDeleteQuestion={this.fetchQuestions.bind(this)} />;
        });
    }

    getLatestQuestions() {
        return this.state.latestQuestions.map((question, index) => {
            return <QuestionRow key={index} question={question} onDeleteQuestion={this.fetchQuestions.bind(this)} />;
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
                <h2>
                    All questions
                </h2>
                <div className="row spacing">
                    <div className="col-8 col-sm-12 column spacing">
                        <div>
                            <div className="headline"><i className="fas fa-clock" /> {t`Recent`}</div>
                            {this.getLatestQuestions()}
                        </div>
                        <div>
                            <div className="headline"><i className="fas fa-user-clock" /> {t`Your recent`}</div>
                            {this.getUserQuestions()}
                        </div>
                    </div>
                    <div className="col-4 col-sm-12 column spacing">
                        <div>
                            <div className="headline"><i className="fas fa-burn" /> {t`Popular`}</div>
                            {this.getPopularQuestions()}
                        </div>
                        <div>
                            <div className="headline"><i className="fas fa-eye" /> {t`Popular unanswered`}</div>
                            {this.getPopularUnansweredQuestions()}
                        </div>
                        <div>
                            <div className="headline"><i className="fas fa-certificate" /> {t`Recently accepted`}</div>
                            {this.getRecentlyAcceptedQuestions()}
                        </div>
                        <div>
                            <div className="headline"><i className="fas fa-tags" /> {t`Popular tags`}</div>
                            Tags goes here
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

HomeView.contextType = UserContext;

export default HomeView;

import React from 'react';
import { t } from 'ttag';
import { withRouter, NavLink } from 'react-router-dom';
import QuestionRow from '../../components/questions/questionrow';
import Loader from '../../components/utils/loader';
import Button from '../../components/forms/button';
import InputField from '../../components/forms/inputfield';
import * as Question from '../../models/question';

class QuestionsView extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            loaded: false,
            questions: [],
            searchStr: '',
            loadingSearch: false,
            searchResult: [],
            searched: false
        };
    }

    componentDidMount() {
        this.fetchQuestions();
    }

    handleChange(node) {
        const value = node.target.value;
        clearTimeout(this.searchTimer);

        const name = node.target.name;
        this.setState({
            [name]: value,
            searched: false
        });

        if (value === '') {
            this.setState({
                searchResult: []
            });
            return;
        }
        this.searchTimer = setTimeout(() => {
            this.fetchSearch();
            this.setState({
                loadingSearch: true,
                searched: true
            });
        }, 700);
    }

    fetchSearch() {
        Question.searchQuestions(this.state.searchStr).then((questions) => {
            this.setState({
                searchResult: questions,
                loadingSearch: false
            });
        });
    }

    fetchQuestions() {
        Question.getQuestionsFromUser(2).then((questions) => {
            this.setState({
                questions: questions,
                loaded: true
            });
        });
    }

    renderQuestionRows() {
        return this.state.questions.map((question, index) => {
            return <QuestionRow key={index} question={question} />;
        });
    }

    renderSearchResult() {
        if (this.state.loadingSearch) {
            return <div className="padded-vertical"><Loader /></div>;
        }

        if (this.state.searchResult.length <= 0 && this.state.searchStr.length > 0 && this.state.searched) {
            return <div className="padded-bottom-large">
                {t`No questions found.`}
            </div>;
        }

        if (this.state.searchResult.length <= 0) {
            return null;
        }

        const searchResult = this.state.searchResult.map((question, index) => {
            return <QuestionRow small key={index} question={question} />;
        });
        return (
            <div className="padded-bottom-large">
                <h2>{t`Search result`}</h2>
                {searchResult}
            </div>
        )
    }

    navigate(url) {
        this.props.history.push(url);
    }

    render() {
        if (!this.state.loaded) {
            return (
                <Loader fillPage />
            );
        }
        return (
            <div>
                <div className="flex flex-end padded-bottom">
                    <Button color="primary" onClick={this.navigate.bind(this, '/create/question')}>
                        {t`New question`}
                    </Button>
                </div>

                <div className="padded-bottom">
                    <InputField
                        label={t`Search questions`}
                        onChange={this.handleChange.bind(this)}
                        name="searchStr"
                        value={this.state.searchStr}
                        autocomplete="off"
                        rounded
                        icon="fa-search"
                    />
                </div>

                <div>
                    {this.renderSearchResult()}
                    {this.renderQuestionRows()}
                </div>
            </div>
        );
    }
}

export default withRouter(QuestionsView);

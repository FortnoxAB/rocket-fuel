import React from 'react';
import { t } from 'ttag';
import { withRouter } from 'react-router-dom';
import QuestionRow from '../components/questions/questionrow';
import Loader from '../components/utils/loader';
import InputField from '../components/forms/inputfield';
import * as Question from '../models/question';

class SearchView extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            questions: [],
            searchStr: '',
            loadingSearch: false,
            searchResult: [],
            searched: false
        };
    }

    handleChange(node) {
        const value = node.target.value;
        const name = node.target.name;

        const searchQuery = value.trim().toLowerCase();
        const oldSearchQuery = this.state.searchStr.trim().toLowerCase();

        this.setState({
            searchStr:value
        });

        if(oldSearchQuery === searchQuery) {
            return;
        }

        if(searchQuery === '') {
            this.setState({
                loadingSearch: false,
                searched: false
            });
        } else {
            this.setState({
                searched: false,
                loadingSearch: true
            });
            clearTimeout(this.searchTimer);
            this.searchTimer = setTimeout(() => {
                Question.searchQuestions(searchQuery).then((questions) => {
                    console.log(questions);
                    this.setState({
                        searchResult: questions,
                        loadingSearch: false,
                        searched: true
                    });
                });
            }, 700);
        }
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

        if (this.state.searchStr.trim() === "") {
            return <div className="padded-bottom-large">
                {t`What are you waiting for? Type something!`}
            </div>;
        }

        if (this.state.searchResult.length === 0 && this.state.searched) {
            return <div className="padded-bottom-large">
                {t`No questions found.`}
            </div>;
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
        return (
            <div>
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
                {this.renderSearchResult()}
            </div>
        );
    }
}

export default withRouter(SearchView);

import React from 'react';
import { t } from 'ttag';
import { withRouter, NavLink } from 'react-router-dom';
import QuestionCard from '../../components/questions/questioncard';
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

	renderQuestionCards() {
		return this.state.questions.map((question, index) => {
			return  <QuestionCard key={index} question={question} />;
		});
	}

	renderSearchResult() {
		if (this.state.loadingSearch) {
			return <div className="padded-vertical"><Loader /></div>;
		}

		if (this.state.searchResult.length <= 0 && this.state.searchStr.length > 0 && this.state.searched) {
			return <div>{t`No questions found.`}</div>;
		}

		if (this.state.searchResult.length <= 0) {
			return null;
		}

		const searchResult = this.state.searchResult.map((question, index) => {
			return  <QuestionCard small key={index} question={question} />;
		});
		return (
			<div className="underlined">
				<h2>{t`Search result`}</h2>
				{searchResult}
			</div>
		)
	}

	render() {
		if (!this.state.loaded) {
			return(
					<Loader fillPage />
			);
		}
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
                        className="lg"
                    />
                </div>

				<div className="flex-end padded-bottom">
					<NavLink to="/create/thread">
						<Button color="secondary"><i className="fa fa-pencil" /> {t`New question`}</Button>
					</NavLink>
				</div>

				<div>
					{this.renderSearchResult()}
					{this.renderQuestionCards()}
				</div>
			</div>
		);
	}
}

export default withRouter(QuestionsView);

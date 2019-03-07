import React from 'react';
import { t } from 'ttag';
import { withRouter, NavLink } from 'react-router-dom';
import QuestionCard from '../../components/questioncard';
import FillPage from '../../components/utils/fillpage';
import Loader from '../../components/utils/loader';
import Button from '../../components/button';
import InputField from '../../components/inputfield';
import * as Question from '../../models/question';

class QuestionsView extends React.Component {
	constructor(props) {
		super(props);

		this.state = {
			loaded: false,
			questions: [],
			searchStr: '',
			loadingSearch: false
		};
	}

	componentDidMount() {
		this.fetchQuestions();
	}

	handleChange(target) {
		const value = target.value.trim();
		clearTimeout(this.searchTimer);
		if (value !== '') {
			this.searchTimer = setTimeout(() => {
				this.fetchSearch();
				this.setState({
					loadingSearch: true
				});
			}, 700);
		}
		const name = target.name;
		this.setState({
			[name]: value
		});
	}

	fetchSearch() {
		Question.searchQuestions(this.state.searchStr).then((questions) => {
			console.log(questions);
		});
	}

	fetchQuestions() {
		Question.getQuestions(2).then((questions) => {
			this.setState({
				questions: questions,
				loaded: true
			});
		});
		/*
		this.setState({loaded: false});
		this.context.threads().getAllTreads({
			limit:50
		}).then((threads)=> {
			this.setState({
				loaded: true,
				threads: threads
			});
		});
		*/
	}

	renderQuestionCards() {
		return this.state.questions.map((question, index) => {
			return  <QuestionCard key={index} question={question} />;
		});
	}

	renderSearchResult() {
		if (this.state.loadingSearch) {
			setTimeout(() => {
				this.setState({
					loadingSearch: false
				});
			}, 2000);
			return <Loader />;
		}
	}

	render() {
		if (!this.state.loaded) {
			return(
					<Loader fillPage />
			);
		}
		return (
			<div>
				<div className="padded-bottom flex-end">
					<NavLink to="/create/thread">
						<Button color="secondary"><i className="fa fa-pencil" /> {t`New question`}</Button>
					</NavLink>
				</div>

				<div className="flex-grow padded-bottom">
					<InputField
						placeholder={t`Search question`}
						onChange={this.handleChange.bind(this)}
						name="searchStr"
						value={this.state.searchStr}
						className="flex-grow"
					/>
				</div>

				<div>
					{this.renderQuestionCards()}
					{this.renderSearchResult()}
				</div>
			</div>
		);
	}
}

export default withRouter(QuestionsView);

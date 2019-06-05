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
			questions: [],
			loaded: false
		};
	}

	componentDidMount() {
		this.fetchQuestions();
	}

	fetchQuestions() {
		const user = this.context.state.user;
		Question.getQuestionsFromUser(user.id).then((questions) => {
			this.setState({
				questions: questions,
				loaded: true
			});
		});
	}

	getUsersPost() {
		return this.state.questions.map((question, index) => {
			return  <QuestionCard small key={index} question={question} />;
		});
	}

	getDisplayName() {
		return this.context.state.user.name;
	}

	render() {
		if (!this.state.loaded) {
			return(
				<Loader fillPage />
			);
		}
		return (
			<div>
				<div className="user-space">
					<h1>{this.getDisplayName()}</h1>

				</div>
				<div>
					<h3>{t`Your recent questions`}</h3>
					{this.getUsersPost()}
				</div>
			</div>
		)

	}
}

HomeView.contextType = UserContext;

export default HomeView;

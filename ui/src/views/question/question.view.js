import React from 'react';
import { t } from 'ttag';
import { withRouter } from 'react-router-dom';
import FillPage from '../../components/utils/fillpage';
import Loader from '../../components/utils/loader';
import Markdown from '../../components/markdown';
import InputField from '../../components/inputfield';
import Button from '../../components/button';
import Question from '../../components/question';
import Answer from '../../components/answer';
import * as QuestionApi from '../../models/question';

class QuestionView extends React.Component {
	constructor(props) {
		super(props);

		this.state = {
			loaded: false,
			question: null,
			answer: '',
			answers: [],
			postingAnswer: false
		};
	}

	onChangeAnswer(node) {
		this.setState({
			answer: node.target.value
		});
	}

	componentDidMount() {
		const questionId = this.props.match.params.id;
		this.loadThreadAndAnswers(questionId);
	}

	loadThreadAndAnswers(questionId) {
		QuestionApi.getQuestionById(questionId).then((question) => {
			this.setState({
				loaded: true,
				question: question
			})
		}).catch(() => {
			this.props.history.replace('../404');
		});
		/*
		const getThread = this.context.questions().getThread(questionId);
		const getAnswers = this.context.questions().getAnswers(questionId);
		Promise.all([getThread,getAnswers]).then((values) => {
			const question = values[0];
			const answers = this.placeAcceptedAnswerAtTop(values[1]);
			this.setState({
				loaded: true,
				question: question,
				answers: answers
			});
		});
		*/
	}

	placeAcceptedAnswerAtTop(answers) {
		const notAccepted = answers.filter(answer => answer.accepted !== true);
		if(notAccepted.length !== answers.length) {
			const accepted = answers.find(answer => answer.accepted === true);
			return [accepted].concat(notAccepted);
		}
		return answers
	}

	saveAnswer() {
		/*
		this.setState({
			postingAnswer: true
		});
		this.context.questions().addAnswerToThread(this.props.match.params.id, {
			answer: this.state.answer,
			votes: 0,
			accepted: false,
			created: Date.now()
		}).then(() => {
			this.setState({
				answer:'',
				postingAnswer: false
			});
			this.loadThreadAndAnswers(this.props.match.params.id);
		}).catch(() => {
			this.setState({
				postingAnswer: false
			});
		});
		*/
	}

	renderAnswers() {
		return this.state.answers.map((answer, index) => {
			return  <Answer onAnswer={this.onAnswerAccepted.bind(this)} enableAnswer={!this.state.question.answerAccepted} answer={answer} key={index} />;
		});
	}

	onAnswerAccepted(answer) {
		/*
		this.context.questions().markAnswerAsAccepted(this.state.question.id,answer.id)
			.then(() => {

				let foundAnswer = this.state.answers.find((a) => a.id === answer.id);
				foundAnswer.accepted = true;

				this.setState({
					question: { ...this.state.question,
						answered: true
					 },
					 answers: this.state.answers
				})
			});
			*/
	}

	renderAnswerForm() {
		if(this.state.postingAnswer) {
			return <Loader />;
		}

		return(
			<div className="answer-form">
				<InputField
					type="textarea"
					name="answer"
					value={this.state.answer}
					onChange={this.onChangeAnswer.bind(this)}
				/>
				<Button color="secondary" onClick={this.saveAnswer.bind(this)}>{t`Post answer`}</Button>
			</div>
		);

	}

	renderPreview() {
		if (!this.state.answer) {
			return null;
		}
		return (
			<div className="padded-vertical">
				<div className="underlined">{t`Preview`}</div>
				<Markdown text={this.state.answer} />
			</div>
		);
	}

	render() {
		if (!this.state.loaded) {
			return(
				<FillPage>
					<Loader />
				</FillPage>
			);
		}

		return (
			<div className="content">
				<Question question={this.state.question} />
				<h3>{t`Answers`}</h3>
				{this.renderAnswers()}
				{this.renderAnswerForm()}
				{this.renderPreview()}
			</div>
		);
	}
}

export default withRouter(QuestionView);

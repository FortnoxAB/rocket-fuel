import React from 'react';
import { withRouter } from 'react-router-dom';
import FillPage from '../../components/utils/fillpage';
import Loader from '../../components/utils/loader';
import InputField from '../../components/inputfield';
import Button from '../../components/button';
import Thread from '../../components/thread';
import Answer from '../../components/answer';
import * as Question from '../../models/question';

class QuestionView extends React.Component {
	constructor(props) {
		super(props);

		this.state = {
			loaded: false,
			thread: null,
			answer: '',
			answers: [],
			postingAnswer: false
		};
	}

	onChangeAnswer(target) {
		this.setState({
			answer: target.value
		});
	}

	componentDidMount() {
		const questionId = this.props.match.params.id;
		this.loadThreadAndAnswers(questionId);
	}

	loadThreadAndAnswers(questionId) {
		Question.getQuestions(questionId).then((question) => {
			this.setState({
				loaded: true,
				question: question
			})
		});
		/*
		const getThread = this.context.threads().getThread(questionId);
		const getAnswers = this.context.threads().getAnswers(questionId);
		Promise.all([getThread,getAnswers]).then((values) => {
			const thread = values[0];
			const answers = this.placeAcceptedAnswerAtTop(values[1]);
			this.setState({
				loaded: true,
				thread: thread,
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
		this.context.threads().addAnswerToThread(this.props.match.params.id, {
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
			return  <Answer onAnswer={this.onAnswerAccepted.bind(this)}enableAnswer={!this.state.thread.answered} answer={answer} key={index} />;
		});
	}

	onAnswerAccepted(answer) {
		/*
		this.context.threads().markAnswerAsAccepted(this.state.thread.id,answer.id)
			.then(() => {

				let foundAnswer = this.state.answers.find((a) => a.id === answer.id);
				foundAnswer.accepted = true;
		
				this.setState({
					thread: { ...this.state.thread,  
						answered: true
					 },
					 answers: this.state.answers
				})
			});
			*/
	}

	renderAnswerForm() {
		const user = window.sessionStorage.getItem('user');

		if (!user) {
			return <div>Logga in för att svara på tråden.</div>;
		}

		if(this.state.postingAnswer) {
			return <Loader />
		}

		return(
			<div className="answer-form">
				<InputField
					type="textarea"
					name="answer"
					value={this.state.answer}
					onChange={this.onChangeAnswer.bind(this)}
				/>
				<Button onClick={()=>this.saveAnswer.bind(this)}>Skicka svar</Button>
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
				<Thread thread={this.state.question} />
				<h3>Svar</h3>
				{this.renderAnswers()}
				{this.renderAnswerForm()}
			</div>
		);
	}
}

export default withRouter(QuestionView);

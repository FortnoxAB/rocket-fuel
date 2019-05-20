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
import * as AnswerApi from '../../models/answer';
import { AppContext } from '../../appcontext';


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
				question: question
			});

			AnswerApi.getAnswersByQuestionId(questionId).then((resp) => {

				this.setState({
					loaded: true,
					answers : resp
				});
			})

		}).catch(() => {
			this.props.history.replace('../404');
		});
	}

	saveAnswer() {

		this.setState({
			postingAnswer: true
		});

		const answer = {
			answer: this.state.answer
        };

		AnswerApi.answerQuestion(answer, this.props.match.params.id, this.context.state.token).then((resp) => {
		    this.setState({
		        answer: '',
		        postingAnswer: false
		    });
			this.loadThreadAndAnswers(this.props.match.params.id);
		});
	}

	renderAnswers() {
		return this.state.answers.map((answer, index) => {
			return  <Answer onAnswer={this.onAnswerAccepted.bind(this)} enableAnswer={!this.state.question.answerAccepted} answer={answer} key={index} />;
		});
	}

	onAnswerAccepted(answer) {
		AnswerApi.acceptAnswer(answer.id).then(() => {
			this.loadThreadAndAnswers(this.props.match.params.id);
		});
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

const WrappedQuestionView = withRouter(QuestionView);
WrappedQuestionView.WrappedComponent.contextType = AppContext;
export default WrappedQuestionView;

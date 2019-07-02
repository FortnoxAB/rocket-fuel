import React from 'react';
import { t } from 'ttag';
import { withRouter } from 'react-router-dom';
import FillPage from '../../components/helpers/fillpage';
import Loader from '../../components/utils/loader';
import Markdown from '../../components/helpers/markdown';
import InputField from '../../components/forms/inputfield';
import Button from '../../components/forms/button';
import Post from '../../components/questions/post';
import Answer from '../../components/questions/answer';
import * as QuestionApi from '../../models/question';
import * as AnswerApi from '../../models/answer';
import { UserContext } from '../../usercontext';
import Question from '../../components/questions/question';


class QuestionView extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            loaded: false,
            question: null,
            answer: '',
            answers: [],
            postingAnswer: false,
            answerError: null,
            owned: false
        };
    }

    onChangeAnswer(node) {
        this.setState({
            answer: node.target.value
        });
    }

    componentDidMount() {
        const questionId = this.props.match.params.id;
        this.loadQuestionAndAnswers(questionId);
    }

    loadQuestionAndAnswers(questionId) {
        Promise.all([
            QuestionApi.getQuestionById(questionId),
            AnswerApi.getAnswersByQuestionId(questionId, this.context.state.token)
        ]).then((resp) => {
            this.setState({
                question: resp[0],
                owned: this.context.state.user.id === resp[0].userId,
                answers: resp[1],
                loaded: true
            });
        }).catch(() => {
            this.props.history.replace('../404');
        });
    }

    saveAnswer() {
        if (!this.state.answer || this.state.answer.trim().length <= 0) {
            this.setState({
                answerError: t`You cannot leave an empty answer.`
            });
            return;
        }

        this.setState({
            postingAnswer: true,
            answerError: null
        });

        const answer = {
            answer: this.state.answer
        };

        AnswerApi.answerQuestion(answer, this.props.match.params.id, this.context.state.token).then((resp) => {
            this.setState({
                answer: '',
                postingAnswer: false
            });
            this.loadQuestionAndAnswers(this.props.match.params.id);
        });
    }

    renderAnswers() {
        if (this.state.answers.length <= 0) {
            return (
                <div className="padded-bottom-large text-center">
                    {t`No answers, be the first one to answer this question.`}
                </div>
            );
        }
        return this.state.answers.map((answer, index) => {
            return (
                <Answer
                    onAnswer={this.onAnswerAccepted.bind(this)}
                    enableAccept={!this.state.question.answerAccepted && this.state.owned}
                    answer={answer}
                    key={index}
                    onDeleteAnswer={this.loadQuestionAndAnswers.bind(this, this.props.match.params.id)}
                    onEditAnswer={this.loadQuestionAndAnswers.bind(this, this.props.match.params.id)}
                    onUpVote={this.onUpVote.bind(this)}
                    onDownVote={this.onDownVote.bind(this)}
                />
             );
        });
    }

    onAnswerAccepted(answerId) {
        AnswerApi.acceptAnswer(answerId).then(() => {
            this.loadQuestionAndAnswers(this.props.match.params.id);
        });
    }

    onUpVote(answerId) {
        AnswerApi.upVoteAnswer(answerId)
            .then(() => this.loadQuestionAndAnswers(this.props.match.params.id));
    }

    onDownVote(answerId) {
        AnswerApi.downVoteAnswer(answerId)
            .then(() => this.loadQuestionAndAnswers(this.props.match.params.id));
    }

    renderAnswerForm() {
        return (
            <div className="answer-form">
                <InputField
                    markdown
                    label={t`Answer`}
                    type="textarea"
                    name="answer"
                    value={this.state.answer}
                    onChange={this.onChangeAnswer.bind(this)}
                    errorMessage={this.state.answerError}
                />
                <Button
                    color="secondary"
                    onClick={this.saveAnswer.bind(this)}
                    loading={this.state.postingAnswer}
                >
                    {t`Post answer`}
                </Button>
            </div>
        );

    }

    renderPreview() {
        if (!this.state.answer) {
            return null;
        }
        return (
            <div className="padded-vertical">
                <h3>{t`Preview`}</h3>
                <Markdown text={this.state.answer} />
            </div>
        );
    }

    render() {
        if (!this.state.loaded) {
            return (
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

const WrappedQuestionView                        = withRouter(QuestionView);
WrappedQuestionView.WrappedComponent.contextType = UserContext;
export default WrappedQuestionView;

import React from 'react';
import { withRouter } from 'react-router-dom';
import { t } from 'ttag';
import InputField from '../../components/forms/inputfield';
import Button from '../../components/forms/button';
import Loader from '../../components/utils/loader';
import * as Question from '../../models/question';
import { UserContext } from '../../usercontext';
import Post from '../../components/questions/post';

class CreateQuestionView extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            title: '',
            question: '',
            bounty: '0',
            postingQuestion: false,
            error: {
                title: null,
                question: null,
                bounty: null
            },
            editPost: null,
            loaded: true
        };
    }

    componentDidMount() {
        const questionId = parseInt(this.props.match.params.id, 10);
        const userId = this.context.state.user.id;
        if (!questionId) {
            return;
        }
        this.setState({
            loaded: false
        });
        Question.getQuestionById(questionId).then((question) => {
            if (question.userId !== userId) {
                this.setState({
                    loaded: true
                });
                this.props.history.replace('/create/question');
                return;
            }
            this.setState({
                title: question.title,
                question: question.question,
                bounty: question.bounty,
                editPost: questionId,
                loaded: true
            });
        }).catch(() => {
            this.setState({
                loaded: true
            });
            this.props.history.replace('/create/question');
        });
    }

    handleChange(node) {
        const target = node.target;
        const value  = target.value;
        const name   = target.name;
        this.setState({
            [name]: value
        });
    }

    handleChangeBounty(node) {
        let bountyValue = node.target.value;
        if (bountyValue < 0) {
            bountyValue = 0;
        }
        this.setState({
            bounty: bountyValue
        });
    }

    saveQuestion() {
        let hasErrors = false;
        this.validateAll();

        this.setState({
            postingQuestion: true
        });

        Object.values(this.state.error).forEach((msg) => {
            if (msg) {
                hasErrors = true;
            }
        });

        if (hasErrors) {
            this.setState({
                postingQuestion: false
            });

            return;
        }

        const question = {
            title: this.state.title,
            question: this.state.question,
            bounty: this.state.bounty
        };

        if (!this.state.editPost) {
            Question.createQuestion(question, this.context.state.token).then((resp) => {
                this.props.history.push(`/question/${resp.id}`);
            }).catch((e) => {
                this.setState({
                    postingQuestion: false
                });
            });
            return;
        }

        Question.updateQuestion(this.state.editPost, question).then(() => {
            this.props.history.push(`/question/${this.state.editPost}`);
        }).catch((e) => {
            this.setState({
                postingQuestion: false
            });
        });
    }


    renderPreview() {
        if (!this.state.question && !this.state.title) {
            return null;
        }
        return (
            <div className="padded-vertical">
                <div className="headline">{t`Preview`}</div>
                <Post
                    body={this.state.question}
                    title={this.state.title}
                    userName={this.context.state.user.name}
                    picture={this.context.state.user.picture}
                />
            </div>
        );
    }

    validateAll() {
        this.validateTitle(this.state.title, 'title');
        this.validateQuestion(this.state.question, 'question');
        // this.validateBounty(this.state.bounty, 'bounty');
    }

    validateTitle(value, fieldName) {
        let newError = null;
        if (value.trim().length <= 0) {
            newError = t`The title cannot be empty.`;
        }
        this.setError(fieldName, newError);
    }

    validateQuestion(value, fieldName) {
        let newError = null;
        if (value.trim().length <= 0) {
            newError = t`The question cannot be empty.`;
        }
        this.setError(fieldName, newError);
    }

    validateBounty(value, fieldName) {
        let newError = null;
        if (!value || value < 0) {
            newError = t`Bounty must be zero or more.`;
        }
        if (value.indexOf('.') !== -1) {
            newError = t`Bounty cannot contain decimals.`;
        }

        this.setError(fieldName, newError);
    }

    setError(fieldName, errorMessage) {
        const newErrors      = this.state.error;
        newErrors[fieldName] = errorMessage;

        this.setState({
            error: newErrors
        });
    }

    renderTitle() {
        if (this.state.editPost) {
            return t`Edit question`;
        }

        return t`New question`;
    }

    renderButtonTitle() {
        if (this.state.editPost) {
            return t`Save question`;
        }

        return t`Post question`;
    }

    render() {
        if (!this.state.loaded) {
            return <Loader fillPage />
        }

        return (
            <div>
                <div className="headline">{this.renderTitle()}</div>
                <div className="form">
                    <InputField
                        className="padded-bottom"
                        onChange={this.handleChange.bind(this)}
                        name="title"
                        label={t`Title`}
                        type="text"
                        value={this.state.title}
                        errorMessage={this.state.error.title}
                        autocomplete="off"
                        validate={this.validateTitle.bind(this)}
                    />
                    <InputField
                        markdown
                        className="padded-bottom"
                        onChange={this.handleChange.bind(this)}
                        name="question"
                        label={t`Question`}
                        type="textarea"
                        value={this.state.question}
                        errorMessage={this.state.error.question}
                        validate={this.validateQuestion.bind(this)}
                    />
                    {/*<InputField
						className="padded-bottom"
						placeholder={t`Bounty`}
						onChange={this.handleChangeBounty.bind(this)}
						name="bounty"
                        label="Bounty"
						type="number"
						value={this.state.bounty}
                        errorMessage={this.state.error.bounty}
                        validate={this.validateBounty.bind(this)}
					/>*/}
                    <Button
                        color="secondary"
                        loading={this.state.postingQuestion}
                        onClick={this.saveQuestion.bind(this)}
                    >
                        {this.renderButtonTitle()}
                    </Button>
                </div>
                {this.renderPreview()}
            </div>
        );
    }
}

CreateQuestionView.contextType = UserContext;

export default withRouter(CreateQuestionView);

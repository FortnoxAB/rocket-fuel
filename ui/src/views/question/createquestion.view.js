import React from 'react';
import { withRouter } from 'react-router-dom';
import { t } from 'ttag';
import InputField from '../../components/forms/inputfield';
import Button from '../../components/forms/button';
import Markdown from '../../components/helpers/markdown';
import Loader from '../../components/utils/loader';
import * as Question from '../../models/question';
import { UserContext } from '../../usercontext';

class CreateQuestionView extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
			title: '',
			question: '',
			bounty: '0',
			postingThread: false,
            error: {
			    title: null,
                question: null,
                bounty: null
            }
		};
	}

	handleChange(node) {
		const target = node.target;
		const value = target.value;
		const name = target.name;
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

	saveThread() {
	    let hasErrors = false;
        this.validateAll();

		this.setState({
			postingThread: true
		});

        if (Object.keys(this.state.error).length > 0) {
            console.log(1);
        }

        Object.values(this.state.error).forEach((msg) => {
            if (msg) {
                hasErrors = true;
            }
        });

        if (hasErrors) {
            this.setState({
                postingThread: false
            });

            return;
        }

		const question = {
			title: this.state.title,
			question: this.state.description,
			bounty: this.state.bounty
		};

		Question.createQuestion(question, this.context.state.token).then((resp) => {
			this.props.history.push(`/question/${resp.id}`);
		});
	}

	renderPreview() {
		if (!this.state.question && !this.state.title) {
			return null;
		}
		return (
			<div className="padded-vertical">
				<div className="underlined">{t`Preview`}</div>
				<h1>{this.state.title}</h1>
				<Markdown text={this.state.question} />
			</div>
		);
	}

	validateAll() {
        this.validateTitle(this.state.title, 'title');
        this.validateQuestion(this.state.question, 'question');
        this.validateBounty(this.state.bounty, 'bounty');
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
        if (value.indexOf(".") !== -1) {
            newError = t`Bounty cannot contain decimals.`;
        }

        this.setError(fieldName, newError);
    }

    setError(fieldName, errorMessage) {
        const newErrors = this.state.error;
        newErrors[fieldName] = errorMessage;

        this.setState({
            error: newErrors
        });
    }

	render() {
		if(this.state.postingThread) {
			return <Loader fillPage />
		}

		return (
			<div>
				<h1>{t`New question`}</h1>
                <p></p>
				<div className="form">
					<InputField
						className="padded-bottom"
						placeholder={t`Title`}
						onChange={this.handleChange.bind(this)}
						name="title"
                        label="Title"
						type="text"
						value={this.state.title}
                        errorMessage={this.state.error.title}
                        autocomplete="off"
                        validate={this.validateTitle.bind(this)}
					/>
					<div className="padded-bottom">
						{t`Use Markdown in question field.`} <a href="https://guides.github.com/features/mastering-markdown/" target="_blank">{t`Markdown-syntax`}</a>
					</div>
					<InputField
						className="padded-bottom"
						placeholder={t`Question`}
						onChange={this.handleChange.bind(this)}
						name="question"
                        label="Question"
						type="textarea"
						value={this.state.question}
                        errorMessage={this.state.error.question}
                        validate={this.validateQuestion.bind(this)}
					/>
					<InputField
						className="padded-bottom"
						placeholder={t`Bounty`}
						onChange={this.handleChangeBounty.bind(this)}
						name="bounty"
                        label="Bounty"
						type="number"
						value={this.state.bounty}
                        errorMessage={this.state.error.bounty}
                        validate={this.validateBounty.bind(this)}
					/>
					<Button color="secondary" onClick={this.saveThread.bind(this)}>{t`Post question`}</Button>
				</div>
				{this.renderPreview()}
			</div>
		);
	}
}

CreateQuestionView.contextType = UserContext;

export default withRouter(CreateQuestionView);

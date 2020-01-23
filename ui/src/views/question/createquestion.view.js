import React from 'react';
import {withRouter} from 'react-router-dom';
import {t} from 'ttag';
import InputField from '../../components/forms/inputfield';
import Button from '../../components/forms/button';
import Loader from '../../components/utils/loader';
import * as Question from '../../models/question';
import * as Tag from '../../models/tag';
import {UserContext} from '../../usercontext';
import Post from '../../components/questions/post';

class CreateQuestionView extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            title: '',
            question: '',
            bounty: '0',
            tag: '',
            activeTags: [],
            tagSearchResult: null,
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
                activeTags: question.tags.map(tag => tag.label),
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

    handleTagChange(node) {
        clearTimeout(this.searchTimer);
        const target = node.target;
        const value  = target.value.replace(/\s/g, '').toLowerCase();

        this.setState({
            tag: value
        });

        if (value.length <= 0) {
            this.setState({
                tagSearchResult: null
            });
            return;
        }

        this.searchTimer = setTimeout(() => {
            Tag.searchTag(value).then((result) => {
                this.setState({
                    tagSearchResult: result.map(tag => tag.label)
                });
            })
        }, 700);
    }

    handleChange(node) {
        const target = node.target;
        const value  = target.value;
        const name   = target.name;

        this.setState({
            [name]: value
        });
    }

    addTag(tag = null) {
        this.setError('tag', '');
        if (!tag) {
            tag = this.state.tag;
        }

        if (tag.length <= 0) {
            return;
        }

        if (this.hasMaxAmountOfTags()){
            this.setError('tag', t`You have the maximum amount of tags.`);
            return;
        }

        if (this.state.activeTags.indexOf(tag) > -1) {
            this.setError('tag', t`${tag} is already in your tags.`);
            return;
        }

        this.setState({
            activeTags: [...this.state.activeTags, tag],
            tag: '',
            tagSearchResult: null
        });
    }

    onKeyDownTags(e) {
        const isAddKeyPressed = e.key === 'Enter' || e.key === ' ';

        if (isAddKeyPressed) {
            this.addTag();
        }
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
            bounty: this.state.bounty,
            tags: this.state.activeTags
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
                    tags={this.state.activeTags}
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

    hasMaxAmountOfTags() {
        return this.state.activeTags.length >= 5;
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

    removeTagByIndex(index) {
        if (this.state.activeTags.length <= 0) {
            return;
        }
        const newTagsArray = this.state.activeTags;
        newTagsArray.splice(index, 1);
        this.setState({
            activeTags: newTagsArray
        });
    }

    renderTags() {
        if (this.state.activeTags.length <= 0) {
            return null;
        }

        return (
            <div className="flex padded-vertical">
                {this.state.activeTags.map((tag, i) => {
                    return (
                        <div className="tag flex" key={i}>
                            #{tag}
                            <div className="remove" onClick={this.removeTagByIndex.bind(this, i)}>
                                <i className="fa fa-times" />
                            </div>
                        </div>
                    );
                })}
            </div>
        );
    }

    renderTagsDropdown() {
        const searchResult = this.state.tagSearchResult;
        if (!searchResult) {
            return null;
        }

        return (
            <ul className="field-auto-complete">
                {
                    searchResult.map((tag, i) => {
                        return <li onClick={this.addTag.bind(this, tag)} key={i}>{tag}</li>
                    })
                }
            </ul>
        );
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
                    <div className="markdown-text">
                        {t`Enter a tag and press space/enter to add it, you may have up to five tags.`}
                    </div>
                    <div className="relative">
                        <InputField
                            onChange={this.handleTagChange.bind(this)}
                            onKeyPress={this.onKeyDownTags.bind(this)}
                            name="tag"
                            label={t`Tags`}
                            value={this.state.tag}
                            errorMessage={this.state.error.tag}
                            autocomplete="off"
                        />
                        {this.renderTagsDropdown()}
                    </div>
                    {this.renderTags()}
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

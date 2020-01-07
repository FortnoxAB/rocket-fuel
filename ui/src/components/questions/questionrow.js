import React from 'react';
import { withRouter } from 'react-router-dom';
import { t } from 'ttag';
import Certificate from '../utils/certificate';
import Trophy from '../utils/trophy';
import { UserContext } from '../../usercontext';
import Post from './post';
import Tags from './tags';

class QuestionRow extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            user: null
        };
    }

    componentDidMount() {
        this.setState({
            user: this.context.state.user,
            isDropdownOpen: false
        });
    }

    getState() {
        if (this.props.question.answerAccepted) {
            return 'answered';
        }
        return 'unanswered';
    }

    printCertificate() {
        const hasAccepted = this.props.question.answerAccepted;
        return <Certificate active={hasAccepted} />;
    }

    printTrophy() {
        const hasEnoughVotes = this.props.question.votes > 20;
        if (!hasEnoughVotes) {
            return null;
        }
        return <Trophy active />;
    }

    getClasses() {
        return [
            'question-row flex',
            this.props.question.answerAccepted ? 'accepted' : '',
            this.props.small ? 'small' : ''
        ].join(' ');
    }

    renderTags() {
        if (this.props.hideTags) {
            return null;
        }

        const tags = this.props.question.tags;

        if (!tags || tags.length <= 0) {
            return null;
        }

        return tags.map((tag, i) => {
            return (
                <div className="tag" key={i}>
                    <div onClick={this.test.bind(this)}>{tag}</div>
                </div>
            );
        })
    }

    navigateToQuestion(questionId) {
        this.props.history.push(`/question/${questionId}`);
    }

    getVotesNumber() {
        const votes = this.props.question.votes;

        if (votes > 0) {
            return `+${votes}`;
        }

        return votes;
    }

    renderFooter() {
        if (this.props.hideTags) {
            return null;
        }
        if (this.props.small) {
            return (
                <div>
                    <Tags tags={this.props.question.tags} />
                </div>
            );
        }

        return (
            <div className="footer">
                <div className="user">
                    <div className="username">{this.props.question.createdBy}</div>
                    <div>{Post.getTime(this.props.question.createdAt)}</div>
                </div>
                <div>
                    <Tags tags={this.props.question.tags} />
                </div>
            </div>
        );
    }

    renderUtils() {
        if (this.props.small) {
            return null;
        }

        return (
            <div className="utils flex-column">
                <div className="flex flex-column center-center">
                    <div className="votes">{this.getVotesNumber()}</div>
                    <div className="votes-text">{t`votes`}</div>
                </div>
                <div className="accepted">
                    {this.printCertificate()}
                </div>
            </div>
        );
    }

    render() {
        return (
            <div className={`${this.getClasses()} ${this.getState()}`}>
                <div className="content">
                    {this.renderUtils()}
                    <div className="body">
                        <div className="title" onClick={this.navigateToQuestion.bind(this, this.props.question.id)}>
                            {this.props.question.title}
                        </div>
                        {this.renderFooter()}
                    </div>
                </div>
            </div>
        );
    }
}

QuestionRow.defaultProps = {
    user: {},
    question: {},
    hideTags: false,
    small: false
};

export default withRouter(QuestionRow);

QuestionRow.contextType = UserContext;

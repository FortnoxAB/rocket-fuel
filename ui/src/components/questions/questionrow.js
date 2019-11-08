import React from 'react';
import { withRouter } from 'react-router-dom';
import { t } from 'ttag';
import Certificate from '../utils/certificate';
import Trophy from '../utils/trophy';
import { UserContext } from '../../usercontext';
import Post from './post';

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
        ].join(' ');
    }

    renderTags() {
        if (this.props.hideTags) {
            return null;
        }

        const tags = this.props.question.tags;

        if (!tags || tags.length <= 0) {
            return <div className="tag alternative">{t`No tags`}</div>
        }

        return tags.map((tag, i) => {
            return (
                <div className="tag" key={i}>
                    {tag}
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

    render() {
        return (
            <div className={`${this.getClasses()} ${this.getState()}`}>
                <div className="content" onClick={this.navigateToQuestion.bind(this, this.props.question.id)}>
                    <div className="utils flex-column">
                        <div className="flex flex-column center-center">
                            <div className="votes">{this.getVotesNumber()}</div>
                            <div className="votes-text">{t`votes`}</div>
                        </div>
                        <div className="accepted">
                            {this.printCertificate()}
                        </div>
                    </div>
                    <div className="body">
                        <div className="title">
                            {this.props.question.title}
                        </div>
                        <div className="footer">
                            <div className="user">
                                <div className="username">{this.props.question.createdBy}</div>
                                <div>{Post.getTime(this.props.question.createdAt)}</div>
                            </div>
                            <div className="tags">
                                {this.renderTags()}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );

        return (
            <div className={`${this.getClasses()} ${this.getState()}`}>
                <div className="content" onClick={this.navigateToQuestion.bind(this, this.props.question.id)}>
                    {/*<Coins amount={this.props.question.bounty} />*/}
                    <div className="marks">
                        {this.printCertificate()}
                        {this.printTrophy()}
                    </div>
                    <div className="body">
                        <div className="title">
                            {this.props.question.title}
                            <div className="tags">
                                {this.renderTags()}
                            </div>
                        </div>
                        <div className="user">
                            <div>{this.props.question.createdBy}</div>
                            <div>{Post.getTime(this.props.question.createdAt)}</div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

QuestionRow.defaultProps = {
    user: {},
    question: {},
    hideTags: false
};

export default withRouter(QuestionRow);

QuestionRow.contextType = UserContext;

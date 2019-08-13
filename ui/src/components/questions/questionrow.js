import React from 'react';
import { withRouter } from 'react-router-dom';

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
        if (!hasAccepted) {
            return null;
        }
        return <Certificate active={hasAccepted} />;
    }

    printTrophy() {
        const hasEnoughVotes = this.props.question.votes > 20;
        if (!hasEnoughVotes) {
            return null;
        }
        return <Trophy active={hasEnoughVotes} />;
    }

    getClasses() {
        let classes = 'question-row flex';
        if (this.props.small) {
            classes = `${classes} small`;
        }
        return classes;
    }

    renderTags() {
        if (this.props.hideTags) {
            return null;
        }

        return (
            <div className="tags">
                <div className="tag">JavaScript</div>
                <div className="tag">ReactJs</div>
                <div className="tag">Frontend</div>
            </div>
        );
    }

    navigateToQuestion(questionId) {
        this.props.history.push(`/question/${questionId}`);
    }

    render() {
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
                        </div>
                        <div className="user">
                            {this.props.question.createdBy}, {Post.getTime(this.props.question.createdAt)}
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
    small: false,
    hideTags: false
};

export default withRouter(QuestionRow);

QuestionRow.contextType = UserContext;

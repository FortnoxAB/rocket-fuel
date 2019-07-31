import React from 'react'
import Post from './post';
import { UserContext } from '../../usercontext';

class Answer extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <Post
                className="answer"
                body={this.props.answer.answer}
                userName={this.props.answer.createdBy}
                userId={this.props.answer.userId}
                created={this.props.answer.createdAt}
                votes={this.props.answer.votes}
                answered={this.props.answer.accepted}
                answerId={this.props.answer.id}
                picture={this.props.answer.picture}
                onDelete={this.props.onDeleteAnswer.bind(this)}
                onEdit={this.props.onEditAnswer.bind(this)}
                enableAccept={this.props.enableAccept}
                onAnswer={this.props.onAnswer.bind(this)}
                onUpVote={this.props.onUpVote.bind(this)}
                onDownVote={this.props.onDownVote.bind(this)}
                allowUpVote={this.isVoteAllowed(1)}
                allowDownVote={this.isVoteAllowed(-1)}
            />
        );
    }

    isVoteAllowed(voteValue) {
        return this.context.state.user.id !== this.props.answer.userId && this.props.answer.currentUserVote !== voteValue;
    }
}

Answer.defaultProps = {
    answer: null,
    enableAccept: true,
    onEditAnswer: () => {},
    onDeleteAnswer: () => {},
    onAnswer: () => {},
    onUpVote: () => {},
    onDownVote: () => {}
};

export default Answer;

Answer.contextType = UserContext;

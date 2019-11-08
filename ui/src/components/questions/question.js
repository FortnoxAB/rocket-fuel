import React from 'react';
import Post from './post';
import { UserContext } from '../../usercontext';

class Question extends React.Component {
    render() {
        return (
            <Post
                tags={this.props.question.tags}
                body={this.props.question.question}
                title={this.props.question.title}
                userName={this.props.question.createdBy}
                userId={this.props.question.userId}
                created={this.props.question.createdAt}
                votes={this.props.question.votes}
                answered={this.props.question.answerAccepted}
                questionId={this.props.question.id}
                picture={this.props.question.picture}
                onUpVote={this.props.onUpVote.bind(this)}
                onDownVote={this.props.onDownVote.bind(this)}
                allowUpVote={this.isVoteAllowed(1)}
                allowDownVote={this.isVoteAllowed(-1)}
            />
        );
    }

    isVoteAllowed(voteValue) {
        return this.context.state.user.id !== this.props.question.userId && this.props.question.currentUserVote !== voteValue;
    }
}

Question.defaultProps = {
    question: null,
    onUpVote: () => {},
    onDownVote: () => {}
};

export default Question;

Question.contextType = UserContext;

import React from 'react';
import Post from './post';

class Question extends React.Component {
    render() {
        return (
            <Post
                body={this.props.question.question}
                title={this.props.question.title}
                userName={this.props.question.createdBy}
                userId={this.props.question.userId}
                created={this.props.question.createdAt}
                votes={this.props.question.votes}
                answered={this.props.question.answerAccepted}
                questionId={this.props.question.id}
                picture={this.props.question.picture}
            />
        );
    }
}

Question.defaultProps = {
    question: null
};

export default Question;

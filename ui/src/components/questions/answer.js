import React from 'react'
import Post from './post';

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
            />
        );
    }
}

Answer.defaultProps = {
    answer: null,
    enableAccept: true,
    onEditAnswer: () => {},
    onDeleteAnswer: () => {}
};

export default Answer;

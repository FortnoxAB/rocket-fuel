import React from 'react'
import moment from 'moment';

class Answer extends React.Component {
	constructor(props) {
		super(props);
	}

	getTime() {
		return <i>{moment(this.props.answer.created).fromNow()}</i>;
	}

	renderAccepted() {
		if(!this.props.enableAnswer) {
			return null;
		}

		if (!this.props.answer.accepted) {
			return (
				<div className="unaccepted">
					<i className="fa fa-check" onClick={() => this.props.onAnswer(this.props.answer)}/>
				</div>
			);
		}

		return (
			<div className="accepted">
				<i className="fa fa-check" />
			</div>
		);
	}



	getClasses() {
		let className = 'answer';
		if (this.props.answer.accepted) {
			className = `${className} accepted`;
		}

		return className;
	}

	render() {
		return (
			<div className={this.getClasses()}>
				<div className="post-sidebar">
					<div className="vote">
						<i className="fa fa-caret-up" />
					</div>
					<div>
						{this.props.answer.votes}
					</div>
					<div className="vote">
						<i className="fa fa-caret-down" />
					</div>
					{this.renderAccepted()}
				</div>
				<div className="post-body">
					<div>
						<p>{this.props.answer.answer}</p>
					</div>
					<div className="post-footer">
						<div><i className="fa fa-user" /> {this.props.answer.user.name}</div>
						<div><i className="fa fa-clock-o" /> {this.getTime()}</div>
					</div>
				</div>
			</div>
		);
	}
}

Answer.defaultProps = {
	answer: null,
	enableAnswer: true,
	onAnswer: () => {}
};

export default Answer;

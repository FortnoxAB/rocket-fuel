import React from 'react';
import moment from 'moment/moment';
import Markdown from './markdown';
import Coins from './utils/coins';
import Certificate from './utils/certificate';

class Question extends React.Component {
	constructor(props) {
		super(props);

		this.state = {
			votes: 0
		};
	}

	componentDidMount() {
		this.setState({
			votes: this.props.question.votes
		});
	}

	renderAwarded() {
		if (this.state.votes <= 20) {
			return null;
		}

		return (
			<div className="golden">
				<i className="fa fa-trophy" />
			</div>
		);
	}

	renderAnswered() {
		if (this.props.question.answerAccepted) {
			return null;
		}

		return (
			<div className="accepted">
				<Certificate />
			</div>
		);
	}

	getTime() {
		return moment(this.props.question.createdAt).fromNow();
	}

	incrementVotes() {
		this.setState({
			votes: this.state.votes + 1
		});
	}

	decrementVotes() {
		this.setState({
			votes: this.state.votes - 1
		});
	}

	getClasses() {
		let classes = 'question';
		if (this.state.votes < -3) {
			classes = `${classes} faded`;
		}
		return classes;
	}

	render() {
		return (
			<div className={this.getClasses()}>
				<div className="post-sidebar">
					<div>
						<Coins amount={this.props.question.bounty} />
					</div>
					<div className="vote">
						<i onClick={this.incrementVotes.bind(this)} className="fa fa-caret-up" />
					</div>
					<div>
						{this.state.votes}
					</div>
					<div className="vote">
						<i onClick={this.decrementVotes.bind(this)} className="fa fa-caret-down" />
					</div>
					{this.renderAnswered()}
					{this.renderAwarded()}
				</div>
				<div className="post-body">
					<div>
						<h3>{this.props.question.title}</h3>
						<Markdown text={this.props.question.question} />
						</div>
					<div className="post-footer">
						<div><i className="fa fa-user" /> {this.props.question.createdBy}</div>
						<div><i className="fa fa-clock-o" /> {this.getTime()}</div>
					</div>
				</div>
			</div>
		);
	}
}

Question.defaultProps = {
	question: null
};

export default Question;

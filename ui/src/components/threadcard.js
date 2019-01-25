import React from 'react'
import moment from 'moment';

import Coins from '../components/utils/coins';

class ThreadCard extends React.Component {
	constructor(props) {
		super(props);
	}

	getDescription() {
		const description = this.props.thread.description;
		if (!description) {
			return '';
		}

		if (description.length > 200) {
			return `${description.slice(0, 200)}...`;
		}

		return description;
	}

	getTime() {
		return moment(this.props.thread.created).fromNow();
	}

	getState() {
		if (this.props.thread.answered) {
			return 'answered';
		}

		return 'unanswered';
	}

	navigateToThread() {
		window.location.href = `/thread/${this.props.thread.id}`;
	}

	render() {
		return (
			<div className={`thread-card ${this.getState()}`} onClick={this.navigateToThread.bind(this)}>
				<div className="user">
					<div>{this.props.thread.user.name}, {this.getTime()}</div>
					<Coins amount={this.props.thread.bounty} />
				</div>
				<div className="title">
					{this.props.thread.title}
				</div>
				<div className="description">
					{this.getDescription()}
				</div>
			</div>
			);
	}
}

ThreadCard.defaultProps = {
	user: {},
	thread: {}
};

export default ThreadCard;

import React from 'react'
import { NavLink } from 'react-router-dom';
import moment from 'moment';

import Coins from '../components/utils/coins';

class Questioncard extends React.Component {
	constructor(props) {
		super(props);
	}

	getTime() {
		return moment(this.props.question.createdAt).fromNow();
	}

	getState() {
		if (this.props.question.answerAccepted) {
			return 'answered';
		}

		return 'unanswered';
	}

	render() {
		return (
			<div className={`question-card ${this.getState()}`}>
				<div className="body">
					<NavLink className="title" to={`/question/${this.props.question.id}`}>
						{this.props.question.title}
					</NavLink>
					<Coins amount={this.props.question.bounty} />
				</div>
				<div className="user">
					<div>{this.props.question.createdBy}, {this.getTime()}</div>
					<div>
						<div className="tag">JavaScript</div>
						<div className="tag">ReactJs</div>
						<div className="tag">Frontend</div>
					</div>
				</div>
			</div>
			);
	}
}

Questioncard.defaultProps = {
	user: {},
	question: {}
};

export default Questioncard;

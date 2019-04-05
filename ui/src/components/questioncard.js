import React from 'react';
import { NavLink } from 'react-router-dom';
import Certificate from './utils/certificate';
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

	printCertificate() {
		if (!this.props.question.answerAccepted) {
			return null;
		}
		return <Certificate />;
	}

	getClasses() {
		let classes = 'question-card';
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

	render() {
		return (
			<div className={`${this.getClasses()} ${this.getState()}`}>
				<div className="body">
					<NavLink className="title" to={`/question/${this.props.question.id}`}>
							{this.printCertificate()}
						<div className="title-text">{this.props.question.title}</div>
					</NavLink>
					<Coins amount={this.props.question.bounty} />
				</div>
				<div className="flex-between question-footer">
					<div className="user">
						<div>{this.props.question.createdBy}, {this.getTime()}</div>
					</div>
					{this.renderTags()}
				</div>
			</div>
			);
	}
}

Questioncard.defaultProps = {
	user: {},
	question: {},
	small: false,
	hideTags: false
};

export default Questioncard;

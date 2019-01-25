import React from 'react'
import moment from 'moment/moment';

class Thread extends React.Component {
	renderAnswered() {
		if (!this.props.thread.answered) {
			return null;
		}

		return (
			<div className="accepted">
				<i className="fa fa-check" />
			</div>
		);
	}


	constructor(props) {
		super(props);

		this.state = {
			votes: 0
		};
	}

	getTime() {
		return moment(this.props.thread.created).fromNow();
	}

	componentDidMount() {
		this.setState({
			votes:this.props.thread.votes,
			enabled: true
		});
	}

	incrementVotes() {
		this.setState({
			votes: this.state.votes + 1,
			enabled: false
		});
	}

	decrementVotes() {
		this.setState({
			votes: this.state.votes - 1,
			enabled: false
		});
	}

	render() {
		return (
			<div className="thread">
				<div className="post-sidebar">
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
				</div>
				<div className="post-body">
					<div>
						<h3>{this.props.thread.title}</h3>
						<p>{this.props.thread.description}</p>
						</div>
					<div className="post-footer">
						<div><i className="fa fa-user" /> {this.props.thread.user.name}</div>
						<div><i className="fa fa-clock-o" /> {this.getTime()}</div>
					</div>
				</div>
			</div>
		);
	}
}

Thread.defaultProps = {
	thread: null
};

export default Thread;

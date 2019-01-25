import React from 'react';
import { withRouter, NavLink } from 'react-router-dom';
import ThreadCard from '../../components/threadcard';
import FillPage from '../../components/utils/fillpage';
import Loader from '../../components/utils/loader';

class ThreadsView extends React.Component {
	constructor(props) {
		super(props);
		this.headerLinks = [
			{ name: 'Nyast', path: '/threads' },
			{ name: 'Obesvarade', path: '/threads/unanswered' },
			{ name: 'Besvarade', path: '/threads/answered' }
		];

		this.state = {
			loaded: false,
			threads: []
		};
	}

	componentDidMount() {
		this.fetchThreads();
	}

	componentDidUpdate(prevProps) {
		const prevParam = prevProps.match.params.section;
		const param = this.props.match.params.section;

		if (prevParam === param) {
			return;
		}

		if (param === 'unanswered') {
			// fetch only unanswered
			this.fetchThreadsWithAnswerState(false);
			return;
		} else if (param === 'answered') {
			// fetch only answered
			this.fetchThreadsWithAnswerState(true);
			return;
		}

		//fetch newest only
		this.fetchThreads();
	}

	fetchThreadsWithAnswerState(state) {
		/*
		this.setState({loaded: false});
		this.context.threads().getThreadsWithAnswerState(state).then((threads)=> {
			this.setState({
				loaded: true,
				threads: threads
			});
		});
		*/
	}

	fetchThreads() {
		/*
		this.setState({loaded: false});
		this.context.threads().getAllTreads({
			limit:50
		}).then((threads)=> {
			this.setState({
				loaded: true,
				threads: threads
			});
		});
		*/
	}

	renderThreadCards() {
		return this.state.threads.map((thread, index) => {
			return  <ThreadCard key={index} thread={thread} />;
		});
	}

	renderCreateNewLink() {
		const user = window.sessionStorage.getItem('user');

		if (!user) {
			return null;
		}

		return (<NavLink to="/create/thread">Skapa ny</NavLink>);
	}

	render() {
		if (!this.state.loaded) {
			return(
				<FillPage>
					<Loader />
				</FillPage>
			);
		}

		return (
			<div>
				<div className="menu">
					<NavLink to="/threads/new">Nya</NavLink>
					<NavLink to="/threads/unanswered">Obesvarade</NavLink>
					<NavLink to="/threads/answered">Besvarade</NavLink>
					{this.renderCreateNewLink()}
				</div>
				<div className="content">
					{this.renderThreadCards()}
				</div>
			</div>
		);
	}
}

export default withRouter(ThreadsView);

import React from 'react'
import { Route, Switch, withRouter } from 'react-router-dom'

import { AppContext } from '../appcontext';
import Header from '../components/header';
import Userbar from '../components/userbar';

import HomeView from './home.view';
import ThreadsView from './threads/threads.view';
import ThreadView from './threads/thread.view';
import BloglistView from './bloglist.view';
import UsersView from './users.view';
import SearchView from './search.view';
import FaqView from './faq.view';
import CreateView from './create.view';
import PageNotFoundView from './pagenotfound.view';

class Routing extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
			signedIn: false
		};
	}

	componentDidMount() {
		/*
		this.unregisterAuthObserver = this.context.firebase.auth().onAuthStateChanged(
			(user) => this.setState({signedIn: !!user})
		);
		*/
	}

	componentWillUnmount() {
		this.unregisterAuthObserver();
	}

	render() {
		return (
			<div className="view">
				<Userbar signedIn={this.state.signedIn} />
				<Switch>
					<Route exact path="/"
					       render={() => <HomeView />} />
					<Route path="/threads/:section?"
					       render={() => <ThreadsView />} />
					<Route path="/thread/:id"
					       render={() => <ThreadView />} />
					<Route path="/bloglist/:id?"
					       render={() => <BloglistView />} />
					<Route path="/users/:userId?"
					       render={() => <UsersView />} />
					<Route path="/create/:type"
					       render={() => <CreateView />} />
					<Route path="/search"
					       render={() => <SearchView />} />
					<Route path="/faq"
					       render={() => <FaqView />} />
					<Route render={() => <PageNotFoundView />} />
				</Switch>
			</div>
		);
	}
}

export default withRouter(Routing);

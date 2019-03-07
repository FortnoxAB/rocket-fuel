import React from 'react'
import { Route, Switch, withRouter } from 'react-router-dom'
import { AppContext } from '../appcontext';
import SignedInView from '../components/signedinview';
import SignInView from './signInView';

import HomeView from './home.view';
import QuestionsView from './question/questions.view';
import QuestionView from './question/question.view';
import BloglistView from './bloglist.view';
import UsersView from './users.view';
import SearchView from './search.view';
import FaqView from './faq.view';
import CreatequestionView from './question/createquestion.view';
import PageNotFoundView from './pagenotfound.view';

class Routing extends React.Component {
	constructor(props) {
		super(props);
	}
	render() {
		if (!this.context.state.user) {
			return (
				<div className="view">
					<SignInView />
				</div>
			)
		}
		return (
			<SignedInView>
				<Switch>
					<Route exact path="/"
					       render={() => <HomeView />} />
					<Route path="/questions"
					       render={() => <QuestionsView />} />
					<Route path="/question/:id"
					       render={() => <QuestionView />} />
					<Route path="/bloglist/:id?"
					       render={() => <BloglistView />} />
					<Route path="/users/:userId?"
					       render={() => <UsersView />} />
					<Route path="/create/:type"
					       render={() => <CreatequestionView />} />
					<Route path="/search"
					       render={() => <SearchView />} />
					<Route path="/faq"
					       render={() => <FaqView />} />
					<Route render={() => <PageNotFoundView />} />
				</Switch>
			</SignedInView>
		);
	}
}

export default withRouter(Routing);

Routing.contextType = AppContext;

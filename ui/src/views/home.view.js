import React from 'react';

import SignInView from './signInView';
import FillPage from '../components/utils/fillpage';
import Loader from '../components/utils/loader';

class HomeView extends React.Component {
	constructor(props) {
		super(props);

		this.state = {
			userSignedIn: null,
			loaded: false
		};
	}

	componentDidMount() {
		/*
		this.unregisterAuthObserver = this.context.firebase.auth().onAuthStateChanged(
			(user) => {
				if (!user) {
					this.setState({loaded: true});
					return;
				}
				const userObj = {
					name: user.displayName,
					id: user.uid,
					email: user.email
				};
				window.sessionStorage.setItem('user', JSON.stringify(userObj));
				this.setState({userSignedIn: !!user, loaded: true});
			}
		);
		*/
	}

	componentWillUnmount() {
		this.unregisterAuthObserver();
	}


	getUserDisplayName() {
		const user = JSON.parse(window.sessionStorage.getItem('user'));
		return user.name;
	}

	render() {
		if (!this.state.loaded) {
			return(
				<FillPage>
					<Loader />
				</FillPage>
			);
		}

		if (!this.state.userSignedIn) {
			return (
				<div className="content">
					<SignInView />
				</div>
			)
		}
		return (
			<div className="content">
				<div className="user-space">
					<h2>{this.getUserDisplayName()}</h2>

				</div>
				<div className="flex-row grow">
					<div>
						<b>Senaste trådarna</b>
						<div className="thread-card">
							placeholder
						</div>
						<div className="thread-card">
							placeholder
						</div>
						<div className="thread-card">
							placeholder
						</div>
						<div className="thread-card">
							placeholder
						</div>
						<div className="thread-card">
							placeholder
						</div>
					</div>

					<div>
						<b>Populäraste trådarna</b>
						<div className="thread-card">
							placeholder
						</div>
						<div className="thread-card">
							placeholder
						</div>
						<div className="thread-card">
							placeholder
						</div>
						<div className="thread-card">
							placeholder
						</div>
						<div className="thread-card">
							placeholder
						</div>
					</div>

					<div>
						<b>Äldsta obesvarade trådarna</b>
						<div className="thread-card">
							placeholder
						</div>
						<div className="thread-card">
							placeholder
						</div>
						<div className="thread-card">
							placeholder
						</div>
						<div className="thread-card">
							placeholder
						</div>
						<div className="thread-card">
							placeholder
						</div>
					</div>
				</div>
			</div>
		)

	}
}

export default HomeView;

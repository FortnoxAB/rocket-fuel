import React from 'react';
import { withRouter } from 'react-router-dom';
import { t } from 'ttag';
import { googleClientId } from '../../config';
import Button from '../components/button';
import Loader from '../components/utils/loader';
import Logo from '../components/utils/logo';
import * as User from '../models/user';
import { AppContext } from '../appcontext';

class SignInView extends React.Component {
	constructor(props) {
		super(props);
		this.GoogleAuth = null;
		this.GoogleUser = null;

		this.state = {
			loaded: false
		};
	}

	componentDidMount() {
		gapi.load('auth2', () => {
			gapi.auth2.init({
				client_id: googleClientId
			});
			this.GoogleAuth = gapi.auth2.getAuthInstance();
			this.setupSignInListener();

			this.GoogleAuth.then(() => {
				this.GoogleUser = this.GoogleAuth.currentUser.get();

				if (this.GoogleUser.isSignedIn()) {
					const token = this.GoogleUser.getAuthResponse().id_token;
					this.signInUser(token);
					return;
				}
				this.setState({
					loaded: true
				});
			});
		});
	}

	setupSignInListener() {
		this.GoogleAuth.isSignedIn.listen((isSignedIn) => {
			if (isSignedIn) {
				const token = this.GoogleUser.getAuthResponse().id_token;
				this.signInUser(token);
				return;
			}
			this.updateUserInContext(null);
		});
	}

	signInUser(token) {
		User.signIn(token).then((user) => {
			this.updateUserInContext(user, token);
			this.props.history.push('/');
		})
			.catch(() => {
				this.updateUserInContext(null);
				this.setState({
					loaded: true
				});
			});
	}

	updateUserInContext(user, token = null) {
		this.context.setState({
			user: user,
			token: token
		});
	}

	onSignIn() {
		this.GoogleAuth.signIn();
	}

	render() {
		if (!this.state.loaded) {
			return <Loader fillPage />
		}
		return (
			<div className="center-center fill-page flex-column">
				<Logo size="large" />
				<Button color="google" onClick={this.onSignIn.bind(this)}>{t`Login`} <i className="fa fa-google" /> </Button>
			</div>
		);
	}
}

const WrappedSignInView = withRouter(SignInView);

WrappedSignInView.WrappedComponent.contextType = AppContext;

export default WrappedSignInView;

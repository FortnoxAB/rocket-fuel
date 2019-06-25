import React from 'react';
import { withRouter } from 'react-router-dom';
import { t } from 'ttag';
import Button from '../components/forms/button';
import Logo from '../components/utils/logo';
import { UserContext } from '../usercontext';
import src from '../images/space-bg.mp4';

class SignInView extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div className="fill-page center-center flex-column login-view">
                <video autoPlay muted loop className="fullscreen-video">
                    <source src={src} type="video/mp4" />
                </video>
                <Logo size="large" className="text-light" />
                <Button
                    color="primary"
                    rounded
                    onClick={this.props.onSignIn.bind(this)}>{t`Sign in`}</Button>
            </div>
        );
    }
}

SignInView.defaultProps = {
    onSignIn: () => {
    }
};

const WrappedSignInView = withRouter(SignInView);

WrappedSignInView.WrappedComponent.contextType = UserContext;

export default WrappedSignInView;

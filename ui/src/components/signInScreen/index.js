import React from 'react';

class SignInScreen extends React.Component {
  constructor(props) {
    super(props);
  }
  state = {
    isSignedIn: false
  };

  componentDidMount() {
  }
  
  componentWillUnmount() {
    this.unregisterAuthObserver();
  }

  render() {
    if (!this.state.isSignedIn) {
      return (
        <div>
          <p>Please sign-in:</p>
          <button>sign in</button>
        </div>
      );
    }
    return (
      <div>
        <p>Welcome NAME! You are now signed-in!</p>
        <a>Sign-out</a>
      </div>
    );
  }
}

export {SignInScreen}

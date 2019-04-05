import React from 'react'

export const AppContext = React.createContext();

export class Provider extends React.Component {
	state = {
		user: null
	};

	render() {
		return (
			<AppContext.Provider value={{
				state: this.state,
				setState: (newState) => {this.setState(newState)}
			}}>
				{this.props.children}
			</AppContext.Provider>
		);
	}
}

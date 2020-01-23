import React from 'react'

export const UserContext = React.createContext(null);

export class UserProvider extends React.Component {
    state = {
        user: null,
        token: null,
        theme: localStorage.getItem('theme')
    };

    render() {
        return (
            <UserContext.Provider value={{
                state: this.state,
                setState: (newState) => {
                    this.setState(newState)
                }
            }}>
                {this.props.children}
            </UserContext.Provider>
        );
    }
}

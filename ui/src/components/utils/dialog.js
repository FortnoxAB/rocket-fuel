import React from 'react';
import PropTypes from 'prop-types';

class Dialog extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        if (!this.props.isOpen) {
            return null;
        }
        return (
            <div className="dialog-blocker">
                <div className="dialog">
                    <div className="title">{this.props.title}</div>
                    <div className="body">{this.props.children}</div>
                </div>
            </div>
        );
    }
}

Dialog.propTypes = {
    isOpen: PropTypes.bool,
    title: PropTypes.string,
    children: PropTypes.node.isRequired
};

Dialog.defaultProps = {
    isOpen: false,
    title: ''
};

export default Dialog;

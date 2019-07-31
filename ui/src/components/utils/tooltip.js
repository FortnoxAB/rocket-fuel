import React from 'react';
import PropTypes from 'prop-types';

class Tooltip extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
          visible: false
        };
    }

    showTooltip() {
        this.setState({
            visible: true
        });
    }

    hideTooltip() {
        this.setState({
            visible: false
        });
    }

    renderTooltipContent() {
        if (!this.state.visible) {
            return null;
        }

        return (
            <span className="tooltip">
                {this.props.content}
            </span>
        );
    }

    render() {
        return (
            <div
                className="tooltip-wrap"
                onMouseEnter={this.showTooltip.bind(this)}
                onMouseLeave={this.hideTooltip.bind(this)}
            >
                {this.props.children}
                {this.renderTooltipContent()}
            </div>
        );
    }
}

Tooltip.defaultProps = {
    content: null
};

Tooltip.propTypes = {
    content: PropTypes.node
};

export default Tooltip;
